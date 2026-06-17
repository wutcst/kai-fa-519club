/** 联机 API 与领域类型（Model 层） */

import type { ExitAvailability, ItemView, UiActionFlags } from '@/model/soloTypes'

export interface ApiResponse<T> {
  code: number
  data: T
  message: string
}

export interface RoomInfo {
  roomId: string
  roomName: string
  playerCount: number
  level: number
  remainingSeconds: number
  hostPlayerId: string
}

export interface PlayerState {
  playerId: string
  displayName: string
  roomId: string
  roomName: string
  inventory: string[]
}

export interface ChatMessage {
  id: number
  playerId: string
  displayName: string
  text: string
  timestampMs: number
}

export interface GameState {
  level: number
  levelState: string
  remainingSeconds: number
  timerText: string
  activePlayerId: string
  roomId: string
  roomDescription: string
  bulletin?: string
  westTrapBanner?: string | null
  roomItems: ItemView[]
  inventory: ItemView[]
  inventoryWeight: number
  maxInventoryWeight: number
  remainingCapacity: number
  exits: ExitAvailability
  actions: UiActionFlags
  players: PlayerState[]
  chatMessages: ChatMessage[]
}

export interface RoomSession {
  roomId: string
  roomName: string
  playerId: string
  displayName: string
  state: GameState | null
}

export interface CommandResponse {
  messages: string[]
  quitRequested: boolean
  noticeMessage?: string | null
  state: GameState | null
}

export interface PortalSession {
  roomId: string
  roomName: string
  playerId: string
  displayName: string
}

export interface CommandRequest {
  roomId: string
  playerId: string
  commandWord: string
  secondWord?: string
}

export interface SendChatRequest {
  playerId: string
  text: string
}

export const EMPTY_EXITS: ExitAvailability = {
  north: false,
  south: false,
  east: false,
  west: false,
  back: false,
}

export const EMPTY_ACTIONS: UiActionFlags = {
  showNpc: false,
  showFeed: false,
  showCombine: false,
  showUnlock: false,
  showSleep: false,
  showSubmit: false,
}

export function normalizeGameState(state: GameState | null | undefined): GameState | null {
  if (!state) {
    return null
  }
  return {
    ...state,
    roomItems: state.roomItems ?? [],
    inventory: state.inventory ?? [],
    inventoryWeight: state.inventoryWeight ?? 0,
    maxInventoryWeight: state.maxInventoryWeight ?? 3000,
    remainingCapacity: state.remainingCapacity ?? 3000,
    exits: state.exits ?? EMPTY_EXITS,
    actions: state.actions ?? EMPTY_ACTIONS,
    players: state.players ?? [],
    chatMessages: state.chatMessages ?? [],
  }
}
