import { computed, ref } from 'vue'

/** 场景表现：经典平面 / 沉浸定向转场 */
export type GameDisplayMode = 'classic' | 'immersive'

/** 移动方向（含原路返回） */
export type MoveDirection = 'north' | 'south' | 'east' | 'west' | 'back'

const STORAGE_KEY = '519club.displayMode'
const DEFAULT_MODE: GameDisplayMode = 'classic'

function readStoredMode(): GameDisplayMode {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'classic' || stored === 'immersive') {
      return stored
    }
  } catch {
    // ignore
  }
  return DEFAULT_MODE
}

function persistMode(mode: GameDisplayMode) {
  try {
    localStorage.setItem(STORAGE_KEY, mode)
  } catch {
    // ignore
  }
}

const displayModeRef = ref<GameDisplayMode>(readStoredMode())

/**
 * 全局场景显示模式（localStorage 持久化）。
 */
export function useGameDisplayMode() {
  const displayMode = displayModeRef

  const isImmersive = computed(() => displayMode.value === 'immersive')

  function setDisplayMode(mode: GameDisplayMode) {
    displayMode.value = mode
    persistMode(mode)
  }

  return {
    displayMode,
    isImmersive,
    setDisplayMode,
  }
}
