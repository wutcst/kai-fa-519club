<script setup lang="ts">
import type { ItemView } from '@/model/soloTypes'
import ItemIcon from '@/component/game/ItemIcon.vue'
import GlassPanel from '@/component/common/GlassPanel.vue'

defineProps<{
  item: ItemView
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <div class="inspect-backdrop" @click.self="emit('close')">
    <GlassPanel strong padding="22px 24px" class="inspect-panel">
      <button type="button" class="close-btn" aria-label="关闭" @click="emit('close')">×</button>
      <div class="inspect-head">
        <ItemIcon :name="item.name" :size="52" />
        <h3>{{ item.name }}</h3>
      </div>
      <p class="inspect-body">{{ item.longDescription || '一件普通物品。' }}</p>
    </GlassPanel>
  </div>
</template>

<style scoped>
.inspect-backdrop {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(4, 6, 12, 0.72);
  backdrop-filter: blur(4px);
}

.inspect-panel {
  position: relative;
  width: min(420px, 92vw);
  max-height: min(70vh, 520px);
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

.inspect-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
  padding-right: 28px;
}

.inspect-head h3 {
  margin: 0;
  font-size: 1.05rem;
  color: var(--text-primary);
}

.inspect-body {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.65;
  font-size: 0.92rem;
  white-space: pre-wrap;
}
</style>
