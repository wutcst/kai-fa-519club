<script setup lang="ts">
import type { PlayerState } from '@/model/types'

defineProps<{
  players: PlayerState[]
  selfPlayerId: string
}>()
</script>

<template>
  <aside class="player-dock glass-hud">
    <div class="dock-title">同房间玩家</div>
    <ul class="player-list">
      <li
        v-for="player in players"
        :key="player.playerId"
        :class="['player-chip', { self: player.playerId === selfPlayerId }]"
      >
        <span class="name">{{ player.displayName }}</span>
        <span v-if="player.playerId === selfPlayerId" class="badge">你</span>
        <span class="loc">{{ player.roomName }}</span>
      </li>
    </ul>
  </aside>
</template>

<style scoped>
.player-dock {
  position: absolute;
  left: 16px;
  top: 16px;
  z-index: 20;
  padding: 12px 14px;
  max-width: 220px;
}

.dock-title {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.player-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.player-chip {
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  font-size: 0.82rem;
}

.player-chip.self {
  background: rgba(88, 166, 255, 0.12);
  border: 1px solid rgba(88, 166, 255, 0.3);
}

.name {
  font-weight: 600;
}

.badge {
  margin-left: 6px;
  font-size: 0.65rem;
  padding: 1px 5px;
  border-radius: 999px;
  background: rgba(88, 166, 255, 0.3);
}

.loc {
  display: block;
  margin-top: 2px;
  color: var(--text-muted);
  font-size: 0.72rem;
}

.glass-hud {
  background: var(--hud-bg);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius-sm);
  backdrop-filter: blur(12px);
}
</style>
