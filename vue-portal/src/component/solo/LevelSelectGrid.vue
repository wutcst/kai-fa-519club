<script setup lang="ts">
import type { SoloLevelOption } from '@/model/soloTypes'

defineProps<{
  levels: SoloLevelOption[]
  selectedLevel: number
  comingSoonLabel: string
  layout?: 'grid' | 'row'
}>()

const emit = defineEmits<{
  select: [level: number]
  locked: [level: SoloLevelOption]
  comingSoon: []
}>()

function shortTitle(title: string): string {
  const colon = title.indexOf('：')
  return colon >= 0 ? title.slice(colon + 1) : title
}

function onLevelClick(level: SoloLevelOption) {
  if (!level.unlocked) {
    emit('locked', level)
    return
  }
  emit('select', level.levelNumber)
}
</script>

<template>
  <div class="level-select">
    <p class="level-select-hint">选择关卡 · 通关前一关后解锁下一关</p>
    <div class="level-grid" :class="{ 'level-grid-row': layout === 'row' }">
      <button
        v-for="level in levels"
        :key="level.levelNumber"
        type="button"
        class="level-card"
        :class="{
          selected: selectedLevel === level.levelNumber,
          locked: !level.unlocked,
          cleared: level.cleared,
        }"
        @click="onLevelClick(level)"
      >
        <span class="level-badge">L{{ level.levelNumber }}</span>
        <span class="level-name">{{ shortTitle(level.title) }}</span>
        <span v-if="!level.unlocked" class="level-lock" aria-hidden="true">🔒</span>
        <span v-else-if="level.cleared" class="level-done">已通关</span>
      </button>

      <button
        type="button"
        class="level-card level-more"
        :aria-label="comingSoonLabel"
        @click="emit('comingSoon')"
      >
        <span class="level-ellipsis">{{ comingSoonLabel }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.level-select {
  width: 100%;
  margin-top: 4px;
}

.level-select-hint {
  margin: 0 0 10px;
  font-size: 0.82rem;
  color: rgba(220, 228, 245, 0.65);
}

.level-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.level-grid-row {
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}

@media (max-width: 720px) {
  .level-grid-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.level-grid-row .level-card {
  min-height: 96px;
  align-items: center;
  text-align: center;
}

.level-grid-row .level-name {
  font-size: 0.78rem;
}

.level-grid-row .level-lock {
  position: static;
  margin-top: 2px;
}

.level-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  min-height: 72px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(140, 170, 220, 0.22);
  background: rgba(12, 18, 32, 0.55);
  color: rgba(235, 240, 255, 0.92);
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.2s,
    background 0.2s,
    transform 0.15s;
}

.level-card:hover:not(.locked) {
  border-color: rgba(120, 190, 255, 0.45);
  transform: translateY(-1px);
}

.level-card.selected {
  border-color: rgba(100, 200, 255, 0.75);
  background: rgba(30, 60, 110, 0.55);
  box-shadow: 0 0 0 1px rgba(100, 200, 255, 0.25);
}

.level-card.locked {
  opacity: 0.45;
  cursor: not-allowed;
}

.level-badge {
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: rgba(130, 200, 255, 0.9);
}

.level-name {
  font-size: 0.82rem;
  line-height: 1.35;
}

.level-lock {
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 0.85rem;
}

.level-done {
  font-size: 0.68rem;
  color: rgba(140, 230, 180, 0.85);
}

.level-more {
  align-items: center;
  justify-content: center;
  border-style: dashed;
}

.level-ellipsis {
  font-size: 1.6rem;
  line-height: 1;
  letter-spacing: 0.2em;
  color: rgba(180, 200, 230, 0.7);
}
</style>
