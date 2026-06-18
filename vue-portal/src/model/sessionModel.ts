import { ref } from 'vue'
import type { GameState, PortalSession } from '@/model/types'
import { normalizeGameState } from '@/model/types'

const STORAGE_KEY = 'zuul-portal-session'

function loadSession(): PortalSession | null {
  try {
    const raw = sessionStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as PortalSession) : null
  } catch {
    return null
  }
}

/** 全局会话与对局状态（Model 层单例） */
export const sessionModel = ref<PortalSession | null>(loadSession())
export const gameStateModel = ref<GameState | null>(null)

export function saveSession(value: PortalSession): void {
  sessionModel.value = value
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value))
}

export function clearSession(): void {
  sessionModel.value = null
  gameStateModel.value = null
  sessionStorage.removeItem(STORAGE_KEY)
}

export function updateGameState(state: GameState | null): void {
  gameStateModel.value = normalizeGameState(state)
}
