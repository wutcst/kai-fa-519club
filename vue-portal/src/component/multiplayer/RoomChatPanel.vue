<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import type { ChatMessage } from '@/model/types'
import GlassButton from '@/component/common/GlassButton.vue'

const props = defineProps<{
  messages: ChatMessage[]
  selfPlayerId: string
  modelValue: string
  busy?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  send: []
}>()

const expanded = ref(false)
const listRef = ref<HTMLElement | null>(null)

const unreadCount = computed(() => props.messages.length)

function togglePanel() {
  expanded.value = !expanded.value
}

function formatTime(timestampMs: number) {
  const date = new Date(timestampMs)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  },
)
</script>

<template>
  <div class="chat-root">
    <button
      type="button"
      class="chat-toggle"
      :class="{ active: expanded }"
      :aria-expanded="expanded"
      aria-label="打开房间聊天"
      @click="togglePanel"
    >
      <svg class="chat-icon" viewBox="0 0 48 48" aria-hidden="true">
        <path
          d="M10 14h28a4 4 0 0 1 4 4v12a4 4 0 0 1-4 4H22l-8 6v-6h-4a4 4 0 0 1-4-4V18a4 4 0 0 1 4-4z"
          fill="rgba(255,170,90,0.12)"
          stroke="currentColor"
          stroke-width="2.2"
          stroke-linejoin="round"
        />
        <path
          d="M16 24h16M16 30h10"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          opacity="0.7"
        />
      </svg>
      <span v-if="unreadCount > 0 && !expanded" class="badge">{{ unreadCount }}</span>
    </button>

    <Transition name="panel-slide">
      <div v-if="expanded" class="chat-panel glass-hud">
        <div class="panel-header">
          <span class="label">房间聊天</span>
          <button type="button" class="close-btn" aria-label="收起聊天" @click="expanded = false">×</button>
        </div>

        <div ref="listRef" class="chat-list" aria-label="房间聊天">
          <p v-if="messages.length === 0" class="chat-empty">暂无消息，和队友打个招呼吧</p>
          <div
            v-for="message in messages"
            :key="message.id"
            class="chat-line"
            :class="{ self: message.playerId === selfPlayerId }"
          >
            <span class="chat-meta">
              {{ message.displayName }} · {{ formatTime(message.timestampMs) }}
            </span>
            <span class="chat-text">{{ message.text }}</span>
          </div>
        </div>

        <div class="chat-input-row">
          <input
            class="chat-input"
            :value="modelValue"
            maxlength="200"
            placeholder="发送消息…"
            :disabled="busy"
            @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
            @keyup.enter="emit('send')"
          />
          <GlassButton accent :disabled="busy || !modelValue.trim()" @click="emit('send')">
            发送
          </GlassButton>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.chat-root {
  position: absolute;
  right: 16px;
  bottom: 20px;
  z-index: 24;
}

.chat-toggle {
  position: relative;
  width: 58px;
  height: 58px;
  border-radius: 16px;
  border: 1px solid rgba(255, 180, 100, 0.22);
  background: rgba(15, 18, 28, 0.72);
  backdrop-filter: blur(10px);
  color: #ffc9a0;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.chat-toggle:hover {
  background: rgba(255, 170, 90, 0.16);
  border-color: rgba(255, 170, 90, 0.45);
}

.chat-toggle.active {
  border-color: rgba(255, 170, 90, 0.55);
  background: rgba(255, 170, 90, 0.2);
}

.chat-toggle:active {
  transform: scale(0.96);
}

.chat-icon {
  width: 34px;
  height: 34px;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--danger);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.chat-panel {
  position: absolute;
  right: 0;
  bottom: 68px;
  width: min(360px, calc(100vw - 32px));
  padding: 12px 14px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.label {
  font-size: 0.82rem;
  color: var(--text-primary);
  font-weight: 600;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-muted);
  font-size: 1.1rem;
  cursor: pointer;
}

.close-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.14);
}

.chat-list {
  max-height: 180px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
  padding-right: 4px;
}

.chat-empty {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.78rem;
}

.chat-line {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.chat-line.self {
  background: rgba(255, 170, 90, 0.12);
  border-color: rgba(255, 170, 90, 0.22);
}

.chat-meta {
  font-size: 0.68rem;
  color: var(--text-muted);
}

.chat-text {
  font-size: 0.82rem;
  color: var(--text-primary);
  word-break: break-word;
}

.chat-input-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.chat-input {
  flex: 1;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
  font-size: 0.84rem;
}

.chat-input:focus {
  outline: none;
  border-color: rgba(255, 170, 90, 0.45);
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(12px);
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.22s ease;
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
