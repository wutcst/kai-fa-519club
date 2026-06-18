<script setup lang="ts">
defineProps<{
  message: string
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Transition name="toast">
    <div v-if="message" class="lobby-toast" role="status">
      <span>{{ message }}</span>
      <button type="button" class="close-btn" aria-label="关闭" @click="emit('close')">×</button>
    </div>
  </Transition>
</template>

<style scoped>
.lobby-toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 90;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  border-radius: 999px;
  border: 1px solid rgba(120, 200, 140, 0.35);
  background: rgba(8, 18, 14, 0.92);
  color: #c8f0d4;
  font-size: 0.88rem;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);
  max-width: min(480px, calc(100vw - 32px));
}

.close-btn {
  border: none;
  background: transparent;
  color: rgba(200, 240, 212, 0.7);
  font-size: 1.2rem;
  line-height: 1;
  cursor: pointer;
  padding: 0 2px;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-8px);
}
</style>
