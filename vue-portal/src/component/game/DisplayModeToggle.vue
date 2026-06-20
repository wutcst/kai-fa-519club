<script setup lang="ts">
import type { GameDisplayMode } from '@/model/gameDisplayMode'
import { useGameDisplayMode } from '@/model/gameDisplayMode'

withDefaults(
  defineProps<{
    compact?: boolean
  }>(),
  {
    compact: false,
  },
)

const { displayMode, setDisplayMode } = useGameDisplayMode()

const options: { value: GameDisplayMode; label: string; hint: string }[] = [
  { value: 'classic', label: '经典', hint: '平面淡入淡出' },
  { value: 'immersive', label: '沉浸', hint: '定向暗角转场' },
]

function select(mode: GameDisplayMode) {
  if (displayMode.value !== mode) {
    setDisplayMode(mode)
  }
}
</script>

<template>
  <div class="display-mode" :class="{ compact }" role="group" aria-label="场景显示模式">
    <span v-if="!compact" class="display-mode-label">场景模式</span>
    <div class="display-mode-options">
      <button
        v-for="option in options"
        :key="option.value"
        type="button"
        class="mode-option"
        :class="{ active: displayMode === option.value }"
        :title="option.hint"
        :aria-pressed="displayMode === option.value"
        @click="select(option.value)"
      >
        {{ option.label }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.display-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.display-mode.compact {
  flex-direction: row;
  align-items: center;
  gap: 6px;
}

.display-mode-label {
  font-size: 0.78rem;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

.display-mode-options {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(8, 10, 18, 0.55);
  backdrop-filter: blur(8px);
}

.mode-option {
  border: none;
  background: transparent;
  color: var(--text-muted);
  font: inherit;
  font-size: 0.78rem;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.compact .mode-option {
  padding: 5px 10px;
  font-size: 0.72rem;
}

.mode-option:hover {
  color: var(--text-primary);
}

.mode-option.active {
  color: #e8f2ff;
  background: rgba(88, 166, 255, 0.28);
  box-shadow: 0 0 12px rgba(88, 166, 255, 0.2);
}
</style>
