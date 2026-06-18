<script setup lang="ts">
import type { RoomInvite } from '@/model/types'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  invites: RoomInvite[]
  loading: boolean
}>()

const emit = defineEmits<{
  accept: [invite: RoomInvite]
  reject: [invite: RoomInvite]
}>()
</script>

<template>
  <section v-if="invites.length" class="invite-strip" aria-label="收到的组队邀请">
    <p class="invite-title">组队邀请</p>
    <div class="invite-list">
      <div
        v-for="invite in invites"
        :key="`${invite.roomId}-${invite.createdAtMs}`"
        class="invite-chip"
      >
        <span class="invite-text">{{ invite.fromDisplayName }} 邀请你加入「{{ invite.roomName }}」</span>
        <div class="invite-actions">
          <GlassButton accent :disabled="loading" @click="emit('accept', invite)">接受</GlassButton>
          <GlassButton :disabled="loading" @click="emit('reject', invite)">拒绝</GlassButton>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.invite-strip {
  flex-shrink: 0;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid rgba(120, 200, 140, 0.28);
  background: rgba(20, 40, 32, 0.72);
}

.invite-title {
  margin: 0 0 8px;
  font-size: 0.78rem;
  letter-spacing: 0.08em;
  color: #a8f0c8;
}

.invite-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.invite-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.invite-text {
  font-size: 0.84rem;
  color: var(--text-primary);
  min-width: 0;
  flex: 1;
}

.invite-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
