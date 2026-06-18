<script setup lang="ts">
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  visible: boolean
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  danger?: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <div v-if="visible" class="modal-backdrop" @click.self="emit('cancel')">
    <div class="modal-card" role="dialog" :aria-labelledby="title">
      <h3>{{ title }}</h3>
      <p class="message">{{ message }}</p>
      <div class="actions">
        <GlassButton :danger="danger" :accent="!danger" @click="emit('confirm')">
          {{ confirmLabel ?? '确定' }}
        </GlassButton>
        <GlassButton @click="emit('cancel')">{{ cancelLabel ?? '取消' }}</GlassButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 130;
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
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(12, 16, 28, 0.96);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5);
}

.modal-card h3 {
  margin: 0 0 10px;
  font-size: 1.12rem;
}

.message {
  margin: 0 0 18px;
  color: var(--text-muted);
  font-size: 0.9rem;
  line-height: 1.55;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}
</style>
