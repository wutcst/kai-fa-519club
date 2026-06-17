import type { MoveDirection } from '@/model/gameDisplayMode'

/** 定向暗角时长（沉浸模式点击方向后） */
export const VIGNETTE_MS = 300

/** 房间切换动画时长 */
export const ROOM_TRANSITION_MS = 480

export function sleep(ms: number) {
  return new Promise<void>((resolve) => {
    setTimeout(resolve, ms)
  })
}

export function isMoveDirection(value: string): value is MoveDirection {
  return value === 'north' || value === 'south' || value === 'east' || value === 'west' || value === 'back'
}
