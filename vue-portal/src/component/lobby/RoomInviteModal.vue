<script setup lang="ts">
import type { RoomInvite } from '@/model/types'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  visible: boolean
  invite: RoomInvite | null
  loading?: boolean
}>()

const emit = defineEmits<{
  accept: []
  reject: []
}>()
</script>

<template>
  <div v-if="visible && invite" class="modal-backdrop" @click.self="emit('reject')">
    <div class="modal-card" role="dialog" aria-labelledby="invite-modal-title">
      <p class="eyebrow">组队邀请</p>
      <h3 id="invite-modal-title">收到联机邀请</h3>
      <p class="message">
        <strong>{{ invite.fromDisplayName }}</strong> 邀请你加入房间
        <span class="room-name">「{{ invite.roomName }}」</span>
      </p>
      <p class="hint">接受后将离开当前页面并进入队伍</p>
      <div class="actions">
        <GlassButton accent :disabled="loading" @click="emit('accept')">接受</GlassButton>
        <GlassButton :disabled="loading" @click="emit('reject')">拒绝</GlassButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(4, 6, 12, 0.78);
  backdrop-filter: blur(6px);
}

.modal-card {
  width: min(400px, calc(100vw - 32px));
  padding: 24px;
  border-radius: 18px;
  border: 1px solid rgba(120, 200, 140, 0.35);
  background: rgba(12, 20, 18, 0.96);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 0.72rem;
  letter-spacing: 0.14em;
  color: #a8f0c8;
}

.modal-card h3 {
  margin: 0 0 12px;
  font-size: 1.12rem;
}

.message {
  margin: 0 0 8px;
  font-size: 0.92rem;
  line-height: 1.55;
}

.room-name {
  color: #b8e8c8;
}

.hint {
  margin: 0 0 18px;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}
</style>
