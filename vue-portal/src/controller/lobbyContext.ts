import { inject, type InjectionKey } from 'vue'
import type { useLobbyController } from '@/controller/useLobbyController'

export type LobbyController = ReturnType<typeof useLobbyController>

export const LOBBY_CTRL_KEY: InjectionKey<LobbyController> = Symbol('lobbyCtrl')

export function useLobbyContext(): LobbyController {
  const context = inject(LOBBY_CTRL_KEY)
  if (!context) {
    throw new Error('useLobbyContext 必须在 MultiplayerLayout 内使用')
  }
  return context
}
