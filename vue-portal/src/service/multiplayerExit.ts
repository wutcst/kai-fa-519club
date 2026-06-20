import { clearSession, sessionModel } from '@/model/sessionModel'
import * as roomService from '@/service/roomService'

let releasing = false

/**
 * 离开联机（回首页或登出）时释放房间：房主解散房间，队员离开房间。
 */
export async function releaseMultiplayerRoom(): Promise<void> {
  if (releasing) {
    return
  }
  releasing = true
  try {
    const session = sessionModel.value
    if (session?.roomId && session.playerId) {
      await leaveCurrentRoom(session.roomId, session.playerId, session.isHost === true)
      return
    }
    const team = await roomService.fetchMyTeamRoom()
    if (!team) {
      clearSession()
      return
    }
    await leaveCurrentRoom(team.roomId, team.playerId, team.host)
  } catch {
    clearSession()
  } finally {
    releasing = false
  }
}

async function leaveCurrentRoom(roomId: string, playerId: string, isHost: boolean): Promise<void> {
  try {
    if (isHost) {
      await roomService.leaveRoom(roomId, playerId, 'DISSOLVE')
    } else {
      await roomService.leaveRoom(roomId, playerId, 'LEAVE')
    }
  } catch {
    // 房间可能已被解散
  } finally {
    clearSession()
  }
}
