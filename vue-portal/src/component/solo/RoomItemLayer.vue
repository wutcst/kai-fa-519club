<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ItemView } from '@/model/soloTypes'
import { getItemAnchor } from '@/model/roomLayoutDefaults'
import ItemIcon from '@/component/game/ItemIcon.vue'
import ItemInspectDialog from '@/component/solo/ItemInspectDialog.vue'

const props = defineProps<{
  items: ItemView[]
  roomId: string
  level?: number
  disabled?: boolean
}>()

const emit = defineEmits<{
  take: [name: string]
  'pickup-visual': [payload: { name: string; x: number; y: number }]
  'focus-change': [payload: { active: boolean; x: number; y: number }]
}>()

const focusedItem = ref<ItemView | null>(null)
const focusAnchor = ref<[number, number] | null>(null)
const inspectOpen = ref(false)

/** 本房间/关卡内物品首次出现顺序，拾取后仍保留槽位，避免其余物品顺移 */
const itemSlotOrder = ref<string[]>([])

watch(
  () => [props.roomId, props.level ?? 0] as const,
  () => {
    itemSlotOrder.value = []
  },
)

watch(
  () => props.items,
  (items) => {
    const order = [...itemSlotOrder.value]
    let changed = false
    for (const item of items) {
      if (!order.includes(item.name)) {
        order.push(item.name)
        changed = true
      }
    }
    if (changed) {
      itemSlotOrder.value = order
    }
  },
  { immediate: true, deep: true },
)

const positionedItems = computed(() => {
  const totalSlots = Math.max(itemSlotOrder.value.length, props.items.length, 1)
  return props.items.map((item) => {
    const slotIndex = Math.max(0, itemSlotOrder.value.indexOf(item.name))
    const anchor = getItemAnchor(props.roomId, item.name, slotIndex, totalSlots, props.level)
    return {
      item,
      x: anchor.x,
      y: anchor.y,
      placement: anchor.placement,
      index: slotIndex,
    }
  })
})

const focusStyle = computed(() => {
  if (!focusAnchor.value) {
    return undefined
  }
  const [x, y] = focusAnchor.value
  return {
    left: `${x * 100}%`,
    top: `${y * 100}%`,
  }
})

watch(
  () => props.items,
  (items) => {
    if (focusedItem.value && !items.some((item) => item.name === focusedItem.value?.name)) {
      closeFocus()
    }
  },
)

watch(
  () => props.disabled,
  (disabled) => {
    if (disabled) {
      closeFocus()
    }
  },
)

function openFocus(item: ItemView, x: number, y: number) {
  if (props.disabled) {
    return
  }
  focusedItem.value = item
  focusAnchor.value = [x, y]
  inspectOpen.value = false
  emit('focus-change', { active: true, x, y })
}

function closeFocus() {
  focusedItem.value = null
  focusAnchor.value = null
  inspectOpen.value = false
  emit('focus-change', { active: false, x: 0.5, y: 0.5 })
}

function pickUp() {
  if (!focusedItem.value || !focusAnchor.value) {
    return
  }
  const [x, y] = focusAnchor.value
  emit('pickup-visual', { name: focusedItem.value.name, x, y })
  emit('take', focusedItem.value.name)
  closeFocus()
}

function openInspect() {
  inspectOpen.value = true
}

function closeInspect() {
  inspectOpen.value = false
}
</script>

<template>
  <div class="room-items">
    <button
      v-for="{ item, x, y, placement, index } in positionedItems"
      :key="item.name"
      type="button"
      class="room-item-chip"
      :class="[`surface-${placement}`, { hidden: focusedItem?.name === item.name }]"
      :style="{
        left: `${x * 100}%`,
        top: `${y * 100}%`,
        animationDelay: `${index * 0.35}s`,
      }"
      :aria-label="item.name"
      @click="openFocus(item, x, y)"
    >
      <ItemIcon :name="item.name" :size="22" />
    </button>

    <Transition name="focus-fade">
      <div v-if="focusedItem && focusAnchor" class="item-focus-layer">
        <div
          class="focus-vignette"
          :style="{
            '--spot-x': `${focusAnchor[0] * 100}%`,
            '--spot-y': `${focusAnchor[1] * 100}%`,
          }"
          @click="closeFocus"
        />

        <div class="focus-spotlight" :style="focusStyle">
          <div class="spotlight-glow" />
          <div class="focused-item">
            <ItemIcon :name="focusedItem.name" :size="54" />
          </div>

          <div class="focus-actions">
            <button type="button" class="focus-btn primary" @click="pickUp">拾取</button>
            <button type="button" class="focus-btn" @click="openInspect">查看</button>
          </div>
        </div>
      </div>
    </Transition>

    <ItemInspectDialog
      v-if="inspectOpen && focusedItem"
      :item="focusedItem"
      @close="closeInspect"
    />
  </div>
