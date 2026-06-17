<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ItemIcon from '@/component/game/ItemIcon.vue'
import { playGameSfx } from '@/service/gameSfx'

export type PickupFlight = {
  id: number
  name: string
  fromX: number
  fromY: number
}

const props = defineProps<{
  flight: PickupFlight
}>()

const emit = defineEmits<{
  done: [id: number]
}>()

const visible = ref(false)

/** 与 InventoryHudSolo 背包按钮大致对齐 */
const TARGET_X = 52
const TARGET_Y_OFFSET = 48

onMounted(() => {
  playGameSfx('pickup')
  requestAnimationFrame(() => {
    visible.value = true
  })
  window.setTimeout(() => {
    emit('done', props.flight.id)
  }, 620)
})
</script>

<template>
  <div
    class="pickup-flight"
    :class="{ flying: visible }"
    :style="{
      '--from-x': `${flight.fromX * 100}%`,
      '--from-y': `${flight.fromY * 100}%`,
      '--to-x': `${TARGET_X}px`,
      '--to-y': `calc(100% - ${TARGET_Y_OFFSET}px)`,
    }"
  >
    <div class="pickup-flight-icon">
      <ItemIcon :name="flight.name" :size="32" />
    </div>
  </div>
</template>

<style scoped>
.pickup-flight {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 40;
}

.pickup-flight-icon {
  position: absolute;
  left: var(--from-x);
  top: var(--from-y);
  transform: translate(-50%, -50%) scale(1);
  width: 44px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(160, 210, 255, 0.75);
  background: rgba(10, 14, 24, 0.88);
  display: grid;
  place-items: center;
  box-shadow: 0 0 16px rgba(88, 166, 255, 0.55);
  opacity: 1;
  transition:
    left 0.58s cubic-bezier(0.22, 1, 0.32, 1),
    top 0.58s cubic-bezier(0.22, 1, 0.32, 1),
    transform 0.58s cubic-bezier(0.22, 1, 0.32, 1),
    opacity 0.2s ease 0.48s;
}

.pickup-flight.flying .pickup-flight-icon {
  left: var(--to-x);
  top: var(--to-y);
  transform: translate(-50%, -50%) scale(0.55);
  opacity: 0;
}
</style>
