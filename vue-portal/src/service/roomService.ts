import { apiGet, apiRequest, jsonPost } from '@/service/httpClient'
import type { RoomInfo, RoomInvite, RoomSession, TeamRoom } from '@/model/types'

export function listRooms(): Promise<RoomInfo[]> {
  return apiRequest<RoomInfo[]>('/api/rooms')
}

export function fetchMyTeamRoom(): Promise<TeamRoom | null> {
  return apiGet<TeamRoom | null>('/api/rooms/mine')
}

export function fetchRoomInvites(): Promise<RoomInvite[]> {
  return apiGet<RoomInvite[]>('/api/rooms/invites')
}

export function rejectRoomInvite(roomId: string): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/invites/${encodeURIComponent(roomId)}/reject`, {})
}

export function createRoom(roomName: string): Promise<RoomSession> {
  return jsonPost<RoomSession>('/api/rooms', { roomName })
}

export function joinRoom(roomId: string): Promise<RoomSession> {
  return jsonPost<RoomSession>(`/api/rooms/${encodeURIComponent(roomId)}/join`, {})
}

export function inviteFriendToRoom(roomId: string, friendUserId: number): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/invite`, { friendUserId })
}

export function startRoomGame(roomId: string, levelNumber = 1): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/start`, { levelNumber })
}

export function endRoomRound(roomId: string): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/end-round`, {})
}

export function abandonLobby(roomId: string): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/abandon-lobby`, {})
}

export function leaveRoom(
  roomId: string,
  playerId: string,
  action: 'LEAVE' | 'DISSOLVE' | 'TRANSFER_HOST' = 'LEAVE',
  newHostPlayerId?: string,
): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/leave`, {
    playerId,
    action,
    newHostPlayerId,
  })
}

export function sendChat(roomId: string, playerId: string, text: string): Promise<import('@/model/types').ChatMessage> {
  return jsonPost(`/api/rooms/${encodeURIComponent(roomId)}/chat`, { playerId, text })
}
