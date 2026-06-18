import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useGameDisplayMode } from '@/model/gameDisplayMode'
import type { MoveDirection } from '@/model/gameDisplayMode'
import { ROOM_TRANSITION_MS, VIGNETTE_MS, isMoveDirection, sleep } from '@/model/sceneTransition'
import { POLL_INTERVAL_MS } from '@/model/theme'
import {
  isTimerDangerLevel,
  isTimerWarningLevel,
  timerPressureLevel,
} from '@/model/timerPressure'
import type { SoloViewState } from '@/model/soloTypes'
import {
  clearSoloSession,
  saveSoloSession,
  updateSoloViewState,
  useSoloSessionModel,
} from '@/model/soloSessionModel'
import * as soloService from '@/service/soloService'
import { playGameSfx } from '@/service/gameSfx'
import { screenShake, triggerScreenShake } from '@/model/gameJuice'

/**
 * 单机五关 Controller（MVC 之 C）。
 */
export function useSoloGameController() {
  const router = useRouter()
  const { soloSession, soloViewState } = useSoloSessionModel()

  const busy = ref(false)
  const error = ref('')
  const noticeText = ref('')
  const noticeVisible = ref(false)
  const combineVisible = ref(false)
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
  let lastLockedMessage = ''

  const viewState = computed(() => soloViewState.value)
  const remainingSeconds = computed(() => viewState.value?.remainingSeconds ?? 999)
  const timerPressure = computed(() => timerPressureLevel(remainingSeconds.value))
  const timerDanger = computed(() => isTimerDangerLevel(timerPressure.value))
  const timerWarning = computed(() => isTimerWarningLevel(timerPressure.value))

  function clearDirectionalTransition() {
    directionalExit.value = false
    moveDirection.value = null
  }

  function finishSceneTransition() {
    sceneTransition.value = false
    clearDirectionalTransition()
  }

  function feedbackLocked(message: string | null | undefined) {
    const trimmed = message?.trim() ?? ''
    if (trimmed && trimmed !== lastLockedMessage) {
      playGameSfx('blocked')
      triggerScreenShake()
    }
    lastLockedMessage = trimmed
  }

  function applyState(state: SoloViewState) {
    feedbackLocked(state.lockedOverlayMessage)
    if (state.roomId && state.roomId !== lastRoomId) {
      sceneTransition.value = true
      setTimeout(() => {
        finishSceneTransition()
      }, ROOM_TRANSITION_MS)
      lastRoomId = state.roomId
    }
    updateSoloViewState(state)
  }

  async function beginDirectionalMove(direction: MoveDirection) {
    if (!isImmersive.value) {
      return
    }
    moveDirection.value = direction
    directionalExit.value = true
    await sleep(VIGNETTE_MS)
  }

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
  }

  function handleCommandResponse(response: Awaited<ReturnType<typeof soloService.executeSoloCommand>>) {
    applyState(response.state)
    if (response.combinePrompt) {
      combineVisible.value = true
    }
    if (response.popupMessage?.trim()) {
      showNotice(response.popupMessage)
    }
  }

  async function pollOnce() {
    const sessionId = soloSession.value?.sessionId
    if (!sessionId) {
      return
    }
    try {
      const state = await soloService.fetchSoloState(sessionId)
      applyState(state)
    } catch {
      // 静默
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
    const sessionId = soloSession.value?.sessionId
    if (!sessionId || busy.value || viewState.value?.interactionBlocked) {
      return
    }
    busy.value = true
    error.value = ''
    try {
      const response = await soloService.executeSoloCommand(sessionId, commandWord, secondWord)
      handleCommandResponse(response)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '命令失败'
      showNotice(error.value, 4000)
    } finally {
      busy.value = false
    }
  }

  async function lookAround() {
    const sessionId = soloSession.value?.sessionId
    if (!sessionId || busy.value) {
      return
    }
    busy.value = true
    try {
      const response = await soloService.performSoloLook(sessionId)
      handleCommandResponse(response)
    } catch (exception) {
      showNotice(exception instanceof Error ? exception.message : '环顾失败', 4000)
    } finally {
      busy.value = false
    }
  }

  async function talkNpc() {
    const sessionId = soloSession.value?.sessionId
    if (!sessionId || busy.value) {
      return
    }
    busy.value = true
    try {
      const response = await soloService.performSoloTalk(sessionId)
      applyState(response.state)
      const lines = response.messages?.length
        ? response.messages.join('\n')
        : response.popupMessage ?? '（对方没有说话。）'
      npcDialogTitle.value = npcTitleForRoom(response.state.roomId)
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
    if (roomId === 'library') {
      return '志愿者'
    }
    if (roomId === 'boxue_north') {
      return '志愿者'
    }
    return 'NPC'
  }

  function closeNpcDialog() {
    npcDialogVisible.value = false
  }

  async function move(direction: string) {
    if (viewState.value?.lockedOverlayMessage || busy.value) {
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
    if (viewState.value?.lockedOverlayMessage || busy.value) {
      return
    }
    playGameSfx('move')
    await beginDirectionalMove('back')
    await runCommand('back')
    if (directionalExit.value && !sceneTransition.value) {
      clearDirectionalTransition()
    }
  }

  function takeItem(name: string) {
    void runCommand('take', name)
  }

  function inspectItem(item: { name: string; longDescription: string }) {
    showNotice(item.longDescription || item.name, 6000)
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

  async function confirmCombine() {
    combineVisible.value = false
    await runCommand('combine')
  }

  function cancelCombine() {
    combineVisible.value = false
  }

  async function dismissOutcome() {
    const sessionId = soloSession.value?.sessionId
    if (!sessionId) {
      return
    }
    busy.value = true
    try {
      const state = await soloService.dismissSoloOutcome(sessionId)
      applyState(state)
    } finally {
      busy.value = false
    }
  }

  function promptUnlock() {
    const password = window.prompt('请输入寝室智能锁八位密码：')
    if (password?.trim()) {
      void runCommand('unlock', password.trim())
    }
  }

  async function startNewGame(playerName: string) {
    busy.value = true
    try {
      const created = await soloService.createSoloSession(playerName)
      saveSoloSession({ sessionId: created.sessionId, playerName })
      applyState(created.state)
      lastRoomId = created.state.roomId
      showNotice('欢迎来到《熄灯前归寝》。\n点击「环顾」查看本关任务与公告。', 6000)
      startPolling()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '无法开始游戏'
    } finally {
      busy.value = false
    }
  }

  async function resumeOrRedirect() {
    if (!soloSession.value) {
      router.replace('/')
      return false
    }
    try {
      const state = await soloService.fetchSoloState(soloSession.value.sessionId)
      applyState(state)
      lastRoomId = state.roomId
      startPolling()
      return true
    } catch {
      clearSoloSession()
      router.replace('/')
      return false
    }
  }

  async function dismissLocked() {
    const sessionId = soloSession.value?.sessionId
    if (!sessionId) {
      return
    }
    const state = await soloService.dismissSoloLocked(sessionId)
    applyState(state)
  }

  async function quitGame() {
    const sessionId = soloSession.value?.sessionId
    stopPolling()
    if (sessionId) {
      try {
        await soloService.destroySoloSession(sessionId)
      } catch {
        // ignore
      }
    }
    clearSoloSession()
    router.push('/')
  }

  onMounted(() => {
    void resumeOrRedirect()
  })

  onUnmounted(() => {
    stopPolling()
    hideNotice()
  })

  return {
    soloSession,
    viewState,
    busy,
    error,
    noticeText,
    noticeVisible,
    combineVisible,
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
    lookAround,
    talkNpc,
    closeNpcDialog,
    takeItem,
    inspectItem,
    inventoryAction,
    runCommand,
    confirmCombine,
    cancelCombine,
    dismissOutcome,
    promptUnlock,
    dismissLocked,
    startNewGame,
    quitGame,
    hideNotice,
  }
}
