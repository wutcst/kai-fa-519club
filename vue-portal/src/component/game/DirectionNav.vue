<script setup lang="ts">
import type { ExitAvailability, UiActionFlags } from '@/model/soloTypes'

const props = withDefaults(
  defineProps<{
    disabled?: boolean
    showLookButton?: boolean
    exits?: ExitAvailability | null
    actions?: UiActionFlags | null
    /** 单机底部 HUD 更高，需抬高方向键位置 */
    variant?: 'solo' | 'multiplayer'
    /** 点击不可通行方向时触发 blocked（联机未开放提示） */
    hintOnBlockedExit?: boolean
  }>(),
  {
    showLookButton: true,
    variant: 'multiplayer',
    actions: null,
    hintOnBlockedExit: false,
  },
)

const emit = defineEmits<{
  move: [direction: string]
  look: []
  back: []
  blocked: []
  feed: []
  combine: []
  submit: []
  unlock: []
  sleep: []
}>()

const directions = [
  { key: 'north' as const, label: '北' },
  { key: 'west' as const, label: '西' },
  { key: 'east' as const, label: '东' },
  { key: 'south' as const, label: '南' },
]

function isEnabled(key: keyof ExitAvailability) {
  if (props.disabled) {
    return false
  }
  if (!props.exits) {
    return true
  }
  return props.exits[key]
}

function onDirectionClick(key: keyof ExitAvailability) {
  if (props.disabled) {
    return
  }
  if (!isEnabled(key)) {
    if (props.hintOnBlockedExit) {
      emit('blocked')
    }
    return
  }
  emit('move', key)
}

function onBackClick() {
  if (props.disabled) {
    return
  }
  if (!isEnabled('back')) {
    if (props.hintOnBlockedExit) {
      emit('blocked')
    }
    return
  }
  emit('back')
}
</script>

<template>
  <nav class="direction-nav" :class="variant" aria-label="方向导航">
    <button
      v-for="dir in directions"
      :key="dir.key"
      type="button"
      class="dir-btn edge"
      :class="[dir.key, { unavailable: !isEnabled(dir.key) }]"
      :disabled="disabled"
      @click="onDirectionClick(dir.key)"
    >
      <span class="arrow" :class="dir.key" />
      <span class="dir-label">{{ dir.label }}</span>
    </button>

    <div class="action-dock">
      <button
        v-if="showLookButton"
        type="button"
        class="action-btn"
        :disabled="disabled"
        @click="emit('look')"
      >
        环顾
      </button>
      <button
        type="button"
        class="action-btn"
        :class="{ unavailable: !isEnabled('back') }"
        :disabled="disabled"
        @click="onBackClick"
      >
        返回
      </button>
      <template v-if="variant === 'solo' && actions">
        <button
          v-if="actions.showSubmit"
          type="button"
          class="action-btn accent"
          :disabled="disabled"
          @click="emit('submit')"
        >
          提交归寝单
        </button>
        <button
          v-if="actions.showCombine"
          type="button"
          class="action-btn accent"
          :disabled="disabled"
          @click="emit('combine')"
        >
          合成锤子
        </button>
        <button
          v-if="actions.showUnlock"
          type="button"
          class="action-btn accent"
          :disabled="disabled"
          @click="emit('unlock')"
        >
          解锁寝室
        </button>
        <button
          v-if="actions.showSleep"
          type="button"
          class="action-btn accent"
          :disabled="disabled"
          @click="emit('sleep')"
        >
          睡觉
        </button>
      </template>
    </div>
  </nav>
</template>

<style scoped>
.direction-nav {
  position: absolute;
  inset: 0;
  z-index: 15;
  pointer-events: none;
}

.direction-nav.multiplayer {
  --edge-south-bottom: 128px;
  --action-dock-bottom: 88px;
}

.direction-nav.solo {
  --edge-south-bottom: 88px;
  --action-dock-bottom: 20px;
}

.dir-btn.edge {
  position: absolute;
  pointer-events: auto;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(15, 18, 28, 0.55);
  backdrop-filter: blur(8px);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  transition: background 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;
}

.dir-btn.edge:hover:not(:disabled) {
  background: rgba(88, 166, 255, 0.22);
  border-color: rgba(88, 166, 255, 0.45);
  box-shadow: 0 0 0 1px rgba(88, 166, 255, 0.15);
}

.dir-btn.edge:active:not(:disabled) {
  transform: scale(0.94);
}

.dir-btn.edge:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.dir-btn.edge.unavailable:not(:disabled) {
  opacity: 0.45;
}

.action-btn.unavailable:not(:disabled) {
  opacity: 0.45;
}

.dir-btn.north {
  top: 78px;
  left: 50%;
  transform: translateX(-50%);
}

.direction-nav.solo .dir-btn.north {
  top: 12px;
}

.dir-btn.north:hover:not(:disabled) {
  transform: translateX(-50%) translateY(-2px);
}

.dir-btn.south {
  bottom: var(--edge-south-bottom);
  left: 50%;
  transform: translateX(-50%);
}

.dir-btn.south:hover:not(:disabled) {
  transform: translateX(-50%) translateY(2px);
}

.dir-btn.west {
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
}

.dir-btn.west:hover:not(:disabled) {
  transform: translateY(-50%) translateX(-2px);
}

.dir-btn.east {
  right: 18px;
  top: 50%;
  transform: translateY(-50%);
}

.dir-btn.east:hover:not(:disabled) {
  transform: translateY(-50%) translateX(2px);
}

.dir-label {
  font-size: 0.68rem;
  color: var(--text-muted);
}

.arrow {
  width: 0;
  height: 0;
  border-left: 7px solid transparent;
  border-right: 7px solid transparent;
  border-bottom: 10px solid var(--accent);
}

.arrow.south {
  transform: rotate(180deg);
}

.arrow.west {
  transform: rotate(-90deg);
}

.arrow.east {
  transform: rotate(90deg);
}

.action-dock {
  position: absolute;
  left: 50%;
  bottom: var(--action-dock-bottom);
  transform: translateX(-50%);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
  max-width: min(760px, calc(100vw - 120px));
  pointer-events: auto;
}

.action-btn {
  min-width: 72px;
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(15, 18, 28, 0.62);
  backdrop-filter: blur(8px);
  color: var(--text-primary);
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.action-btn:hover:not(:disabled) {
  background: rgba(88, 166, 255, 0.22);
  border-color: rgba(88, 166, 255, 0.45);
}

.action-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.action-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.action-btn.accent {
  background: rgba(88, 166, 255, 0.2);
  border-color: rgba(88, 166, 255, 0.42);
  color: #dbeaff;
}

.action-btn.accent:hover:not(:disabled) {
  background: rgba(88, 166, 255, 0.32);
  border-color: rgba(88, 166, 255, 0.55);
}
</style>
