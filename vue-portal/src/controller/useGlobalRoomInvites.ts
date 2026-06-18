import { onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authSessionModel, isLoggedIn } from '@/model/authModel'
import { sessionModel } from '@/model/sessionModel'
import {
  acceptRoomInvite,
  inviteBusy,
  inviteModalTarget,
  inviteModalVisible,
  pollRoomInvites,
  rejectRoomInvite,
  startRoomInvitePolling,
  stopRoomInvitePolling,
} from '@/model/roomInviteStore'

/**
 * 全局组队邀请：登录后在任意页面轮询并弹窗（作客于他人房间时除外）。
 */
export function useGlobalRoomInvites() {
  const router = useRouter()

  watch(
    authSessionModel,
    (session) => {
      if (session) {
        startRoomInvitePolling()
      } else {
        stopRoomInvitePolling()
      }
    },
    { immediate: true },
  )

  watch(
    () => sessionModel.value?.isHost,
    () => {
      if (isLoggedIn()) {
        void pollRoomInvites()
      }
    },
  )

  onUnmounted(() => {
    stopRoomInvitePolling()
  })

  async function acceptInvite() {
    await acceptRoomInvite(router)
    await pollRoomInvites()
  }

  async function rejectInvite() {
    await rejectRoomInvite()
  }

  return {
    inviteModalVisible,
    inviteModalTarget,
    inviteBusy,
    acceptInvite,
    rejectInvite,
  }
}
