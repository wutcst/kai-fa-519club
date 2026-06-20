import { apiGet, apiRequest, buildAuthHeaders, jsonPost } from '@/service/httpClient'
import type { FriendRequest, FriendView } from '@/model/types'

export function listFriends(): Promise<FriendView[]> {
  return apiGet<FriendView[]>('/api/friends')
}

export function listIncomingFriendRequests(): Promise<FriendRequest[]> {
  return apiGet<FriendRequest[]>('/api/friends/requests/incoming')
}

export function sendFriendRequest(username: string): Promise<FriendView | FriendRequest> {
  return jsonPost<FriendView | FriendRequest>('/api/friends', { username })
}

export function acceptFriendRequest(fromUserId: number): Promise<FriendView> {
  return jsonPost<FriendView>(`/api/friends/requests/${fromUserId}/accept`, {})
}

export function rejectFriendRequest(fromUserId: number): Promise<boolean> {
  return jsonPost<boolean>(`/api/friends/requests/${fromUserId}/reject`, {})
}

export function removeFriend(friendUserId: number): Promise<boolean> {
  return apiRequest<boolean>(`/api/friends/${friendUserId}`, {
    method: 'DELETE',
    headers: buildAuthHeaders(),
  })
}
