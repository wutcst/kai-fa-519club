<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { SoloLevelSelection } from '@/model/soloTypes'
import LevelSelectGrid from '@/component/solo/LevelSelectGrid.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const props = defineProps<{
  visible: boolean
  levelSelection: SoloLevelSelection | null
  loading: boolean
  error?: string
}>()

const emit = defineEmits<{
  close: []
  confirm: [level: number]
}>()

const selectedLevel = ref(1)
const notice = ref('')

const selectedInfo = computed(() =>
  props.levelSelection?.levels.find((level) => level.levelNumber === selectedLevel.value) ?? null,
)

watch(
  () => props.visible,
  (open) => {
    if (!open) {
      return
    }
    notice.value = ''
    const first = props.levelSelection?.levels.find((level) => level.unlocked)
    selectedLevel.value = first?.levelNumber ?? 1
  },
)

function onSelect(level: number) {
  notice.value = ''
  selectedLevel.value = level
}

function onLocked(level: { levelNumber: number }) {
  notice.value = `第 ${level.levelNumber} 关尚未解锁，请先通关上一关`
}

function onComingSoon() {
  notice.value = props.levelSelection?.comingSoonMessage ?? '关卡正在开发'
}

function confirm() {
  if (!selectedInfo.value?.unlocked) {
    notice.value = '请选择已解锁的关卡'
    return
  }
  emit('confirm', selectedLevel.value)
}
</script>

<template>
  <div v-if="visible" class="modal-backdrop" @click.self="emit('close')">
    <div class="modal-card" role="dialog">
      <header class="modal-head">
        <h3>选择联机关卡</h3>
        <p>房主选择关卡后全队进入游戏</p>
      </header>

      <LevelSelectGrid
        v-if="levelSelection"
        layout="row"
        :levels="levelSelection.levels"
        :selected-level="selectedLevel"
        :coming-soon-label="levelSelection.comingSoonLabel"
        @select="onSelect"
        @locked="onLocked"
        @coming-soon="onComingSoon"
      />

      <article v-if="selectedInfo" class="level-detail">
        <h4>{{ selectedInfo.title }}</h4>
        <p>{{ selectedInfo.missionHint }}</p>
      </article>

      <p v-if="notice" class="notice">{{ notice }}</p>
      <p v-if="error" class="notice error-line">{{ error }}</p>

      <div class="actions">
        <GlassButton accent :disabled="loading" @click="confirm">
          {{ loading ? '正在进入…' : `开始第 ${selectedLevel} 关` }}
        </GlassButton>
        <GlassButton :disabled="loading" @click="emit('close')">取消</GlassButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 120;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(4, 6, 12, 0.78);
  backdrop-filter: blur(6px);
}

.modal-card {
  width: min(720px, calc(100vw - 32px));
  max-height: calc(100vh - 48px);
  overflow-y: auto;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(255, 160, 80, 0.25);
  background: rgba(12, 16, 28, 0.96);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.5);
}

.modal-head h3 {
  margin: 0 0 6px;
  font-size: 1.2rem;
}

.modal-head p {
  margin: 0 0 18px;
  color: var(--text-muted);
  font-size: 0.88rem;
}

.level-detail {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 12px;
  background: rgba(255, 140, 60, 0.08);
  border: 1px solid rgba(255, 160, 80, 0.2);
}

.level-detail h4 {
  margin: 0 0 6px;
  font-size: 0.95rem;
}

.level-detail p {
  margin: 0;
  font-size: 0.86rem;
  line-height: 1.55;
  color: rgba(220, 228, 245, 0.85);
}

.notice {
  margin: 12px 0 0;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.84rem;
  color: #ffc9a0;
  background: rgba(255, 140, 60, 0.1);
}

.error-line {
  color: #ffb4b4;
  background: rgba(220, 90, 90, 0.12);
  border: 1px solid rgba(220, 90, 90, 0.25);
}

.actions {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>
