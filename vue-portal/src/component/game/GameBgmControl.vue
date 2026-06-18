<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { isGameBgmMuted, isGameSfxMuted, toggleGameSfxMute } from '@/service/gameAudio'
import {
  shouldPlayBgmForRoute,
  startGameBgm,
  stopGameBgm,
  toggleGameBgmMute,
} from '@/service/gameBgm'

const route = useRoute()
const bgmMuted = ref(isGameBgmMuted())
const sfxMuted = ref(isGameSfxMuted())
const visible = ref(false)
const stackedAboveChat = computed(() => route.name === 'multiplayer-room')

watch(
  () => route.name,
  (name) => {
    visible.value = shouldPlayBgmForRoute(name)
    if (shouldPlayBgmForRoute(name)) {
      startGameBgm()
    } else {
      stopGameBgm()
    }
  },
  { immediate: true },
)

function onToggleBgm() {
  bgmMuted.value = toggleGameBgmMute()
}

function onToggleSfx() {
  sfxMuted.value = toggleGameSfxMute()
}
</script>

<template>
  <div v-if="visible" class="audio-controls" :class="{ 'above-chat': stackedAboveChat }">
    <button
      type="button"
      class="audio-toggle"
      :aria-label="sfxMuted ? '开启音效' : '关闭音效'"
      @click="onToggleSfx"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path
          v-if="!sfxMuted"
          d="M4 10v4h4l5 4V6L8 10H4zm10.5 2a4.5 4.5 0 00-2.5-4.03v8.06A4.48 4.48 0 0014.5 12z"
          fill="currentColor"
        />
        <path
          v-else
          d="M4 10v4h4l5 4V6L8 10H4zm11.3 1.5l2.1 2.1 1.4-1.4-2.1-2.1 2.1-2.1-1.4-1.4-2.1 2.1-2.1-2.1-1.4 1.4 2.1 2.1-2.1 2.1 1.4 1.4 2.1-2.1z"
          fill="currentColor"
        />
      </svg>
      <span>{{ sfxMuted ? '音效关' : '音效开' }}</span>
    </button>

    <button
      type="button"
      class="audio-toggle"
      :aria-label="bgmMuted ? '开启音乐' : '关闭音乐'"
      @click="onToggleBgm"
    >
      <svg v-if="!bgmMuted" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M11 5L6 9H3v6h3l5 4V5z" fill="currentColor" />
        <path d="M15.5 8.5a5 5 0 010 7M18 6a8 8 0 010 12" fill="none" stroke="currentColor" stroke-width="1.8" />
      </svg>
      <svg v-else viewBox="0 0 24 24" aria-hidden="true">
        <path d="M11 5L6 9H3v6h3l5 4V5z" fill="currentColor" />
        <path d="M16 9l5 6M21 9l-5 6" stroke="currentColor" stroke-width="1.8" />
      </svg>
      <span>{{ bgmMuted ? '音乐关' : '音乐开' }}</span>
    </button>
  </div>
</template>

<style scoped>
.audio-controls {
  position: fixed;
  right: 16px;
  bottom: 16px;
  z-index: 80;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.audio-controls.above-chat {
  bottom: 86px;
}

.audio-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(12, 16, 28, 0.82);
  backdrop-filter: blur(10px);
  color: var(--text-primary);
  font: inherit;
  font-size: 0.78rem;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.audio-toggle svg {
  width: 18px;
  height: 18px;
}

.audio-toggle:hover {
  border-color: rgba(88, 166, 255, 0.45);
  background: rgba(88, 166, 255, 0.15);
}
</style>
