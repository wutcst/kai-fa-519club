<script setup lang="ts">
import { computed } from 'vue'
import { INVENTORY_SLOT_COUNT } from '@/model/theme'
import ItemIcon from '@/component/game/ItemIcon.vue'

const props = defineProps<{
  items: string[]
}>()

const slots = computed(() => {
  const filled = props.items.slice(0, INVENTORY_SLOT_COUNT)
  const emptyCount = Math.max(0, INVENTORY_SLOT_COUNT - filled.length)
  return [...filled, ...Array(emptyCount).fill('')]
})
</script>

<template>
  <div class="inventory-hud glass-hud">
    <div class="inventory-label">背包</div>
    <div class="slot-row">
      <div
        v-for="(item, index) in slots"
        :key="`${index}-${item}`"
        class="slot"
        :class="{ filled: !!item }"
        :title="item || '空'"
      >
        <ItemIcon v-if="item" :name="item" :size="40" />
        <span v-else class="slot-empty">—</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inventory-hud {
  position: absolute;
  left: 16px;
  bottom: 96px;
  z-index: 20;
  padding: 10px 14px;
  min-width: 320px;
}

.inventory-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.slot-row {
  display: flex;
  gap: 8px;
}

.slot {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  display: grid;
  place-items: center;
  transition: border-color 0.2s ease, transform 0.15s ease;
}

.slot.filled {
  border-color: rgba(136, 198, 255, 0.35);
  background: rgba(255, 255, 255, 0.08);
}

.slot.filled:hover {
  transform: translateY(-2px);
  border-color: rgba(136, 198, 255, 0.6);
}

.slot-icon {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.slot-empty {
  color: rgba(255, 255, 255, 0.25);
  font-size: 1.1rem;
}

.glass-hud {
  background: var(--hud-bg);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(12px);
}
</style>
