import { apiGet, apiRequest, jsonPost } from '@/service/httpClient'
import type {
  SoloCommandResponse,
  SoloLevelSelection,
  SoloSession,
  SoloViewState,
} from '@/model/soloTypes'

const FALLBACK_LEVEL_SELECTION: SoloLevelSelection = {
  levels: [
    {
      levelNumber: 1,
      title: '第一关：初入校园',
      missionHint: '本关目标：拾取三十元，超市换一卡通，回寝室睡觉。',
      unlocked: true,
      cleared: false,
    },
    {
      levelNumber: 2,
      title: '第二关：归寝凭证',
      missionHint: '本关目标：北楼对话领归寝单、超市换一卡通并提交，可探索体育馆与食堂。',
      unlocked: false,
      cleared: false,
    },
    {
      levelNumber: 3,
      title: '第三关：西楼迷局',
      missionHint: '本关目标：体育馆取手电闯黑暗主楼，西楼合成锤子，持双证回寝。',
      unlocked: false,
      cleared: false,
    },
    {
      levelNumber: 4,
      title: '第四关：博学暗夜',
      missionHint: '本关目标：换卡后进图书馆领归寝单，可选喂猫，回寝睡觉。',
      unlocked: false,
      cleared: false,
    },
    {
      levelNumber: 5,
      title: '第五关：终夜归寝',
      missionHint: '本关目标：全图探索，读馆公告推智能锁密码，解锁后睡觉通关。',
      unlocked: false,
      cleared: false,
    },
  ],
  comingSoonLabel: '…',
  comingSoonMessage: '关卡正在开发',
}

export interface SoloLevelsFetchResult {
  selection: SoloLevelSelection
  fromFallback: boolean
}

export async function fetchSoloLevels(): Promise<SoloLevelsFetchResult> {
  try {
    const selection = await apiGet<SoloLevelSelection>('/api/solo/levels')
    return { selection, fromFallback: false }
  } catch {
    return { selection: FALLBACK_LEVEL_SELECTION, fromFallback: true }
  }
}

export function createSoloSession(playerName?: string, levelNumber = 1): Promise<SoloSession> {
  return jsonPost<SoloSession>('/api/solo/sessions', {
    playerName: playerName ?? '玩家',
    levelNumber,
  })
}

export function fetchSoloState(sessionId: string): Promise<SoloViewState> {
  return apiRequest<SoloViewState>(`/api/solo/sessions/${encodeURIComponent(sessionId)}/state`)
}

export function executeSoloCommand(
  sessionId: string,
  commandWord: string,
  secondWord?: string,
): Promise<SoloCommandResponse> {
  return jsonPost<SoloCommandResponse>(
    `/api/solo/sessions/${encodeURIComponent(sessionId)}/command`,
    { commandWord, secondWord },
  )
}

export function performSoloLook(sessionId: string): Promise<SoloCommandResponse> {
  return jsonPost<SoloCommandResponse>(`/api/solo/sessions/${encodeURIComponent(sessionId)}/look`, {})
}

export function performSoloTalk(sessionId: string): Promise<SoloCommandResponse> {
  return jsonPost<SoloCommandResponse>(`/api/solo/sessions/${encodeURIComponent(sessionId)}/talk`, {})
}

export function dismissSoloOutcome(sessionId: string): Promise<SoloViewState> {
  return jsonPost<SoloViewState>(
    `/api/solo/sessions/${encodeURIComponent(sessionId)}/outcome/dismiss`,
    {},
  )
}

export function dismissSoloLocked(sessionId: string): Promise<SoloViewState> {
  return jsonPost<SoloViewState>(
    `/api/solo/sessions/${encodeURIComponent(sessionId)}/locked/dismiss`,
    {},
  )
}

export function destroySoloSession(sessionId: string): Promise<boolean> {
  return apiRequest<boolean>(`/api/solo/sessions/${encodeURIComponent(sessionId)}`, {
    method: 'DELETE',
  })
}
