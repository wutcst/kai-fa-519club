<script setup lang="ts">
import GlassPanel from '@/component/common/GlassPanel.vue'

defineProps<{
  title: string
  message: string
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <div class="npc-dialog-backdrop" @click.self="emit('close')">
    <GlassPanel strong padding="22px 24px" class="npc-dialog-panel">
      <button type="button" class="close-btn" aria-label="关闭" @click="emit('close')">×</button>
      <h3 class="dialog-title">{{ title }}</h3>
      <p class="dialog-body">{{ message }}</p>
    </GlassPanel>
  </div>
</template>

<style scoped>
.npc-dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: 55;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(4, 6, 12, 0.65);
  backdrop-filter: blur(3px);
}

.npc-dialog-panel {
  position: relative;
  width: min(460px, 92vw);
  max-height: min(65vh, 480px);
  overflow: auto;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-muted);
  font-size: 1.25rem;
  line-height: 1;
  cursor: pointer;
}

.close-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.14);
}

.dialog-title {
  margin: 0 0 12px;
  padding-right: 28px;
  font-size: 1.05rem;
  color: var(--accent);
}

.dialog-body {
  margin: 0;
  color: var(--text-primary);
  line-height: 1.65;
  font-size: 0.92rem;
  white-space: pre-wrap;
}
</style>
