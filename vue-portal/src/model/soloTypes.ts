/** 单机五关 Model 类型 */

export interface ItemView {
  name: string
  weight: number
  longDescription: string
  edible: boolean
}

export interface ExitAvailability {
  north: boolean
  south: boolean
  east: boolean
  west: boolean
  back: boolean
}

export interface UiActionFlags {
  showNpc: boolean
  showFeed: boolean
  showCombine: boolean
  showUnlock: boolean
  showSleep: boolean
  showSubmit: boolean
}

export interface OutcomeOverlay {
  type: string
  title: string
  message: string
  actionLabel: string
}

export interface SoloViewState {
  sessionId: string
  level: number
  levelTitle: string
  levelState: string
  remainingSeconds: number
  timerText: string
  roomId: string
  roomDescription: string
  westTrapBanner: string | null
  roomItems: ItemView[]
  inventory: ItemView[]
  inventoryWeight: number
  maxInventoryWeight: number
  remainingCapacity: number
  exits: ExitAvailability
  actions: UiActionFlags
  outcome: OutcomeOverlay | null
  lockedOverlayMessage: string | null
  interactionBlocked: boolean
}

export interface SoloSession {
  sessionId: string
  state: SoloViewState
}

export interface SoloCommandResponse {
  messages: string[]
  popupMessage?: string | null
  combinePrompt?: boolean
  state: SoloViewState
}

export interface SoloSessionRef {
  sessionId: string
  playerName: string
}

export interface SoloLevelOption {
  levelNumber: number
  title: string
  missionHint: string
  unlocked: boolean
  cleared: boolean
}

export interface SoloLevelSelection {
  levels: SoloLevelOption[]
  comingSoonLabel: string
  comingSoonMessage: string
}
