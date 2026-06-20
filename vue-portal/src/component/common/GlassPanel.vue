<script setup lang="ts">
defineProps<{
  strong?: boolean
  padding?: string
}>()
</script>

<template>
  <div class="glass-panel" :class="{ strong }" :style="padding ? { padding } : undefined">
    <slot />
  </div>
</template>

<style scoped>
.glass-panel {
  background: var(--hud-bg);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35);
}

.glass-panel.strong {
  background: var(--hud-bg-strong);
}
</style>
