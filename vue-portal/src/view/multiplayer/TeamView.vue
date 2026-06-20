<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useLobbyContext } from '@/controller/lobbyContext'
import TeamRoomPanel from '@/component/lobby/TeamRoomPanel.vue'

const router = useRouter()
const {
  myTeamRoom,
  friends,
  loading,
  openLevelSelect,
  handleLeaveTeam,
  inviteFriend,
} = useLobbyContext()

function ensureTeamPage() {
  if (!myTeamRoom.value) {
    void router.replace({ name: 'multiplayer-lobby' })
  }
}

onMounted(ensureTeamPage)
watch(myTeamRoom, ensureTeamPage)
</script>

<template>
  <div v-if="myTeamRoom" class="team-page">
    <section class="team-intro">
      <p class="eyebrow">TEAM UP</p>
      <h1>我的队伍</h1>
      <p class="subtitle">查看队员、邀请好友，房主选关后开始归寝</p>
    </section>

    <TeamRoomPanel
      compact
      fill
      class="team-panel"
      :team-room="myTeamRoom"
      :friends="friends"
      :loading="loading"
      @start="openLevelSelect"
      @leave="handleLeaveTeam"
      @invite="inviteFriend"
    />
  </div>
</template>

<style scoped>
.team-page {
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 8px;
  min-height: 0;
  overflow: hidden;
}

.team-intro {
  padding: 0 2px;
}

.team-intro h1 {
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

@media (max-height: 760px) {
  .team-intro .subtitle,
  .team-intro .eyebrow {
    display: none;
  }
}

.team-panel {
  min-height: 0;
  height: 100%;
  max-width: 920px;
  width: 100%;
  margin: 0 auto;
}
</style>
