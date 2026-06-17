<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ItemView } from '@/model/soloTypes'
import { INVENTORY_SLOT_COUNT } from '@/model/theme'
import ItemIcon from '@/component/game/ItemIcon.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const props = defineProps<{
  items: ItemView[]
  inventoryWeight?: number
  maxInventoryWeight?: number
  remainingCapacity?: number
  disabled?: boolean
}>()

const emit = defineEmits<{
  action: [type: 'drop' | 'use' | 'eat' | 'inspect', item: ItemView]
}>()

const expanded = ref(false)
const menuItem = ref<ItemView | null>(null)
const menuX = ref(0)
const menuY = ref(0)

const itemCount = computed(() => props.items.length)

const weightLabel = computed(() => {
  const max = props.maxInventoryWeight ?? 3000
  const remaining = props.remainingCapacity ?? Math.max(0, max - summedWeight.value)
  return `剩余 ${remaining}g`
})

const summedWeight = computed(() =>
  props.items.reduce((sum, item) => sum + item.weight, 0),
)

const weightRatio = computed(() => {
  const max = props.maxInventoryWeight ?? 3000
  const current = props.inventoryWeight ?? summedWeight.value
  return Math.min(1, current / Math.max(1, max))
})

const weightWarning = computed(() => weightRatio.value >= 0.85)

function slots() {
  const filled = props.items.slice(0, INVENTORY_SLOT_COUNT)
  const empties = Array(Math.max(0, INVENTORY_SLOT_COUNT - filled.length)).fill(null)
  return [...filled, ...empties]
}

function togglePanel() {
  expanded.value = !expanded.value
  if (!expanded.value) {
    closeMenu()
  }
}

function openMenu(item: ItemView, event: MouseEvent) {
  if (props.disabled) {
    return
  }
  menuItem.value = item
  menuX.value = event.clientX
  menuY.value = event.clientY
}

function closeMenu() {
  menuItem.value = null
}

function choose(type: 'drop' | 'use' | 'eat' | 'inspect') {
  if (menuItem.value) {
    emit('action', type, menuItem.value)
  }
  closeMenu()
}
</script>

<template>
  <div class="inventory-root">
    <div class="backpack-cluster">
      <span class="weight-tag" :class="{ warn: weightWarning }">{{ weightLabel }}</span>
      <button
      type="button"
      class="backpack-toggle"
      :class="{ active: expanded }"
      :aria-expanded="expanded"
      aria-label="打开背包"
      @click="togglePanel"
    >
      <svg class="backpack-icon" viewBox="0 0 48 48" aria-hidden="true">
        <path
          d="M16 18V14a8 8 0 0 1 16 0v4"
          fill="none"
          stroke="currentColor"
          stroke-width="2.5"
          stroke-linecap="round"
        />
        <rect
          x="10"
          y="18"
          width="28"
          height="22"
          rx="6"
          fill="rgba(88,166,255,0.15)"
          stroke="currentColor"
          stroke-width="2.5"
        />
        <path
          d="M10 26h28"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          opacity="0.55"
        />
        <rect x="21" y="14" width="6" height="6" rx="2" fill="currentColor" opacity="0.8" />
      </svg>
      <span v-if="itemCount > 0" class="badge">{{ itemCount }}</span>
    </button>
    </div>

    <Transition name="panel-slide">
      <div v-if="expanded" class="inventory-panel glass-hud" @click.self="closeMenu">
        <div class="panel-header">
          <span class="label">背包</span>
          <button type="button" class="close-btn" aria-label="收起背包" @click="expanded = false">×</button>
        </div>
        <div class="slot-row">
          <button
            v-for="(item, index) in slots()"
            :key="index"
            type="button"
            class="slot"
            :class="{ filled: !!item }"
            :disabled="!item || disabled"
            @click="item && openMenu(item, $event)"
          >
            <ItemIcon v-if="item" :name="item.name" :size="40" />
            <span v-else class="slot-empty">—</span>
          </button>
        </div>
      </div>
    </Transition>

    <div
      v-if="menuItem"
      class="item-menu glass-hud"
      :style="{ left: `${menuX}px`, top: `${menuY}px` }"
    >
      <GlassButton @click="choose('drop')">丢弃</GlassButton>
      <GlassButton v-if="menuItem.edible" accent @click="choose('eat')">吃</GlassButton>
      <GlassButton accent @click="choose('use')">使用</GlassButton>
      <GlassButton @click="choose('inspect')">查看</GlassButton>
    </div>
  </div>
</template>

<style scoped>
.inventory-root {
  position: absolute;
  left: 16px;
  bottom: 20px;
  z-index: 24;
}

.backpack-cluster {
  display: flex;
  align-items: center;
  gap: 10px;
}

.weight-tag {
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid rgba(136, 198, 255, 0.28);
  background: rgba(12, 16, 28, 0.72);
  backdrop-filter: blur(8px);
  color: #b8d8ff;
  font-size: 0.72rem;
  font-weight: 600;
  white-space: nowrap;
}

.weight-tag.warn {
  border-color: rgba(255, 160, 80, 0.45);
  color: #ffd4a8;
}

.backpack-toggle {
  position: relative;
  width: 58px;
  height: 58px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(15, 18, 28, 0.72);
  backdrop-filter: blur(10px);
  color: var(--accent);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.backpack-toggle:hover {
  background: rgba(88, 166, 255, 0.18);
  border-color: rgba(88, 166, 255, 0.45);
}

.backpack-toggle.active {
  border-color: rgba(88, 166, 255, 0.55);
  background: rgba(88, 166, 255, 0.22);
}

.backpack-toggle:active {
  transform: scale(0.96);
}

.backpack-icon {
  width: 34px;
  height: 34px;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--danger);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.inventory-panel {
  position: absolute;
  left: 0;
  bottom: 68px;
  padding: 12px 14px;
  min-width: 380px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.label {
  font-size: 0.82rem;
  color: var(--text-primary);
  font-weight: 600;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
}

.close-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.14);
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
  cursor: pointer;
}

.slot.filled {
  border-color: rgba(136, 198, 255, 0.35);
}

.slot-empty {
  color: rgba(255, 255, 255, 0.25);
}

.item-menu {
  position: fixed;
  transform: translate(-50%, -100%);
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 8px;
  z-index: 40;
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.22s ease;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(12px);
}
</style>
