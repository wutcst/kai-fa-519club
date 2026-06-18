/**
 * 游戏 BGM：Web Audio 合成轻快循环（无需外部音频文件）。
 * 需在用户点击「开始游戏 / 进房」后调用 start() 以满足浏览器自动播放策略。
 */

import {
  ensureGameAudio,
  getBgmGain,
  isGameBgmMuted,
  toggleGameBgmMute,
  unlockGameAudio,
} from '@/service/gameAudio'

const MELODY = [
  523.25, 587.33, 659.25, 783.99, 659.25, 587.33, 523.25, 493.88,
  440.0, 493.88, 523.25, 587.33, 659.25, 587.33, 523.25, 523.25,
]

const BEAT_MS = 360

let beatTimer: ReturnType<typeof setInterval> | null = null
let stepIndex = 0
let running = false

function playTone(frequency: number, startTime: number, duration: number, volume: number, type: OscillatorType) {
  const ctx = ensureGameAudio()
  const bus = getBgmGain()
  if (!bus) {
    return
  }

  const oscillator = ctx.createOscillator()
  const gain = ctx.createGain()
  oscillator.type = type
  oscillator.frequency.value = frequency

  gain.gain.setValueAtTime(0.0001, startTime)
  gain.gain.exponentialRampToValueAtTime(volume, startTime + 0.025)
  gain.gain.exponentialRampToValueAtTime(0.0001, startTime + duration)

  oscillator.connect(gain)
  gain.connect(bus)
  oscillator.start(startTime)
  oscillator.stop(startTime + duration + 0.05)
}

function playBeat() {
  if (!running || isGameBgmMuted()) {
    return
  }

  const ctx = ensureGameAudio()
  void ctx.resume()

  const time = ctx.currentTime
  const frequency = MELODY[stepIndex % MELODY.length]
  playTone(frequency, time, 0.26, 0.22, 'triangle')

  if (stepIndex % 2 === 0) {
    playTone(frequency / 2, time, 0.32, 0.12, 'sine')
  }

  if (stepIndex % 4 === 0) {
    playTone(130.81, time, 0.4, 0.08, 'sine')
  }

  stepIndex += 1
}

export function isGameBgmRunning(): boolean {
  return running
}

export { isGameBgmMuted, unlockGameAudio as unlockGameBgm, toggleGameBgmMute }

export function startGameBgm() {
  unlockGameAudio()
  if (running) {
    return
  }
  running = true
  stepIndex = 0
  playBeat()
  beatTimer = setInterval(playBeat, BEAT_MS)
}

export function stopGameBgm() {
  running = false
  if (beatTimer) {
    clearInterval(beatTimer)
    beatTimer = null
  }
}

export function shouldPlayBgmForRoute(routeName: string | symbol | null | undefined): boolean {
  return routeName === 'solo' || routeName === 'multiplayer-room'
}
