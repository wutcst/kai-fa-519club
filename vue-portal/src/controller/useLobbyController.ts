import { useRouter } from 'vue-router'
import { onMounted, onUnmounted, ref } from 'vue'
import type { FriendRequest, FriendView, GameState, RoomInfo, RoomInvite, RoomMember, TeamRoom } from '@/model/types'
import { authSessionModel, clearAuthSession } from '@/model/authModel'
import { LOBBY_REFRESH_MS } from '@/model/theme'
import { clearSession, saveSession, sessionModel, updateGameState } from '@/model/sessionModel'
import { normalizeGameState } from '@/model/types'
import * as authService from '@/service/authService'
import * as friendService from '@/service/friendService'
import * as roomService from '@/service/roomService'
import * as soloService from '@/service/soloService'
import { unlockGameBgm } from '@/service/gameBgm'
import type { SoloLevelSelection } from '@/model/soloTypes'
import { canInviteFriendToTeam } from '@/util/friendInvite'
import { acceptRoomInvite, pendingInvites, pollRoomInvites, rejectRoomInvite } from '@/model/roomInviteStore'

/**
 * 组队大厅 Controller：好友、房间、邀请与进局。
 */
export function useLobbyController() {
  const router = useRouter()
  const rooms = ref<RoomInfo[]>([])
  const friends = ref<FriendView[]>([])
  const friendRequests = ref<FriendRequest[]>([])
  const myTeamRoom = ref<TeamRoom | null>(null)
  const loading = ref(false)
  const error = ref('')
  const roomName = ref('')
  const friendUsername = ref('')
  const selectedRoomId = ref('')
  const hostLeaveVisible = ref(false)
  const levelSelectVisible = ref(false)
  const levelSelection = ref<SoloLevelSelection | null>(null)
  const confirmVisible = ref(false)
  const confirmTitle = ref('')
  const confirmMessage = ref('')
  const confirmDanger = ref(false)
  const toastMessage = ref('')
  const lastMembers = ref<RoomMember[]>([])
  let refreshTimer: ReturnType<typeof setInterval> | null = null
  let toastTimer: ReturnType<typeof setTimeout> | null = null
  let confirmAction: (() => void) | null = null
  let friendRequestNotificationsReady = false

  function isAuthErrorMessage(message: string): boolean {
    return message.includes('请先登录') || message.includes('登录已过期')
  }

  function handleAuthFailure(message: string) {
    clearAuthSession()
    error.value = message
    router.push({ name: 'auth', query: { redirect: '/multiplayer' } })
  }

  async function ensureServerAuth(): Promise<boolean> {
    try {
      await authService.fetchProfile()
      return true
    } catch (exception) {
      const message = exception instanceof Error ? exception.message : '登录已过期，请重新登录'
      handleAuthFailure(isAuthErrorMessage(message) ? '登录已过期，请重新登录' : message)
      return false
    }
  }

  async function refreshFriends() {
    try {
      friends.value = await friendService.listFriends()
    } catch {
      friends.value = []
    }
  }

  async function refreshFriendRequests() {
    try {
      const previous = friendRequests.value.map((request) => request.userId)
      friendRequests.value = await friendService.listIncomingFriendRequests()
      if (friendRequestNotificationsReady) {
        for (const request of friendRequests.value) {
          if (!previous.includes(request.userId)) {
            showToast(`${request.displayName} 请求添加你为好友`)
          }
        }
      } else {
        friendRequestNotificationsReady = true
      }
    } catch {
      friendRequests.value = []
    }
  }

  async function refreshInvites() {
    await pollRoomInvites()
  }

  function showToast(message: string, autoHideMs = 5000) {
    toastMessage.value = message
    if (toastTimer) {
      clearTimeout(toastTimer)
    }
    if (autoHideMs > 0) {
      toastTimer = setTimeout(() => {
        toastMessage.value = ''
      }, autoHideMs)
    }
  }

  function clearToast() {
    toastMessage.value = ''
    if (toastTimer) {
      clearTimeout(toastTimer)
      toastTimer = null
    }
  }

  function detectMemberLeaves(team: TeamRoom) {
    if (!team.host || lastMembers.value.length === 0) {
      lastMembers.value = [...team.members]
      return
    }
    const currentIds = new Set(team.members.map((member) => member.playerId))
    for (const member of lastMembers.value) {
      if (!member.host && !currentIds.has(member.playerId)) {
        showToast(`${member.displayName} 已离开队伍`)
      }
    }
    lastMembers.value = [...team.members]
  }

  function openConfirm(title: string, message: string, action: () => void, danger = false) {
    confirmTitle.value = title
    confirmMessage.value = message
    confirmDanger.value = danger
    confirmAction = action
    confirmVisible.value = true
  }

  function closeConfirm() {
    confirmVisible.value = false
    confirmAction = null
  }

  function confirmDialog() {
    const action = confirmAction
    closeConfirm()
    action?.()
  }

  async function tryEnterActiveGame(team: TeamRoom) {
    if (!team.inGame || router.currentRoute.value.name === 'multiplayer-room') {
      return
    }
    try {
      const joined = await roomService.joinRoom(team.roomId)
      enterGameRoom(joined.state)
    } catch {
      // 进局失败时保持在大厅
    }
  }

  async function refreshMyTeamRoom() {
    try {
      const previous = myTeamRoom.value
      myTeamRoom.value = await roomService.fetchMyTeamRoom()
      if (myTeamRoom.value) {
        detectMemberLeaves(myTeamRoom.value)
        saveSession({
          roomId: myTeamRoom.value.roomId,
          roomName: myTeamRoom.value.roomName,
          playerId: myTeamRoom.value.playerId,
          displayName: authSessionModel.value?.displayName ?? '玩家',
          isHost: myTeamRoom.value.host,
        })
        if (!previous?.inGame && myTeamRoom.value.inGame) {
          await tryEnterActiveGame(myTeamRoom.value)
        }
      } else {
        clearSession()
        lastMembers.value = []
      }
    } catch {
      myTeamRoom.value = null
      lastMembers.value = []
    }
  }

  async function refreshRooms() {
    loading.value = true
    error.value = ''
    try {
      rooms.value = await roomService.listRooms()
      if (!selectedRoomId.value && rooms.value.length > 0) {
        selectedRoomId.value = rooms.value[0].roomId
      }
      await Promise.all([refreshFriends(), refreshFriendRequests(), refreshInvites(), refreshMyTeamRoom()])
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '无法连接联机服务器'
    } finally {
      loading.value = false
    }
  }

  function enterGameRoom(state: GameState | null) {
    const team = myTeamRoom.value
    if (!team) {
      return
    }
    saveSession({
      roomId: team.roomId,
      roomName: team.roomName,
      playerId: team.playerId,
      displayName: authSessionModel.value?.displayName ?? '玩家',
      isHost: team.host,
    })
    updateGameState(normalizeGameState(state))
    router.push('/multiplayer/room')
  }

  async function goToTeamPage() {
    await router.push({ name: 'multiplayer-team' })
  }

  async function goToHallPage() {
    await router.push({ name: 'multiplayer-lobby' })
  }

  async function createRoom() {
    if (!roomName.value.trim()) {
      error.value = '请填写房间名'
      return
    }
    if (!(await ensureServerAuth())) {
      return
    }
    loading.value = true
    error.value = ''
    try {
      await roomService.createRoom(roomName.value.trim())
      roomName.value = ''
      await refreshRooms()
      await goToTeamPage()
    } catch (exception) {
      const message = exception instanceof Error ? exception.message : '创建房间失败'
      if (isAuthErrorMessage(message)) {
        handleAuthFailure('登录已过期，请重新登录')
        return
      }
      error.value = message
    } finally {
      loading.value = false
    }
  }

  async function openLevelSelect() {
    const team = myTeamRoom.value
    if (!team) {
      error.value = '请先创建或加入房间'
      return
    }
    if (!team.host) {
      error.value = '仅房主可以开始游戏'
      return
    }
    error.value = ''
    try {
      const result = await soloService.fetchSoloLevels()
      levelSelection.value = result.selection
      levelSelectVisible.value = true
    } catch {
      error.value = '无法加载关卡列表'
    }
  }

  function closeLevelSelect() {
    levelSelectVisible.value = false
    error.value = ''
  }

  async function confirmStartGame(levelNumber: number) {
    const team = myTeamRoom.value
    if (!team) {
      return
    }
    unlockGameBgm()
    loading.value = true
    error.value = ''
    try {
      await roomService.startRoomGame(team.roomId, levelNumber)
      const joined = await roomService.joinRoom(team.roomId)
      myTeamRoom.value = { ...team, inGame: true }
      levelSelectVisible.value = false
      enterGameRoom(joined.state)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '无法开始游戏'
      try {
        await refreshMyTeamRoom()
      } catch {
        // 忽略
      }
    } finally {
      loading.value = false
    }
  }

  async function joinSelected() {
    if (!selectedRoomId.value) {
      error.value = '请选择要加入的房间'
      return
    }
    const target = rooms.value.find((room) => room.roomId === selectedRoomId.value)
    if (target?.inGame) {
      error.value = '该房间正在游戏中，请等待本局结束'
      return
    }
    if (!(await ensureServerAuth())) {
      return
    }
    loading.value = true
    error.value = ''
    try {
      await roomService.joinRoom(selectedRoomId.value)
      await refreshRooms()
      await goToTeamPage()
    } catch (exception) {
      const message = exception instanceof Error ? exception.message : '加入房间失败'
      if (isAuthErrorMessage(message)) {
        handleAuthFailure('登录已过期，请重新登录')
        return
      }
      error.value = message
    } finally {
      loading.value = false
    }
  }

  async function acceptInvite(invite: RoomInvite) {
    if (!(await ensureServerAuth())) {
      return
    }
    loading.value = true
    error.value = ''
    try {
      await acceptRoomInvite(router, invite)
      await refreshRooms()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '接受邀请失败'
    } finally {
      loading.value = false
    }
  }

  async function rejectInvite(invite: RoomInvite) {
    try {
      await rejectRoomInvite(invite)
      await refreshInvites()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '拒绝邀请失败'
    }
  }

  async function inviteFriend(friend: FriendView) {
    const team = myTeamRoom.value
    if (!team) {
      error.value = '请先创建房间'
      return
    }
    if (!canInviteFriendToTeam(friend, team)) {
      error.value = '该好友已在其他房间或无法邀请'
      return
    }
    try {
      await roomService.inviteFriendToRoom(team.roomId, friend.userId)
      error.value = ''
      showToast(`已向 ${friend.displayName} 发送邀请`)
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '邀请失败'
    }
  }

  async function addFriend() {
    if (!friendUsername.value.trim()) {
      error.value = '请填写好友用户名'
      return
    }
    try {
      const result = await friendService.sendFriendRequest(friendUsername.value.trim())
      friendUsername.value = ''
      if ('status' in result) {
        showToast(`已与 ${result.displayName} 成为好友`)
        await refreshFriends()
      } else {
        showToast(`已向 ${result.displayName} 发送好友申请`)
      }
      await refreshFriendRequests()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '添加好友失败'
    }
  }

  async function acceptFriendRequest(request: FriendRequest) {
    try {
      const friend = await friendService.acceptFriendRequest(request.userId)
      showToast(`已添加 ${friend.displayName} 为好友`)
      await Promise.all([refreshFriends(), refreshFriendRequests()])
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '接受好友申请失败'
    }
  }

  async function rejectFriendRequest(request: FriendRequest) {
    try {
      await friendService.rejectFriendRequest(request.userId)
      await refreshFriendRequests()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '拒绝好友申请失败'
    }
  }

  async function leaveTeamRoom(action: 'LEAVE' | 'DISSOLVE' | 'TRANSFER_HOST' = 'LEAVE', newHostPlayerId?: string) {
    const team = myTeamRoom.value
    if (!team) {
      return
    }
    loading.value = true
    try {
      await roomService.leaveRoom(team.roomId, team.playerId, action, newHostPlayerId)
      clearSession()
      myTeamRoom.value = null
      await refreshRooms()
      await goToHallPage()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '离开房间失败'
    } finally {
      loading.value = false
    }
  }

  async function handleLeaveTeam() {
    const team = myTeamRoom.value
    if (!team) {
      return
    }
    if (!team.host) {
      openConfirm('离开队伍', '确定离开当前房间？', () => void leaveTeamRoom('LEAVE'))
      return
    }
    const others = team.members.filter((member) => !member.host)
    if (others.length === 0) {
      openConfirm('解散房间', '离开后将解散房间，确定吗？', () => void leaveTeamRoom('DISSOLVE'), true)
      return
    }
    hostLeaveVisible.value = true
  }

  function closeHostLeaveModal() {
    hostLeaveVisible.value = false
  }

  async function dissolveTeamRoom() {
    hostLeaveVisible.value = false
    await leaveTeamRoom('DISSOLVE')
  }

  async function transferHostAndLeave(playerId: string) {
    hostLeaveVisible.value = false
    await leaveTeamRoom('TRANSFER_HOST', playerId)
  }

  const hostLeaveCandidates = () =>
    myTeamRoom.value?.members.filter((member) => !member.host) ?? []

  onMounted(async () => {
    if (authSessionModel.value) {
      const authed = await ensureServerAuth()
      if (!authed) {
        return
      }
    }
    await refreshRooms()
    if (sessionModel.value && myTeamRoom.value?.inGame) {
      router.replace('/multiplayer/room')
      return
    }
    if (myTeamRoom.value && router.currentRoute.value.name === 'multiplayer-lobby') {
      await router.replace({ name: 'multiplayer-team' })
    }
    refreshTimer = setInterval(() => void refreshRooms(), LOBBY_REFRESH_MS)
  })

  onUnmounted(() => {
    if (refreshTimer) {
      clearInterval(refreshTimer)
    }
    if (toastTimer) {
      clearTimeout(toastTimer)
    }
  })

  return {
    rooms,
    friends,
    friendRequests,
    invites: pendingInvites,
    myTeamRoom,
    loading,
    error,
    accountDisplayName: authSessionModel,
    roomName,
    friendUsername,
    selectedRoomId,
    refreshRooms,
    createRoom,
    openLevelSelect,
    closeLevelSelect,
    confirmStartGame,
    levelSelectVisible,
    levelSelection,
    joinSelected,
    acceptInvite,
    rejectInvite,
    inviteFriend,
    addFriend,
    acceptFriendRequest,
    rejectFriendRequest,
    handleLeaveTeam,
    hostLeaveVisible,
    closeHostLeaveModal,
    dissolveTeamRoom,
    transferHostAndLeave,
    hostLeaveCandidates,
    confirmVisible,
    confirmTitle,
    confirmMessage,
    confirmDanger,
    closeConfirm,
    confirmDialog,
    toastMessage,
    clearToast,
  }
}
