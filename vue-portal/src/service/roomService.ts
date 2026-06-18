import { apiRequest, jsonPost } from '@/service/httpClient'
import type { ChatMessage, RoomInfo, RoomSession } from '@/model/types'

export function listRooms(): Promise<RoomInfo[]> {
  return apiRequest<RoomInfo[]>('/api/rooms')
}

export function createRoom(roomName: string, hostName: string): Promise<RoomSession> {
  return jsonPost<RoomSession>('/api/rooms', { roomName, hostName })
}

export function joinRoom(roomId: string, displayName: string): Promise<RoomSession> {
  return jsonPost<RoomSession>(`/api/rooms/${encodeURIComponent(roomId)}/join`, { displayName })
}

export function leaveRoom(roomId: string, playerId: string): Promise<boolean> {
  return jsonPost<boolean>(`/api/rooms/${encodeURIComponent(roomId)}/leave`, { playerId })
}

export function sendChat(roomId: string, playerId: string, text: string): Promise<ChatMessage> {
  return jsonPost<ChatMessage>(`/api/rooms/${encodeURIComponent(roomId)}/chat`, {
    playerId,
    text,
  })
}
