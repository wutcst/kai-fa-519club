<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView } from 'vue-router'
import GameBgmControl from '@/component/game/GameBgmControl.vue'
import RoomInviteModal from '@/component/lobby/RoomInviteModal.vue'
import { useGlobalRoomInvites } from '@/controller/useGlobalRoomInvites'
import { usePresenceHeartbeat } from '@/controller/usePresenceHeartbeat'

const route = useRoute()
usePresenceHeartbeat()

const {
  inviteModalVisible,
  inviteModalTarget,
  inviteBusy,
  acceptInvite,
  rejectInvite,
} = useGlobalRoomInvites()

const immersive = computed(() =>
  route.name === 'solo' || route.name === 'multiplayer-room',
)

const lockViewport = computed(() =>
  route.name === 'home'
  || route.name === 'multiplayer-lobby'
  || route.name === 'multiplayer-team',
)
</script>

<template>
  <div class="app-root" :class="{ immersive, 'lock-viewport': lockViewport }">
    <RouterView />
    <GameBgmControl />
    <RoomInviteModal
      :visible="inviteModalVisible"
      :invite="inviteModalTarget"
      :loading="inviteBusy"
      @accept="acceptInvite"
      @reject="rejectInvite"
    />
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
}

.app-root.lock-viewport {
  height: 100vh;
  overflow: hidden;
}

.app-root.immersive {
  overflow: hidden;
}
</style>
