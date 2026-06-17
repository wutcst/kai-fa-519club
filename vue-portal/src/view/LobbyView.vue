<script setup lang="ts">
/**
 * 联机大厅 View：动态走廊背景 + 高级游戏感布局。
 */
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useLobbyController } from '@/controller/useLobbyController'
import LobbyAmbientBackground from '@/component/home/LobbyAmbientBackground.vue'
import CreateRoomPanel from '@/component/lobby/CreateRoomPanel.vue'
import RoomListPanel from '@/component/lobby/RoomListPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const {
  rooms,
  loading,
  error,
  displayName,
  roomName,
  selectedRoomId,
  refreshRooms,
  createAndEnter,
  joinSelected,
} = useLobbyController()

const roomCount = computed(() => rooms.value.length)
const totalPlayers = computed(() => rooms.value.reduce((sum, room) => sum + room.playerCount, 0))
const selectedRoom = computed(() => rooms.value.find((room) => room.roomId === selectedRoomId.value))
</script>

<template>
  <div class="lobby-view">
    <LobbyAmbientBackground />
    <div class="lobby-vignette" />

    <div class="lobby-shell">
      <nav class="lobby-nav">
        <RouterLink to="/" class="back-link">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
          返回首页
        </RouterLink>
        <GlassButton :disabled="loading" @click="refreshRooms">刷新大厅</GlassButton>
      </nav>

      <header class="lobby-hero">
        <div class="hero-badge">MULTIPLAYER LOBBY</div>
        <h1>联机归寝大厅</h1>
        <p>创建或加入房间，与队友一起在熄灯前赶回寝室</p>

        <div class="stats-row">
          <div class="stat-chip">
            <span class="stat-value">{{ roomCount }}</span>
            <span class="stat-label">在线房间</span>
          </div>
          <div class="stat-chip">
            <span class="stat-value">{{ totalPlayers }}</span>
            <span class="stat-label">在线玩家</span>
          </div>
          <div class="stat-chip accent">
            <span class="stat-value">4</span>
            <span class="stat-label">房间上限人数</span>
          </div>
        </div>
      </header>

      <div class="lobby-grid">
        <CreateRoomPanel
          v-model:display-name="displayName"
          v-model:room-name="roomName"
          :loading="loading"
          @create="createAndEnter"
        />

        <RoomListPanel
          :rooms="rooms"
          :selected-room-id="selectedRoomId"
          :loading="loading"
          :error="error"
          @refresh="refreshRooms"
          @join="joinSelected"
          @select="selectedRoomId = $event"
        />
      </div>

      <footer v-if="selectedRoom" class="lobby-preview">
        <span class="preview-label">已选房间</span>
        <strong>{{ selectedRoom.roomName }}</strong>
        <span class="preview-meta">
          第 {{ selectedRoom.level }} 关 · {{ selectedRoom.playerCount }}/4 人 · 剩余
          {{ selectedRoom.remainingSeconds }} 秒
        </span>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.lobby-view {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.lobby-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, transparent 58%, rgba(8, 10, 18, 0.32) 100%),
    linear-gradient(to bottom, rgba(8, 10, 18, 0.08), transparent 28%, rgba(8, 10, 18, 0.18));
  z-index: 1;
}

.lobby-shell {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: lobbyIn 0.7s ease both;
}

.lobby-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.88rem;
  transition: color 0.15s ease;
}

.back-link svg {
  width: 18px;
  height: 18px;
}

.back-link:hover {
  color: var(--text-primary);
}

.lobby-hero {
  text-align: center;
  max-width: 720px;
  margin: 0 auto;
  padding: 8px 16px 12px;
  border-radius: 20px;
  background: rgba(8, 12, 22, 0.28);
  backdrop-filter: blur(6px);
}

.hero-badge {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  color: #ffc9a0;
  border: 1px solid rgba(255, 160, 80, 0.25);
  background: rgba(255, 140, 60, 0.08);
  margin-bottom: 12px;
}

.lobby-hero h1 {
  margin: 0;
  font-size: clamp(1.6rem, 4vw, 2.2rem);
  font-weight: 800;
  text-shadow: 0 0 30px rgba(255, 170, 90, 0.2);
}

.lobby-hero p {
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 0.92rem;
}

.stats-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-top: 20px;
}

.stat-chip {
  min-width: 100px;
  padding: 10px 16px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(12, 16, 28, 0.42);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-chip.accent {
  border-color: rgba(88, 166, 255, 0.3);
  background: rgba(88, 166, 255, 0.08);
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.lobby-grid {
  display: grid;
  grid-template-columns: minmax(300px, 400px) minmax(340px, 1fr);
  gap: 20px;
  align-items: start;
  width: min(1080px, 100%);
  margin: 0 auto;
}

.lobby-preview {
  width: min(1080px, 100%);
  margin: 0 auto;
  padding: 12px 18px;
  border-radius: 999px;
  border: 1px solid rgba(88, 166, 255, 0.25);
  background: rgba(12, 16, 28, 0.72);
  backdrop-filter: blur(10px);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  justify-content: center;
  font-size: 0.88rem;
}

.preview-label {
  color: var(--text-muted);
}

.preview-meta {
  color: var(--text-muted);
  font-size: 0.82rem;
}

@keyframes lobbyIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 900px) {
  .lobby-grid {
    grid-template-columns: 1fr;
  }
}
</style>
