<script setup lang="ts">
import { computed } from 'vue'
import type { TimerPressureLevel } from '@/model/timerPressure'

const props = defineProps<{
  timerText: string
  level: number
  levelState: string
  roomName: string
  polling: boolean
  danger: boolean
  warning: boolean
  pressure?: TimerPressureLevel
}>()

const isCritical = computed(() => props.pressure === 'critical')
</script>

<template>
  <header
    class="hud-timer glass-hud"
    :class="{ danger, warning, critical: isCritical }"
  >    <div class="timer-main">{{ timerText || '计时同步中…' }}</div>
    <div class="timer-sub">
      <span>第 {{ level }} 关</span>
      <span class="dot">·</span>
      <span>{{ levelState }}</span>
      <span class="dot">·</span>
      <span :class="{ live: polling }">{{ polling ? '同步中' : '暂停' }}</span>
    </div>
    <div class="room-tag">{{ roomName }}</div>
  </header>
</template>

<style scoped>
.hud-timer {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 20;
  min-width: 240px;
  padding: 12px 16px;
  text-align: right;
}

.timer-main {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.hud-timer.warning .timer-main {
  color: #ffc460;
}

.hud-timer.danger .timer-main {
  color: var(--danger);
  animation: timerPulse 0.8s ease-in-out infinite alternate;
}

.hud-timer.critical {
  border-color: rgba(255, 80, 80, 0.55);
  box-shadow: 0 0 20px rgba(255, 60, 60, 0.25);
}

.hud-timer.critical .timer-main {
  animation: timerPulse 0.45s ease-in-out infinite alternate;
}

.timer-sub {
  margin-top: 4px;
  font-size: 0.78rem;
  color: var(--text-muted);
  display: flex;
  justify-content: flex-end;
  gap: 4px;
  flex-wrap: wrap;
}

.dot {
  opacity: 0.5;
}

.live {
  color: #9ecaff;
}

.room-tag {
  margin-top: 6px;
  font-size: 0.75rem;
  color: var(--accent);
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(12px);
}

@keyframes timerPulse {
  from {
    opacity: 1;
  }
  to {
    opacity: 0.55;
  }
}
</style>
