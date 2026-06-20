import { ensureGameAudio, getSfxGain, isGameSfxMuted, unlockGameAudio } from '@/service/gameAudio'

export type GameSfxId = 'move' | 'pickup' | 'blocked' | 'notice' | 'ui'

function playTone(
  frequency: number,
  startTime: number,
  duration: number,
  volume: number,
  type: OscillatorType,
) {
  const ctx = ensureGameAudio()
  const bus = getSfxGain()
  if (!bus || isGameSfxMuted()) {
    return
  }

  const oscillator = ctx.createOscillator()
  const gain = ctx.createGain()
  oscillator.type = type
  oscillator.frequency.value = frequency

  gain.gain.setValueAtTime(0.0001, startTime)
  gain.gain.exponentialRampToValueAtTime(volume, startTime + 0.012)
  gain.gain.exponentialRampToValueAtTime(0.0001, startTime + duration)

  oscillator.connect(gain)
  gain.connect(bus)
  oscillator.start(startTime)
  oscillator.stop(startTime + duration + 0.04)
}

function playNoise(startTime: number, duration: number, volume: number) {
  const ctx = ensureGameAudio()
  const bus = getSfxGain()
  if (!bus || isGameSfxMuted()) {
    return
  }

  const bufferSize = Math.max(1, Math.floor(ctx.sampleRate * duration))
  const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate)
  const data = buffer.getChannelData(0)
  for (let index = 0; index < bufferSize; index += 1) {
    data[index] = (Math.random() * 2 - 1) * (1 - index / bufferSize)
  }

  const source = ctx.createBufferSource()
  const filter = ctx.createBiquadFilter()
  const gain = ctx.createGain()
  source.buffer = buffer
  filter.type = 'lowpass'
  filter.frequency.value = 520

  gain.gain.setValueAtTime(volume, startTime)
  gain.gain.exponentialRampToValueAtTime(0.0001, startTime + duration)

  source.connect(filter)
  filter.connect(gain)
  gain.connect(bus)
  source.start(startTime)
  source.stop(startTime + duration)
}

const SFX_PLAYERS: Record<GameSfxId, () => void> = {
  move() {
    const time = ensureGameAudio().currentTime
    playTone(180, time, 0.08, 0.12, 'sine')
    playNoise(time + 0.02, 0.1, 0.06)
  },
  pickup() {
    const time = ensureGameAudio().currentTime
    playTone(880, time, 0.1, 0.16, 'sine')
    playTone(1174.66, time + 0.07, 0.12, 0.12, 'triangle')
  },
  blocked() {
    const time = ensureGameAudio().currentTime
    playTone(140, time, 0.14, 0.2, 'square')
    playTone(95, time + 0.05, 0.18, 0.14, 'sine')
  },
  notice() {
    const time = ensureGameAudio().currentTime
    playTone(523.25, time, 0.08, 0.1, 'triangle')
    playTone(659.25, time + 0.06, 0.1, 0.08, 'sine')
  },
  ui() {
    const time = ensureGameAudio().currentTime
    playTone(420, time, 0.05, 0.08, 'sine')
  },
}

export function playGameSfx(id: GameSfxId) {
  unlockGameAudio()
  SFX_PLAYERS[id]()
}
