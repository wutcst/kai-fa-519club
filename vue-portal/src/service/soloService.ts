import { apiRequest, jsonPost } from '@/service/httpClient'
import type { SoloCommandResponse, SoloSession, SoloViewState } from '@/model/soloTypes'

export function createSoloSession(playerName?: string): Promise<SoloSession> {
  return jsonPost<SoloSession>('/api/solo/sessions', { playerName: playerName ?? '玩家' })
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
