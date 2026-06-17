<script setup lang="ts">
import { computed } from 'vue'
import { timerPressureLevel } from '@/model/timerPressure'

const props = defineProps<{
  remainingSeconds: number
}>()

const level = computed(() => timerPressureLevel(props.remainingSeconds))
</script>

<template>
  <div
    v-if="level !== 'none'"
    class="timer-pressure"
    :class="level"
    aria-hidden="true"
  />
</template>

<style scoped>
.timer-pressure {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 17;
  opacity: 1;
  transition: opacity 0.6s ease;
}

.timer-pressure.warning {
  background:
    radial-gradient(ellipse at center, transparent 42%, rgba(255, 140, 40, 0.22) 100%),
    linear-gradient(to bottom, rgba(255, 120, 30, 0.12), rgba(255, 90, 20, 0.08));
  animation: pressureBreathe 3.2s ease-in-out infinite;
}

.timer-pressure.danger {
  background:
    radial-gradient(ellipse at center, transparent 36%, rgba(255, 80, 40, 0.38) 100%),
    linear-gradient(to bottom, rgba(220, 60, 30, 0.2), rgba(180, 30, 20, 0.14));
  animation: pressureBreathe 2s ease-in-out infinite;
}

.timer-pressure.critical {
  background:
    radial-gradient(ellipse at center, transparent 28%, rgba(255, 40, 40, 0.55) 100%),
    linear-gradient(to bottom, rgba(200, 20, 20, 0.28), rgba(120, 10, 10, 0.22));
  animation: pressureCritical 0.9s ease-in-out infinite;
}

@keyframes pressureBreathe {
  0%,
  100% {
    opacity: 0.82;
  }
  50% {
    opacity: 1;
  }
}

@keyframes pressureCritical {
  0%,
  100% {
    opacity: 0.75;
    filter: brightness(1);
  }
  50% {
    opacity: 1;
    filter: brightness(1.08);
  }
}
</style>
