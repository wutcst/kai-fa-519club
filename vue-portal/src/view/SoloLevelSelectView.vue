<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { authSessionModel, isLoggedIn, mergeAuthSession } from '@/model/authModel'
import {
  loadSoloPendingSetup,
  saveSoloSession,
  updateSoloViewState,
} from '@/model/soloSessionModel'
import type { SoloLevelOption, SoloLevelSelection } from '@/model/soloTypes'
import * as soloService from '@/service/soloService'
import * as authService from '@/service/authService'
import { unlockGameBgm } from '@/service/gameBgm'
import HomeAmbientBackground from '@/component/home/HomeAmbientBackground.vue'
import LevelSelectGrid from '@/component/solo/LevelSelectGrid.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const router = useRouter()

const playerName = ref('玩家')
const loading = ref(false)
const levelsLoading = ref(true)
const error = ref('')
const notice = ref('')
const selectedLevel = ref(1)
const levelSelection = ref<SoloLevelSelection | null>(null)

const selectedLevelInfo = computed(() =>
  levelSelection.value?.levels.find((level) => level.levelNumber === selectedLevel.value) ?? null,
)

onMounted(async () => {
  const pending = loadSoloPendingSetup()
  if (!pending?.playerName?.trim()) {
    router.replace('/')
    return
  }
  playerName.value = pending.playerName.trim()

  if (isLoggedIn()) {
    try {
      const profile = await authService.fetchProfile()
      mergeAuthSession({
        displayName: profile.displayName,
        email: profile.email,
        avatarUrl: profile.avatarUrl,
      })
      if (profile.displayName) {
        playerName.value = profile.displayName
      }
    } catch {
      // 忽略
    }
  }

  await loadLevels()
})

async function loadLevels() {
  levelsLoading.value = true
  error.value = ''
  notice.value = ''
  try {
    const result = await soloService.fetchSoloLevels()
    levelSelection.value = result.selection
    if (result.fromFallback) {
      notice.value =
        '关卡接口不可用：请在 IDEA 中重新运行 ServerApplication（8080）后刷新本页。当前仅可试玩第 1 关。'
    }
    const firstUnlocked = result.selection.levels.find((level) => level.unlocked)
    if (firstUnlocked) {
      selectedLevel.value = firstUnlocked.levelNumber
    }
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : '无法加载关卡列表'
    levelSelection.value = null
  } finally {
    levelsLoading.value = false
  }
}

function onLevelSelect(level: number) {
  notice.value = ''
  selectedLevel.value = level
}

function onLevelLocked(level: SoloLevelOption) {
  notice.value = `第 ${level.levelNumber} 关尚未解锁，请先通关上一关`
}

function onComingSoon() {
  notice.value = levelSelection.value?.comingSoonMessage ?? '关卡正在开发'
}

