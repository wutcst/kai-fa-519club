import { onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { isLoggedIn } from '@/model/authModel'
import { sessionModel } from '@/model/sessionModel'
import type { PresenceStatus } from '@/model/types'
import * as presenceService from '@/service/presenceService'

const HEARTBEAT_MS = 30000

function resolveStatus(path: string): PresenceStatus {
  if (!isLoggedIn()) {
    return 'OFFLINE'
  }
  if (path.startsWith('/solo')) {
    return 'SOLO_PLAYING'
  }
  if (path.startsWith('/multiplayer/room')) {
    return 'MULTIPLAYER_PLAYING'
  }
  if (path.startsWith('/multiplayer')) {
    return sessionModel.value ? 'IN_ROOM' : 'ONLINE'
  }
  return 'ONLINE'
}

/**
 * 全局在线状态心跳。
 */
export function usePresenceHeartbeat() {
  const route = useRoute()
  let timer: ReturnType<typeof setInterval> | null = null

  async function beat() {
    if (!isLoggedIn()) {
      return
    }
    const status = resolveStatus(route.path)
    const roomId = sessionModel.value?.roomId
    try {
      await presenceService.sendPresenceHeartbeat(status, roomId)
    } catch {
      // 忽略心跳失败
    }
  }

  watch(
    () => route.path,
    () => {
      void beat()
    },
  )

  onMounted(() => {
    void beat()
    timer = setInterval(() => void beat(), HEARTBEAT_MS)
  })

  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
    }
  })
}
