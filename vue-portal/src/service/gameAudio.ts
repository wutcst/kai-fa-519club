/**
 * 共享 Web Audio 上下文（BGM + SFX）。
 */

const BGM_VOLUME = 0.1
const SFX_VOLUME = 0.14

let audioContext: AudioContext | null = null
let bgmGain: GainNode | null = null
let sfxGain: GainNode | null = null
let bgmMuted = false
let sfxMuted = false

function loadBgmMuted(): boolean {
  try {
    return sessionStorage.getItem('zuul-bgm-muted') === '1'
  } catch {
    return false
  }
}

function loadSfxMuted(): boolean {
  try {
    return sessionStorage.getItem('zuul-sfx-muted') === '1'
  } catch {
    return false
  }
}

function saveBgmMuted(value: boolean) {
  try {
    sessionStorage.setItem('zuul-bgm-muted', value ? '1' : '0')
  } catch {
    // ignore
  }
}

function saveSfxMuted(value: boolean) {
  try {
    sessionStorage.setItem('zuul-sfx-muted', value ? '1' : '0')
  } catch {
    // ignore
  }
}

bgmMuted = loadBgmMuted()
sfxMuted = loadSfxMuted()

export function ensureGameAudio(): AudioContext {
  if (!audioContext) {
    audioContext = new AudioContext()
    bgmGain = audioContext.createGain()
    sfxGain = audioContext.createGain()
    bgmGain.gain.value = bgmMuted ? 0 : BGM_VOLUME
    sfxGain.gain.value = sfxMuted ? 0 : SFX_VOLUME
    bgmGain.connect(audioContext.destination)
    sfxGain.connect(audioContext.destination)
  }
  return audioContext
}

export function getBgmGain(): GainNode | null {
  ensureGameAudio()
  return bgmGain
}

export function getSfxGain(): GainNode | null {
  ensureGameAudio()
  return sfxGain
}

export function unlockGameAudio() {
  const ctx = ensureGameAudio()
  void ctx.resume()
}

export function isGameBgmMuted(): boolean {
  return bgmMuted
}

export function isGameSfxMuted(): boolean {
  return sfxMuted
}

export function setGameBgmMuted(value: boolean) {
  bgmMuted = value
  saveBgmMuted(value)
  if (bgmGain && audioContext) {
    bgmGain.gain.setTargetAtTime(value ? 0 : BGM_VOLUME, audioContext.currentTime, 0.08)
  }
}

export function setGameSfxMuted(value: boolean) {
  sfxMuted = value
  saveSfxMuted(value)
  if (sfxGain && audioContext) {
    sfxGain.gain.setTargetAtTime(value ? 0 : SFX_VOLUME, audioContext.currentTime, 0.08)
  }
}

export function toggleGameBgmMute(): boolean {
  setGameBgmMuted(!bgmMuted)
  if (!bgmMuted) {
    unlockGameAudio()
  }
  return bgmMuted
}

export function toggleGameSfxMute(): boolean {
  setGameSfxMuted(!sfxMuted)
  if (!sfxMuted) {
    unlockGameAudio()
  }
  return sfxMuted
}
