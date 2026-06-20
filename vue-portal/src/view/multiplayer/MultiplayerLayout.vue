<script setup lang="ts">
import { computed, provide } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useLobbyController } from '@/controller/useLobbyController'
import { LOBBY_CTRL_KEY } from '@/controller/lobbyContext'
import LobbyAmbientBackground from '@/component/home/LobbyAmbientBackground.vue'
import FriendsSidebar from '@/component/lobby/FriendsSidebar.vue'
import MultiLevelSelectModal from '@/component/lobby/MultiLevelSelectModal.vue'
import HostLeaveModal from '@/component/lobby/HostLeaveModal.vue'
import ConfirmModal from '@/component/common/ConfirmModal.vue'
import LobbyToast from '@/component/lobby/LobbyToast.vue'
import RoomInvitesStrip from '@/component/lobby/RoomInvitesStrip.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const route = useRoute()
const router = useRouter()
const lobby = useLobbyController()

provide(LOBBY_CTRL_KEY, lobby)

const {
  rooms,
  friends,
  friendRequests,
  invites,
  myTeamRoom,
  loading,
  error,
  accountDisplayName,
  friendUsername,
  refreshRooms,
  inviteFriend,
  addFriend,
  acceptFriendRequest,
  rejectFriendRequest,
  acceptInvite,
  rejectInvite,
  levelSelectVisible,
  levelSelection,
  closeLevelSelect,
  confirmStartGame,
  hostLeaveVisible,
  closeHostLeaveModal,
  dissolveTeamRoom,
  transferHostAndLeave,
  hostLeaveCandidates,
  confirmVisible,
  confirmTitle,
  confirmMessage,
  confirmDanger,
  closeConfirm,
  confirmDialog,
  toastMessage,
  clearToast,
} = lobby

const roomCount = computed(() => rooms.value.length)
const totalPlayers = computed(() => rooms.value.reduce((sum, room) => sum + room.playerCount, 0))
const hasTeam = computed(() => !!myTeamRoom.value)
const isHall = computed(() => route.name === 'multiplayer-lobby')
const isTeam = computed(() => route.name === 'multiplayer-team')

async function goHome() {
  myTeamRoom.value = null
  await router.push('/')
}
</script>

<template>
  <div class="mp-shell">
    <LobbyAmbientBackground />
    <div class="mp-vignette" />

    <FriendsSidebar
      :friends="friends"
      :friend-requests="friendRequests"
      :team-room="myTeamRoom"
      v-model:friend-username="friendUsername"
      :loading="loading"
      :can-invite="hasTeam"
      @add="addFriend"
      @invite="inviteFriend"
      @accept-request="acceptFriendRequest"
      @reject-request="rejectFriendRequest"
    />

    <div class="mp-main">
      <header class="mp-toolbar">
        <div class="toolbar-left">
          <button type="button" class="back-link" @click="goHome">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
            </svg>
            首页
          </button>

          <nav class="mp-tabs" aria-label="联机导航">
            <RouterLink
              to="/multiplayer"
              class="tab"
              :class="{ active: isHall }"
            >
              联机大厅
            </RouterLink>
            <RouterLink
              to="/multiplayer/team"
              class="tab"
              :class="{ active: isTeam, disabled: !hasTeam }"
            >
              我的队伍
              <span v-if="hasTeam && myTeamRoom?.inGame" class="tab-dot" />
            </RouterLink>
          </nav>
        </div>

        <div class="toolbar-center">
          <span v-if="accountDisplayName" class="account-chip">
            {{ accountDisplayName.displayName }}
          </span>
          <span class="stat-inline">{{ roomCount }} 房 · {{ totalPlayers }} 人</span>
        </div>

        <GlassButton :disabled="loading" @click="refreshRooms">刷新</GlassButton>
      </header>

      <RoomInvitesStrip
        v-if="!hasTeam"
        :invites="invites"
        :loading="loading"
        @accept="acceptInvite"
        @reject="rejectInvite"
      />

      <RouterView class="mp-page" />
    </div>

    <MultiLevelSelectModal
      :visible="levelSelectVisible"
      :level-selection="levelSelection"
      :loading="loading"
      :error="error"
      @close="closeLevelSelect"
      @confirm="confirmStartGame"
    />

    <HostLeaveModal
      :visible="hostLeaveVisible"
      :members="hostLeaveCandidates()"
      @close="closeHostLeaveModal"
      @dissolve="dissolveTeamRoom"
      @transfer="transferHostAndLeave"
    />

    <ConfirmModal
      :visible="confirmVisible"
      :title="confirmTitle"
      :message="confirmMessage"
      :danger="confirmDanger"
      @confirm="confirmDialog"
      @cancel="closeConfirm"
    />

    <LobbyToast :message="toastMessage" @close="clearToast" />
  </div>
</template>

<style scoped>
.mp-shell {
  position: relative;
  display: flex;
  height: 100vh;
  overflow: hidden;
  animation: mpIn 0.5s ease both;
}

.mp-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, transparent 60%, rgba(8, 10, 18, 0.35) 100%),
    linear-gradient(to bottom, rgba(8, 10, 18, 0.1), transparent 30%, rgba(8, 10, 18, 0.15));
  z-index: 1;
}

.mp-main {
  position: relative;
  z-index: 2;
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 12px 16px 14px;
  gap: 10px;
  overflow: hidden;
}

.mp-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(8, 12, 22, 0.72);
  backdrop-filter: blur(10px);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.82rem;
  white-space: nowrap;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  font: inherit;
}

.back-link svg {
  width: 16px;
  height: 16px;
}

.back-link:hover {
  color: var(--text-primary);
}

.mp-tabs {
  display: flex;
  gap: 6px;
  padding: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tab {
  position: relative;
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 0.84rem;
  color: var(--text-muted);
  text-decoration: none;
  transition: background 0.15s ease, color 0.15s ease;
  white-space: nowrap;
}

.tab:hover:not(.disabled) {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.06);
}

.tab.active {
  color: #dbeaff;
  background: rgba(88, 166, 255, 0.2);
  border: 1px solid rgba(88, 166, 255, 0.35);
}

.tab.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.tab-dot {
  position: absolute;
  top: 6px;
  right: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #6ee7a0;
  box-shadow: 0 0 6px rgba(110, 231, 160, 0.8);
}

.toolbar-center {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  justify-content: center;
  min-width: 0;
}

.account-chip {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.8rem;
  color: #dbeaff;
  background: rgba(88, 166, 255, 0.12);
  border: 1px solid rgba(88, 166, 255, 0.22);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.stat-inline {
  font-size: 0.78rem;
  color: var(--text-muted);
  white-space: nowrap;
}

.mp-page {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

@keyframes mpIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 900px) {
  .mp-shell {
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
  }

  .mp-main {
    flex: 1;
    min-height: 0;
    padding: 10px 12px 12px;
  }

  .toolbar-center {
    display: none;
  }

  .mp-tabs .tab {
    padding: 6px 12px;
    font-size: 0.8rem;
  }
}
</style>
