import { jsonPost } from '@/service/httpClient'
import type { PresenceStatus } from '@/model/types'

export function sendPresenceHeartbeat(status: PresenceStatus, roomId?: string): Promise<boolean> {
  return jsonPost<boolean>('/api/presence/heartbeat', {
    status,
    roomId: roomId ?? null,
  })
}
