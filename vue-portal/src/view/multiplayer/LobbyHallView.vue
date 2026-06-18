<script setup lang="ts">
import { useLobbyContext } from '@/controller/lobbyContext'
import CreateRoomPanel from '@/component/lobby/CreateRoomPanel.vue'
import RoomListPanel from '@/component/lobby/RoomListPanel.vue'

const {
  rooms,
  accountDisplayName,
  roomName,
  loading,
  error,
  selectedRoomId,
  createRoom,
  joinSelected,
  refreshRooms,
} = useLobbyContext()
</script>

<template>
  <div class="hall-page">
    <section class="hall-intro">
      <p class="eyebrow">MULTIPLAYER</p>
      <h1>联机大厅</h1>
      <p class="subtitle">创建或加入房间，再前往「我的队伍」组队开黑</p>
    </section>

    <div class="hall-grid">
      <CreateRoomPanel
        compact
        class="hall-create"
        :account-display-name="accountDisplayName"
        v-model:room-name="roomName"
        :loading="loading"
        :error="error"
        @create="createRoom"
      />
      <RoomListPanel
        compact
        fill
        class="hall-list"
        :rooms="rooms"
        :selected-room-id="selectedRoomId"
        :loading="loading"
        :error="error"
        @refresh="refreshRooms"
        @join="joinSelected"
        @select="selectedRoomId = $event"
      />
    </div>
  </div>
</template>

<style scoped>
.hall-page {
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 8px;
  min-height: 0;
  overflow: hidden;
}

.hall-intro {
  flex-shrink: 0;
  padding: 0 2px;
}

.hall-intro h1 {
  margin: 2px 0 0;
  font-size: 1.15rem;
  font-weight: 800;
}

.subtitle {
  margin: 2px 0 0;
  font-size: 0.78rem;
  color: var(--text-muted);
}

.eyebrow {
  margin: 0;
  font-size: 0.65rem;
  letter-spacing: 0.18em;
  color: rgba(255, 180, 120, 0.75);
}

.hall-create,
.hall-list {
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.hall-grid {
  display: grid;
  grid-template-columns: minmax(240px, 0.95fr) minmax(280px, 1.05fr);
  gap: 12px;
  min-height: 0;
  overflow: hidden;
}

@media (max-width: 900px) {
  .hall-grid {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(0, 0.95fr) minmax(0, 1.05fr);
  }

  .hall-intro .subtitle {
    display: none;
  }
}
</style>
