import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGameDisplayMode } from '@/model/gameDisplayMode'
import type { MoveDirection } from '@/model/gameDisplayMode'
import { ROOM_TRANSITION_MS, VIGNETTE_MS, isMoveDirection, sleep } from '@/model/sceneTransition'
import type { CommandResponse, GameState } from '@/model/types'
import { normalizeGameState } from '@/model/types'
import { POLL_INTERVAL_MS } from '@/model/theme'
import {
  isTimerDangerLevel,
  isTimerWarningLevel,
  timerPressureLevel,
} from '@/model/timerPressure'
import {
  clearSession,
  gameStateModel,
  sessionModel,
  updateGameState,
} from '@/model/sessionModel'
import * as gameService from '@/service/gameService'
import * as roomService from '@/service/roomService'
import { playGameSfx } from '@/service/gameSfx'
import { screenShake, triggerScreenShake } from '@/model/gameJuice'

const LOCKED_EXIT_MESSAGE = '夜色中，这个方向暂未开放。'

/**
 * 联机对局 Controller：轮询、点击操作与房间聊天。
 */
export function useGameRoomController() {
  const router = useRouter()
  const chatInput = ref('')
  const busy = ref(false)
  const error = ref('')
  const noticeText = ref('')
  const noticeVisible = ref(false)
  const lockedOverlayMessage = ref<string | null>(null)
  const npcDialogVisible = ref(false)
  const npcDialogTitle = ref('')
  const npcDialogMessage = ref('')
  const polling = ref(false)
  const sceneTransition = ref(false)
  const directionalExit = ref(false)
  const moveDirection = ref<MoveDirection | null>(null)
  const { isImmersive } = useGameDisplayMode()
  let pollTimer: ReturnType<typeof setInterval> | null = null
  let noticeTimer: ReturnType<typeof setTimeout> | null = null
  let lastRoomId = ''

  const session = computed(() => sessionModel.value)
  const gameState = computed(() => gameStateModel.value)

  const remainingSeconds = computed(() => gameState.value?.remainingSeconds ?? 999)
  const timerPressure = computed(() => timerPressureLevel(remainingSeconds.value))
  const timerDanger = computed(() => isTimerDangerLevel(timerPressure.value))
  const timerWarning = computed(() => isTimerWarningLevel(timerPressure.value))

  function showNotice(text: string, autoHideMs = 5000) {
    if (text.trim()) {
      playGameSfx('notice')
    }
    noticeText.value = text
    noticeVisible.value = true
    if (noticeTimer) {
      clearTimeout(noticeTimer)
    }
    if (autoHideMs > 0) {
      noticeTimer = setTimeout(() => {
        noticeVisible.value = false
      }, autoHideMs)
    }
  }

  function hideNotice() {
    noticeVisible.value = false
    if (noticeTimer) {
      clearTimeout(noticeTimer)
      noticeTimer = null
    }
  }

  function showLockedOverlay(message = LOCKED_EXIT_MESSAGE) {
    playGameSfx('blocked')
    triggerScreenShake()
    lockedOverlayMessage.value = message
  }

  function dismissLockedOverlay() {
    lockedOverlayMessage.value = null
  }

  function findLockedMessage(messages: string[]) {
    return messages.find(
      (line) => line.includes('夜色中') || line.includes('暂未开放'),
    )
  }

  function clearDirectionalTransition() {
    directionalExit.value = false
    moveDirection.value = null
  }

  function finishSceneTransition() {
    sceneTransition.value = false
    clearDirectionalTransition()
  }

  function applyState(state: GameState | null) {
    const normalized = normalizeGameState(state)
    if (normalized?.roomId && normalized.roomId !== lastRoomId) {
      sceneTransition.value = true
      setTimeout(() => {
        finishSceneTransition()
      }, ROOM_TRANSITION_MS)
      lastRoomId = normalized.roomId
    }
    updateGameState(normalized)
  }

  async function beginDirectionalMove(direction: MoveDirection) {
    if (!isImmersive.value) {
      return
    }
    moveDirection.value = direction
    directionalExit.value = true
    await sleep(VIGNETTE_MS)
  }

  function handleCommandFeedback(commandWord: string, result: CommandResponse) {
    const lockedLine = findLockedMessage(result.messages ?? [])
    if (lockedLine && (commandWord === 'go' || commandWord === 'back')) {
      showLockedOverlay(lockedLine)
      return
    }
    if (result.noticeMessage?.trim()) {
      showNotice(result.noticeMessage)
    }
    if (result.quitRequested) {
      showNotice('本局已结束。', 8000)
    }
  }

  async function pollOnce() {
    const active = session.value
    if (!active) {
      return
    }
    try {
      const state = await gameService.fetchGameState(active.roomId, active.playerId)
      applyState(state)
    } catch {
      // 轮询失败静默
    }
  }

  function startPolling() {
    stopPolling()
    polling.value = true
    void pollOnce()
    pollTimer = setInterval(() => void pollOnce(), POLL_INTERVAL_MS)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
    polling.value = false
  }

  async function runCommand(commandWord: string, secondWord?: string) {
    const active = session.value
    if (!active || busy.value || lockedOverlayMessage.value) {
      return
    }
    busy.value = true
    error.value = ''
    try {
      const result = await gameService.executeCommand({
        roomId: active.roomId,
        playerId: active.playerId,
        commandWord,
        secondWord,
      })
      if (result.state) {
        applyState(result.state)
      }
      handleCommandFeedback(commandWord, result)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '操作失败'
      showNotice(error.value, 4000)
    } finally {
      busy.value = false
    }
  }

  async function move(direction: string) {
    if (lockedOverlayMessage.value || busy.value) {
      return
    }
    playGameSfx('move')
    if (isMoveDirection(direction)) {
      await beginDirectionalMove(direction)
    }
    await runCommand('go', direction)
    if (directionalExit.value && !sceneTransition.value) {
      clearDirectionalTransition()
    }
  }

  async function moveBack() {
    if (lockedOverlayMessage.value || busy.value) {
      return
    }
    playGameSfx('move')
    await beginDirectionalMove('back')
    await runCommand('back')
    if (directionalExit.value && !sceneTransition.value) {
      clearDirectionalTransition()
    }
  }

  function onBlockedExit() {
    showLockedOverlay()
  }

  async function lookAround() {
    await runCommand('look')
    const bulletin = gameState.value?.bulletin?.trim()
    if (bulletin) {
      showNotice(bulletin, 8000)
    }
  }

  function takeItem(name: string) {
    void runCommand('take', name)
  }

  function inventoryAction(action: 'drop' | 'use' | 'eat' | 'inspect', itemName: string) {
    if (action === 'inspect') {
      void runCommand('inspect', itemName)
      return
    }
    if (action === 'eat') {
      void runCommand('eat', itemName)
      return
    }
    void runCommand(action, itemName)
  }

  async function talkNpc() {
    const active = session.value
    if (!active || busy.value) {
      return
    }
    busy.value = true
    try {
      const result = await gameService.executeCommand({
        roomId: active.roomId,
        playerId: active.playerId,
        commandWord: 'talk',
      })
      if (result.state) {
        applyState(result.state)
      }
      const lines = result.messages?.length
        ? result.messages.join('\n')
        : result.noticeMessage ?? '（对方没有说话。）'
      npcDialogTitle.value = npcTitleForRoom(gameState.value?.roomId ?? '')
      npcDialogMessage.value = lines
      npcDialogVisible.value = true
    } catch (exception) {
      showNotice(exception instanceof Error ? exception.message : '对话失败', 4000)
    } finally {
      busy.value = false
    }
  }

  function npcTitleForRoom(roomId: string) {
    if (roomId === 'supermarket') {
      return '宿管阿姨'
    }
    if (roomId === 'library' || roomId === 'boxue_north') {
      return '志愿者'
    }
    return 'NPC'
  }

  function closeNpcDialog() {
    npcDialogVisible.value = false
  }

  function promptUnlock() {
    const password = window.prompt('请输入寝室智能锁八位密码：')
    if (password?.trim()) {
      void runCommand('unlock', password.trim())
    }
  }

  async function sendChat() {
    const active = session.value
    const text = chatInput.value.trim()
    if (!active || !text || busy.value) {
      return
    }
    busy.value = true
    try {
      await roomService.sendChat(active.roomId, active.playerId, text)
      chatInput.value = ''
      await pollOnce()
    } catch (exception) {
      showNotice(exception instanceof Error ? exception.message : '发送失败', 4000)
    } finally {
      busy.value = false
    }
  }

  async function leaveRoom() {
    const active = session.value
    if (!active) {
      router.push('/multiplayer')
      return
    }
    if (!window.confirm('确定离开联机房间？')) {
      return
    }
    stopPolling()
    try {
      await roomService.leaveRoom(active.roomId, active.playerId)
    } catch {
      // 忽略离开失败
    } finally {
      clearSession()
      router.push('/multiplayer')
    }
  }

  onMounted(() => {
    if (!session.value) {
      router.replace('/multiplayer')
      return
    }
    lastRoomId = gameState.value?.roomId ?? ''
    showNotice('欢迎来到联机对局。\n点击「环顾」查看公告，右下角按钮可打开聊天。', 6000)
    startPolling()
  })

  onUnmounted(() => {
    stopPolling()
    hideNotice()
  })

  return {
    session,
    gameState,
    chatInput,
    busy,
    error,
    noticeText,
    noticeVisible,
    lockedOverlayMessage,
    npcDialogVisible,
    npcDialogTitle,
    npcDialogMessage,
    polling,
    sceneTransition,
    directionalExit,
    moveDirection,
    screenShake,
    remainingSeconds,
    timerPressure,
    timerDanger,
    timerWarning,
    move,
    moveBack,
    onBlockedExit,
    lookAround,
    takeItem,
    inventoryAction,
    talkNpc,
    closeNpcDialog,
    promptUnlock,
    sendChat,
    leaveRoom,
    dismissLockedOverlay,
    hideNotice,
    runCommand,
  }
}
