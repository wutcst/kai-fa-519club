import type { FriendView, TeamRoom } from '@/model/types'

export function friendInAnotherRoom(friend: FriendView, teamRoom: TeamRoom | null): boolean {
  if (friend.status === 'IN_ROOM' || friend.status === 'MULTIPLAYER_PLAYING') {
    return true
  }
  if (!friend.roomId) {
    return false
  }
  if (!teamRoom) {
    return true
  }
  return friend.roomId !== teamRoom.roomId
}

export function isFriendInTeam(friend: FriendView, teamRoom: TeamRoom | null): boolean {
  return teamRoom?.members.some((member) => member.userId === friend.userId) ?? false
}

export function canInviteFriendToTeam(friend: FriendView, teamRoom: TeamRoom | null): boolean {
  if (friend.status !== 'ONLINE' && friend.status !== 'SOLO_PLAYING') {
    return false
  }
  if (isFriendInTeam(friend, teamRoom)) {
    return false
  }
  if (friendInAnotherRoom(friend, teamRoom)) {
    return false
  }
  return true
}

export function inviteFriendTitle(friend: FriendView, teamRoom: TeamRoom | null): string {
  if (isFriendInTeam(friend, teamRoom)) {
    return '已在队伍中'
  }
  if (friendInAnotherRoom(friend, teamRoom)) {
    return '已在其他房间'
  }
  if (friend.status === 'OFFLINE') {
    return '好友离线'
  }
  if (friend.status === 'SOLO_PLAYING') {
    return '单机中，可邀请'
  }
  if (friend.status === 'IN_ROOM' || friend.status === 'MULTIPLAYER_PLAYING') {
    return '正在联机中'
  }
  return '邀请进房'
}
