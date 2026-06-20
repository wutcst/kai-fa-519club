import { apiRequest, jsonPost } from '@/service/httpClient'
import type { CommandRequest, CommandResponse, GameState } from '@/model/types'

export function fetchGameState(roomId: string, playerId: string): Promise<GameState> {
  const query = new URLSearchParams({ roomId, playerId })
  return apiRequest<GameState>(`/api/game/state?${query.toString()}`)
}

export function executeCommand(body: CommandRequest): Promise<CommandResponse> {
  return jsonPost<CommandResponse>('/api/game/command', body)
}
