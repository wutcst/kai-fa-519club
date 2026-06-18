import { ref } from 'vue'

export const screenShake = ref(false)

let shakeTimer: ReturnType<typeof setTimeout> | null = null

export function triggerScreenShake(durationMs = 380) {
  screenShake.value = true
  if (shakeTimer) {
    clearTimeout(shakeTimer)
  }
  shakeTimer = setTimeout(() => {
    screenShake.value = false
    shakeTimer = null
  }, durationMs)
}
