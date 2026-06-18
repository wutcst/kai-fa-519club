<script setup lang="ts">
import type { OutcomeOverlay } from '@/model/soloTypes'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  outcome: OutcomeOverlay | null
}>()

const emit = defineEmits<{
  action: []
}>()
</script>

<template>
  <Transition name="outcome-fade">
    <div v-if="outcome" class="outcome-layer">
      <div class="outcome-card glass-hud">
        <h2>{{ outcome.title }}</h2>
        <p>{{ outcome.message }}</p>
        <GlassButton accent @click="emit('action')">{{ outcome.actionLabel }}</GlassButton>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.outcome-layer {
  position: absolute;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.55);
  padding: 24px;
}

.outcome-card {
  max-width: 480px;
  width: 100%;
  padding: 28px 32px;
  text-align: center;
}

.outcome-card h2 {
  margin: 0 0 12px;
  color: var(--accent);
}

.outcome-card p {
  margin: 0 0 20px;
  white-space: pre-wrap;
  line-height: 1.55;
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
}

.outcome-fade-enter-active,
.outcome-fade-leave-active {
  transition: opacity 0.3s ease;
}

.outcome-fade-enter-from,
.outcome-fade-leave-to {
  opacity: 0;
}
</style>
