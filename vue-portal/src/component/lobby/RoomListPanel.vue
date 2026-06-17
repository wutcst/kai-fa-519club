<script setup lang="ts">
import type { RoomInfo } from '@/model/types'
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  rooms: RoomInfo[]
  selectedRoomId: string
  loading: boolean
  error: string
}>()

const emit = defineEmits<{
  refresh: []
  join: []
  select: [roomId: string]
}>()

function playerDots(count: number) {
  return Array.from({ length: 4 }, (_, index) => index < count)
}
</script>

<template>
  <GlassPanel strong padding="22px 24px" class="list-panel">
    <div class="head">
      <div class="head-left">
        <svg class="panel-icon" viewBox="0 0 48 48" aria-hidden="true">
          <rect x="6" y="10" width="36" height="28" rx="4" fill="none" stroke="currentColor" stroke-width="2.2" />
          <path d="M6 18h36" stroke="currentColor" stroke-width="2.2" />
          <circle cx="14" cy="14" r="1.5" fill="currentColor" />
          <circle cx="20" cy="14" r="1.5" fill="currentColor" />
        </svg>
        <div>
          <h2>房间列表</h2>
          <p class="muted">选择房间加入，列表自动刷新</p>
        </div>
      </div>
      <GlassButton :disabled="loading" @click="emit('refresh')">刷新</GlassButton>
    </div>

    <div v-if="rooms.length === 0" class="empty">
      <div class="empty-icon" />
      <p>暂无在线房间</p>
      <span class="muted">创建一个新房间，或稍后再来看看</span>
    </div>

    <ul v-else class="room-list">
      <li
        v-for="room in rooms"
        :key="room.roomId"
        :class="['room-item', { active: selectedRoomId === room.roomId }]"
        @click="emit('select', room.roomId)"
      >
        <div class="room-main">
          <div class="title">{{ room.roomName }}</div>
          <div class="meta">
            <span class="level-badge">L{{ room.level }}</span>
            <span>{{ room.remainingSeconds }}s 剩余</span>
          </div>
        </div>
        <div class="room-side">
          <div class="player-dots">
            <span
              v-for="(filled, index) in playerDots(room.playerCount)"
              :key="index"
              class="dot"
              :class="{ filled }"
            />
          </div>
          <span class="player-count">{{ room.playerCount }}/4</span>
        </div>
      </li>
    </ul>

    <GlassButton
      accent
      class="join-btn"
      :disabled="loading || rooms.length === 0"
      @click="emit('join')"
    >
      加入选中房间
    </GlassButton>

    <p v-if="error" class="error">{{ error }}</p>
  </GlassPanel>
</template>

<style scoped>
.list-panel {
  border: 1px solid rgba(88, 166, 255, 0.18);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.head-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.panel-icon {
  width: 40px;
  height: 40px;
  color: var(--accent);
  flex-shrink: 0;
}

.head h2 {
  margin: 0;
  font-size: 1.12rem;
}

.muted {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 0.82rem;
}

.room-list {
  list-style: none;
  margin: 0 0 14px;
  padding: 0;
  max-height: 360px;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease, transform 0.12s ease;
}

.room-item:hover {
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.16);
}

.room-item.active {
  border-color: rgba(88, 166, 255, 0.5);
  background: rgba(88, 166, 255, 0.12);
  box-shadow: 0 0 20px rgba(88, 166, 255, 0.12);
}

.title {
  font-weight: 600;
  margin-bottom: 6px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.level-badge {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(88, 166, 255, 0.15);
  color: #9ecaff;
  font-weight: 600;
}

.room-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.player-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
}

.dot.filled {
  background: var(--accent);
  border-color: rgba(136, 198, 255, 0.6);
  box-shadow: 0 0 8px rgba(88, 166, 255, 0.45);
}

.player-count {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.join-btn {
  width: 100%;
}

.empty {
  padding: 40px 16px;
  text-align: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.03);
}

.empty p {
  margin: 0 0 6px;
  font-weight: 600;
}

.error {
  margin: 12px 0 0;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: rgba(220, 90, 90, 0.12);
  border: 1px solid rgba(220, 90, 90, 0.3);
  color: #ffb4b4;
  font-size: 0.88rem;
}
</style>
