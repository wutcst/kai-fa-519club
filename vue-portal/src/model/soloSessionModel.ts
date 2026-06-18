import { ref } from 'vue'
import type { SoloSessionRef, SoloViewState } from '@/model/soloTypes'

const STORAGE_KEY = 'zuul-solo-session'

const soloSession = ref<SoloSessionRef | null>(loadSession())
const soloViewState = ref<SoloViewState | null>(null)

function loadSession(): SoloSessionRef | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as SoloSessionRef) : null
  } catch {
    return null
  }
}

export function saveSoloSession(value: SoloSessionRef) {
  soloSession.value = value
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value))
}

export function clearSoloSession() {
  soloSession.value = null
  soloViewState.value = null
  sessionStorage.removeItem(STORAGE_KEY)
}

export function updateSoloViewState(state: SoloViewState | null) {
  soloViewState.value = state
}

export function useSoloSessionModel() {
  return {
    soloSession,
    soloViewState,
    saveSoloSession,
    clearSoloSession,
    updateSoloViewState,
  }
}
