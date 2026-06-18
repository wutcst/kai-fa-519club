import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { GameState, RoomInfo } from '@/model/types'
import { LOBBY_REFRESH_MS } from '@/model/theme'
import { saveSession, sessionModel, updateGameState } from '@/model/sessionModel'
import { normalizeGameState } from '@/model/types'
import * as roomService from '@/service/roomService'
import { unlockGameBgm } from '@/service/gameBgm'

/**
 * 大厅 Controller：协调房间列表与进房逻辑（MVC 之 C）。
 */
export function useLobbyController() {
  const router = useRouter()
  const rooms = ref<RoomInfo[]>([])
  const loading = ref(false)
  const error = ref('')
  const displayName = ref('')
  const roomName = ref('')
  const selectedRoomId = ref('')
  let refreshTimer: ReturnType<typeof setInterval> | null = null

  async function refreshRooms() {
    loading.value = true
    error.value = ''
    try {
      rooms.value = await roomService.listRooms()
      if (!selectedRoomId.value && rooms.value.length > 0) {
        selectedRoomId.value = rooms.value[0].roomId
      }
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '无法连接联机服务器'
    } finally {
      loading.value = false
    }
  }

  function enterRoom(
    roomId: string,
    roomTitle: string,
    playerId: string,
    name: string,
    state: GameState | null,
  ) {
    saveSession({ roomId, roomName: roomTitle, playerId, displayName: name })
    updateGameState(normalizeGameState(state))
    router.push('/multiplayer/room')
  }

  async function createAndEnter() {
    if (!displayName.value.trim() || !roomName.value.trim()) {
      error.value = '请填写昵称和房间名'
      return
    }
    unlockGameBgm()
    loading.value = true
    error.value = ''
    try {
      const created = await roomService.createRoom(roomName.value.trim(), displayName.value.trim())
      enterRoom(created.roomId, created.roomName, created.playerId, created.displayName, created.state)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '创建房间失败'
      loading.value = false
    }
  }

  async function joinSelected() {
    if (!displayName.value.trim()) {
      error.value = '请填写昵称'
      return
    }
    if (!selectedRoomId.value) {
      error.value = '请选择要加入的房间'
      return
    }
    unlockGameBgm()
    loading.value = true
    error.value = ''
    try {
      const joined = await roomService.joinRoom(selectedRoomId.value, displayName.value.trim())
      enterRoom(joined.roomId, joined.roomName, joined.playerId, joined.displayName, joined.state)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '加入房间失败'
      loading.value = false
    }
  }

  onMounted(() => {
    if (sessionModel.value) {
      router.replace('/multiplayer/room')
      return
    }
    void refreshRooms()
    refreshTimer = setInterval(() => void refreshRooms(), LOBBY_REFRESH_MS)
  })

  onUnmounted(() => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
    }
  })

  return {
    rooms,
    loading,
    error,
    displayName,
    roomName,
    selectedRoomId,
    refreshRooms,
    createAndEnter,
    joinSelected,
  }
}
