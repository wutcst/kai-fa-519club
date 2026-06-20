import { ref } from 'vue'
import type { Router } from 'vue-router'
import type { RoomInvite } from '@/model/types'
import { isLoggedIn } from '@/model/authModel'
import { clearSession, saveSession, sessionModel } from '@/model/sessionModel'
import { LOBBY_REFRESH_MS } from '@/model/theme'
import * as roomService from '@/service/roomService'

export const pendingInvites = ref<RoomInvite[]>([])
export const inviteModalVisible = ref(false)
export const inviteModalTarget = ref<RoomInvite | null>(null)
export const inviteBusy = ref(false)

let pollTimer: ReturnType<typeof setInterval> | null = null
let knownInviteKeys = new Set<string>()
let pollingReady = false

function inviteKey(invite: RoomInvite): string {
  return `${invite.roomId}-${invite.createdAtMs}`
}

function isGuestInSomeoneElsesRoom(): boolean {
  const session = sessionModel.value
  return !!session?.roomId && session.isHost === false
}

function openInviteModal(invite: RoomInvite) {
  inviteModalTarget.value = invite
  inviteModalVisible.value = true
}

export function closeInviteModal() {
  inviteModalVisible.value = false
  inviteModalTarget.value = null
}

function showNextPendingInvite() {
  if (inviteModalVisible.value || isGuestInSomeoneElsesRoom()) {
    return
  }
  const next = pendingInvites.value[0]
  if (next) {
    openInviteModal(next)
  }
}

async function leaveCurrentTeamIfNeeded(targetRoomId: string): Promise<void> {
  const session = sessionModel.value
  if (session?.roomId && session.playerId) {
    if (session.roomId === targetRoomId) {
      return
    }
    try {
      if (session.isHost) {
        await roomService.leaveRoom(session.roomId, session.playerId, 'DISSOLVE')
      } else {
        await roomService.leaveRoom(session.roomId, session.playerId, 'LEAVE')
      }
    } catch {
      // 房间可能已被解散
    }
    clearSession()
    return
  }

  try {
    const team = await roomService.fetchMyTeamRoom()
    if (!team || team.roomId === targetRoomId) {
      return
    }
    if (team.host) {
      await roomService.leaveRoom(team.roomId, team.playerId, 'DISSOLVE')
    } else {
      await roomService.leaveRoom(team.roomId, team.playerId, 'LEAVE')
    }
    clearSession()
  } catch {
    // 忽略
  }
}

export async function pollRoomInvites(): Promise<void> {
  if (!isLoggedIn()) {
    pendingInvites.value = []
    knownInviteKeys.clear()
    pollingReady = false
    closeInviteModal()
    return
  }

  if (isGuestInSomeoneElsesRoom()) {
    pendingInvites.value = []
    knownInviteKeys.clear()
    closeInviteModal()
    return
  }

  try {
    const list = await roomService.fetchRoomInvites()
    pendingInvites.value = list
    const currentKeys = new Set(list.map(inviteKey))

    if (!pollingReady) {
      knownInviteKeys = currentKeys
      pollingReady = true
      if (list.length > 0) {
        openInviteModal(list[0])
      }
      return
    }

    for (const invite of list) {
      const key = inviteKey(invite)
      if (!knownInviteKeys.has(key)) {
        knownInviteKeys.add(key)
        if (!inviteModalVisible.value) {
          openInviteModal(invite)
        }
        break
      }
    }

    knownInviteKeys = new Set([...knownInviteKeys].filter((key) => currentKeys.has(key)))

    if (!list.length) {
      knownInviteKeys.clear()
      closeInviteModal()
    }
  } catch {
    // 忽略轮询失败
  }
}

export function startRoomInvitePolling(): void {
  void pollRoomInvites()
  if (pollTimer) {
    return
  }
  pollTimer = setInterval(() => void pollRoomInvites(), LOBBY_REFRESH_MS)
}

export function stopRoomInvitePolling(): void {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  pendingInvites.value = []
  knownInviteKeys.clear()
  pollingReady = false
  closeInviteModal()
}

export async function acceptRoomInvite(router: Router, invite?: RoomInvite): Promise<void> {
  const target = invite ?? inviteModalTarget.value
  if (!target) {
    return
  }
  inviteBusy.value = true
  try {
    await leaveCurrentTeamIfNeeded(target.roomId)
    const joined = await roomService.joinRoom(target.roomId)
    saveSession({
      roomId: joined.roomId,
      roomName: joined.roomName,
      playerId: joined.playerId,
      displayName: joined.displayName,
      isHost: joined.host === true,
    })
    pendingInvites.value = []
    knownInviteKeys.clear()
    closeInviteModal()
    await router.push('/multiplayer/team')
  } finally {
    inviteBusy.value = false
  }
}

export async function rejectRoomInvite(invite?: RoomInvite): Promise<void> {
  const target = invite ?? inviteModalTarget.value
  if (!target) {
    return
  }
  inviteBusy.value = true
  try {
    await roomService.rejectRoomInvite(target.roomId)
    knownInviteKeys.delete(inviteKey(target))
    closeInviteModal()
    await pollRoomInvites()
    showNextPendingInvite()
  } catch {
    closeInviteModal()
    await pollRoomInvites()
    showNextPendingInvite()
  } finally {
    inviteBusy.value = false
  }
}