</template>

<style scoped>
.room-items {
  position: absolute;
  inset: 0;
  z-index: 12;
  pointer-events: none;
}

.room-item-chip {
  position: absolute;
  pointer-events: auto;
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 10px;
  border: 1px solid rgba(136, 198, 255, 0.5);
  background: rgba(8, 12, 22, 0.55);
  cursor: pointer;
  display: grid;
  place-items: center;
  backdrop-filter: blur(4px);
  box-shadow:
    0 0 8px rgba(88, 166, 255, 0.4),
    0 0 16px rgba(88, 166, 255, 0.18),
    inset 0 0 8px rgba(136, 198, 255, 0.1);
  transform: translate(-50%, -100%);
  animation: itemGlowFloat 3.4s ease-in-out infinite;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.room-item-chip.surface-shelf,
.room-item-chip.surface-wall {
  transform: translate(-50%, -58%);
}

.room-item-chip.hidden {
  opacity: 0;
  pointer-events: none;
}

.room-item-chip:hover {
  border-color: rgba(160, 210, 255, 0.85);
  box-shadow:
    0 0 12px rgba(88, 166, 255, 0.6),
    0 0 24px rgba(88, 166, 255, 0.3),
    inset 0 0 12px rgba(136, 198, 255, 0.18);
  transform: translate(-50%, calc(-100% - 3px)) scale(1.08);
}

.room-item-chip.surface-shelf:hover,
.room-item-chip.surface-wall:hover {
  transform: translate(-50%, calc(-58% - 3px)) scale(1.08);
}

.item-focus-layer {
  position: absolute;
  inset: 0;
  z-index: 30;
  pointer-events: auto;
}

.focus-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    circle at var(--spot-x, 50%) var(--spot-y, 70%),
    rgba(8, 10, 16, 0) 0%,
    rgba(8, 10, 16, 0.35) 38%,
    rgba(4, 6, 12, 0.78) 100%
  );
}

.focus-spotlight {
  position: absolute;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  z-index: 2;
}

.spotlight-glow {
  position: absolute;
  left: 50%;
  top: 42%;
  width: 160px;
  height: 160px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(88, 166, 255, 0.28) 0%, rgba(88, 166, 255, 0) 70%);
  pointer-events: none;
  animation: pulseGlow 2s ease-in-out infinite;
}

.focused-item {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 16px;
  border: 1px solid rgba(160, 210, 255, 0.75);
  background: rgba(10, 14, 24, 0.82);
  display: grid;
  place-items: center;
  box-shadow:
    0 0 18px rgba(88, 166, 255, 0.55),
    0 0 36px rgba(88, 166, 255, 0.28);
  animation: focusPop 0.32s ease;
}

.focus-actions {
  display: flex;
  gap: 10px;
}

.focus-btn {
  min-width: 68px;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: rgba(15, 18, 28, 0.82);
  color: var(--text-primary);
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
  backdrop-filter: blur(8px);
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.focus-btn:hover {
  background: rgba(88, 166, 255, 0.2);
  border-color: rgba(88, 166, 255, 0.45);
}

.focus-btn.primary {
  background: rgba(88, 166, 255, 0.24);
  border-color: rgba(88, 166, 255, 0.5);
}

.focus-btn:active {
  transform: scale(0.97);
}

.focus-fade-enter-active,
.focus-fade-leave-active {
  transition: opacity 0.28s ease;
}

.focus-fade-enter-from,
.focus-fade-leave-to {
  opacity: 0;
}

@keyframes itemGlowFloat {
  0%,
  100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.08);
  }
}

@keyframes focusPop {
  from {
    opacity: 0;
    transform: scale(0.82);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes pulseGlow {
  0%,
  100% {
    opacity: 0.75;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.08);
  }
}
</style>
