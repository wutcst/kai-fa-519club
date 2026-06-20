<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import GlassButton from '@/component/common/GlassButton.vue'

const props = defineProps<{
  visible: boolean
  password: string
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:password': [value: string]
  confirm: []
  cancel: []
}>()

const inputRef = ref<HTMLInputElement | null>(null)

watch(
  () => props.visible,
  (open) => {
    if (!open) {
      return
    }
    void nextTick(() => {
      inputRef.value?.focus()
      inputRef.value?.select()
    })
  },
)

function onInput(event: Event) {
  const value = (event.target as HTMLInputElement).value.replace(/\D/g, '').slice(0, 8)
  emit('update:password', value)
}

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    event.preventDefault()
    emit('confirm')
  }
  if (event.key === 'Escape') {
    event.preventDefault()
    emit('cancel')
  }
}
</script>

<template>
  <Transition name="fade">
    <div v-if="visible" class="unlock-layer" @click.self="emit('cancel')">
      <div class="unlock-card glass-hud" role="dialog" aria-labelledby="unlock-title">
        <div class="unlock-icon" aria-hidden="true">
          <svg viewBox="0 0 48 48">
            <rect x="10" y="20" width="28" height="20" rx="4" fill="none" stroke="currentColor" stroke-width="2.2" />
            <path d="M16 20v-4a8 8 0 0 1 16 0v4" fill="none" stroke="currentColor" stroke-width="2.2" />
            <circle cx="24" cy="30" r="2.5" fill="currentColor" />
          </svg>
        </div>
        <h3 id="unlock-title">解锁寝室智能锁</h3>
        <p class="hint">请输入八位数字密码</p>
        <input
          ref="inputRef"
          class="password-input"
          type="text"
          inputmode="numeric"
          pattern="[0-9]*"
          maxlength="8"
          autocomplete="off"
          placeholder="••••••••"
          :value="password"
          :disabled="loading"
          @input="onInput"
          @keydown="onKeydown"
        />
        <p class="digit-count">{{ password.length }} / 8</p>
        <div class="actions">
          <GlassButton accent :disabled="loading || password.length === 0" @click="emit('confirm')">
            {{ loading ? '解锁中…' : '确认解锁' }}
          </GlassButton>
          <GlassButton :disabled="loading" @click="emit('cancel')">取消</GlassButton>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.unlock-layer {
  position: absolute;
  inset: 0;
  z-index: 45;
  display: grid;
  place-items: center;
  background: rgba(4, 6, 12, 0.62);
  backdrop-filter: blur(4px);
}

.unlock-card {
  width: min(360px, calc(100vw - 40px));
  padding: 22px 24px 20px;
  text-align: center;
}

.unlock-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 10px;
  color: #9ecaff;
}

.unlock-icon svg {
  width: 100%;
  height: 100%;
}

.unlock-card h3 {
  margin: 0 0 6px;
  font-size: 1.05rem;
}

.hint {
  margin: 0 0 14px;
  font-size: 0.86rem;
  color: var(--text-muted);
}

.password-input {
  width: 100%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(88, 166, 255, 0.35);
  background: rgba(255, 255, 255, 0.06);
  color: #eef4ff;
  font: inherit;
  font-size: 1.25rem;
  letter-spacing: 0.28em;
  text-align: center;
}

.password-input:focus {
  outline: none;
  border-color: rgba(88, 166, 255, 0.65);
  box-shadow: 0 0 0 3px rgba(88, 166, 255, 0.15);
}

.password-input:disabled {
  opacity: 0.6;
}

.digit-count {
  margin: 8px 0 0;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 16px;
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid rgba(88, 166, 255, 0.28);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