async function startGame() {
  unlockGameBgm()
  loading.value = true
  error.value = ''
  notice.value = ''
  try {
    const created = await soloService.createSoloSession(playerName.value, selectedLevel.value)
    saveSoloSession({
      sessionId: created.sessionId,
      playerName: playerName.value,
    })
    updateSoloViewState(created.state)
    router.push('/solo')
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : '无法开始游戏'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="level-select-view">
    <HomeAmbientBackground />
    <div class="level-select-vignette" />

    <div class="level-select-shell">
      <nav class="level-select-nav">
        <RouterLink to="/" class="back-link">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
          返回首页
        </RouterLink>
        <span v-if="authSessionModel" class="player-tag">
          {{ authSessionModel.displayName }}
        </span>
      </nav>

      <header class="level-select-hero">
        <p class="hero-eyebrow">SOLO CAMPAIGN</p>
        <h1>选择关卡</h1>
        <p class="hero-sub">通关前一关后解锁下一关 · 玩家：{{ playerName }}</p>
      </header>

      <section class="level-select-panel">
        <p v-if="levelsLoading" class="panel-status">正在加载关卡…</p>

        <template v-else-if="levelSelection">
          <LevelSelectGrid
            layout="row"
            :levels="levelSelection.levels"
            :selected-level="selectedLevel"
            :coming-soon-label="levelSelection.comingSoonLabel"
            @select="onLevelSelect"
            @locked="onLevelLocked"
            @coming-soon="onComingSoon"
          />

          <article v-if="selectedLevelInfo" class="level-detail">
            <h2>{{ selectedLevelInfo.title }}</h2>
            <p>{{ selectedLevelInfo.missionHint }}</p>
            <span v-if="selectedLevelInfo.cleared" class="detail-badge cleared">已通关</span>
            <span v-else-if="!selectedLevelInfo.unlocked" class="detail-badge locked">未解锁</span>
          </article>

          <p v-if="notice" class="panel-notice">{{ notice }}</p>
          <p v-if="error" class="panel-error">{{ error }}</p>

          <div class="panel-actions">
            <GlassButton accent :disabled="loading || !selectedLevelInfo?.unlocked" @click="startGame">
              {{ loading ? '正在进入…' : `开始第 ${selectedLevel} 关` }}
            </GlassButton>
          </div>
        </template>

        <p v-else class="panel-error">{{ error || '关卡列表加载失败' }}</p>
      </section>
    </div>
  </div>
</template>

<style scoped>
.level-select-view {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.level-select-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, transparent 20%, rgba(6, 8, 14, 0.78) 100%),
    linear-gradient(to bottom, rgba(6, 8, 14, 0.4), transparent 35%, rgba(6, 8, 14, 0.55));
  z-index: 1;
}

.level-select-shell {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 24px 40px;
  gap: 28px;
}

.level-select-nav {
  width: min(920px, 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: rgba(200, 220, 245, 0.85);
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
}

.back-link svg {
  width: 20px;
  height: 20px;
}

.back-link:hover {
  color: #9ecaff;
}

.player-tag {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.8rem;
  color: #c8d8f0;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
}

.level-select-hero {
  text-align: center;
  max-width: 640px;
}

.hero-eyebrow {
  margin: 0;
  font-size: 0.72rem;
  letter-spacing: 0.22em;
  color: rgba(130, 200, 255, 0.75);
}

.level-select-hero h1 {
  margin: 10px 0 8px;
  font-size: clamp(2rem, 5vw, 2.8rem);
  font-weight: 700;
  color: #eef4ff;
}

.hero-sub {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.95rem;
}

.level-select-panel {
  width: min(920px, 100%);
  padding: 28px 24px 24px;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(12, 16, 28, 0.78);
  backdrop-filter: blur(16px);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
}

.panel-status {
  margin: 0;
  text-align: center;
  color: var(--text-muted);
}

.level-detail {
  margin-top: 22px;
  padding: 16px 18px;
  border-radius: 14px;
  border: 1px solid rgba(120, 180, 255, 0.2);
  background: rgba(20, 36, 64, 0.45);
}

.level-detail h2 {
  margin: 0 0 8px;
  font-size: 1.05rem;
  color: #e8f0ff;
}

.level-detail p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: rgba(210, 220, 240, 0.82);
}

.detail-badge {
  display: inline-block;
  margin-top: 10px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.72rem;
}

.detail-badge.cleared {
  color: rgba(140, 230, 180, 0.9);
  background: rgba(60, 140, 90, 0.2);
}

.detail-badge.locked {
  color: rgba(255, 190, 140, 0.9);
  background: rgba(180, 90, 40, 0.2);
}

.panel-notice,
.panel-error {
  margin: 16px 0 0;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 0.86rem;
}

.panel-notice {
  color: #ffc9a0;
  background: rgba(255, 140, 60, 0.1);
  border: 1px solid rgba(255, 160, 80, 0.22);
}

.panel-error {
  color: #ffb0b0;
  background: rgba(200, 60, 60, 0.12);
  border: 1px solid rgba(255, 100, 100, 0.25);
}

.panel-actions {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

.panel-actions :deep(button) {
  min-width: 220px;
}
</style>
