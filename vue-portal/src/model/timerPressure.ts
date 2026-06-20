/** 熄灯倒计时全屏压迫感阈值（秒） */
export const TIMER_WARNING_SECONDS = 45
export const TIMER_DANGER_SECONDS = 30
export const TIMER_CRITICAL_SECONDS = 15

export type TimerPressureLevel = 'none' | 'warning' | 'danger' | 'critical'

export function timerPressureLevel(remainingSeconds: number): TimerPressureLevel {
  if (remainingSeconds <= TIMER_CRITICAL_SECONDS) {
    return 'critical'
  }
  if (remainingSeconds <= TIMER_DANGER_SECONDS) {
    return 'danger'
  }
  if (remainingSeconds <= TIMER_WARNING_SECONDS) {
    return 'warning'
  }
  return 'none'
}

export function isTimerWarningLevel(level: TimerPressureLevel): boolean {
  return level === 'warning'
}

export function isTimerDangerLevel(level: TimerPressureLevel): boolean {
  return level === 'danger' || level === 'critical'
}
