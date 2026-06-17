<script setup lang="ts">
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  visible: boolean
  message: string
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Transition name="notice-pop">
    <div v-if="visible" class="notice-overlay" @click.self="emit('close')">
      <div class="notice-card glass-hud">
        <p class="notice-text">{{ message }}</p>
        <GlassButton accent @click="emit('close')">关闭</GlassButton>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.notice-overlay {
  position: absolute;
  inset: 0;
  z-index: 30;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.35);
  padding: 24px;
}

.notice-card {
  max-width: 440px;
  width: 100%;
  padding: 22px 24px;
  text-align: center;
}

.notice-text {
  margin: 0 0 16px;
  white-space: pre-wrap;
  line-height: 1.55;
  color: var(--text-primary);
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
}

.notice-pop-enter-active,
.notice-pop-leave-active {
  transition: opacity 0.25s ease;
}

.notice-pop-enter-active .notice-card,
.notice-pop-leave-active .notice-card {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.notice-pop-enter-from,
.notice-pop-leave-to {
  opacity: 0;
}

.notice-pop-enter-from .notice-card,
.notice-pop-leave-to .notice-card {
  transform: translateY(12px) scale(0.96);
  opacity: 0;
}
</style>
