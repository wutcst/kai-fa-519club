<script setup lang="ts">
import { ref } from 'vue'
import type { FriendView, TeamRoom } from '@/model/types'
import UserAvatar from '@/component/common/UserAvatar.vue'
import InviteFriendModal from '@/component/lobby/InviteFriendModal.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const props = defineProps<{
  teamRoom: TeamRoom | null
  friends: FriendView[]
  loading: boolean
  accountName?: string
  compact?: boolean
  fill?: boolean
}>()

const emit = defineEmits<{
  start: []
  leave: []
  invite: [friend: FriendView]
}>()

const MAX_SLOTS = 4
const inviteModalVisible = ref(false)

function canOpenInviteSlot() {
  return props.teamRoom?.host === true && !props.teamRoom.inGame && !props.loading
}

function onEmptySlotClick() {
  if (!canOpenInviteSlot()) {
    return
  }
  inviteModalVisible.value = true
}

function closeInviteModal() {
  inviteModalVisible.value = false
}

function onInviteFromModal(friend: FriendView) {
  emit('invite', friend)
  inviteModalVisible.value = false
}
</script>

<template>
  <section class="team-section" :class="{ compact, fill }">
    <header class="team-header">
      <div>
        <p class="eyebrow">TEAM STATUS</p>
        <h2>队伍情况</h2>
      </div>
      <div v-if="teamRoom" class="room-meta">
        <span class="room-name">{{ teamRoom.roomName }}</span>
        <span class="tag" :class="{ host: teamRoom.host }">{{ teamRoom.host ? '房主' : '队员' }}</span>
        <span v-if="teamRoom.inGame" class="tag playing">游戏中</span>
        <span v-else class="tag lobby">组队中</span>
      </div>
    </header>

    <template v-if="teamRoom">
      <div class="member-grid">
        <div
          v-for="slot in MAX_SLOTS"
          :key="slot"
          class="member-slot"
          :class="{
            filled: !!teamRoom.members[slot - 1],
            host: teamRoom.members[slot - 1]?.host,
            inviteable: !teamRoom.members[slot - 1] && canOpenInviteSlot(),
          }"
          @click="!teamRoom.members[slot - 1] ? onEmptySlotClick() : undefined"
        >
          <template v-if="teamRoom.members[slot - 1]">
            <UserAvatar
              :display-name="teamRoom.members[slot - 1].displayName"
              :size="44"
            />
            <strong>{{ teamRoom.members[slot - 1].displayName }}</strong>
            <span v-if="teamRoom.members[slot - 1].host" class="slot-role">房主</span>
            <span v-else class="slot-role muted">队员</span>
          </template>
          <template v-else>
            <span class="slot-empty">+</span>
            <span class="slot-role muted">{{ canOpenInviteSlot() ? '点击邀请' : '等待加入' }}</span>
          </template>
        </div>
      </div>

      <p class="member-count">{{ teamRoom.members.length }} / {{ MAX_SLOTS }} 人</p>

      <div class="team-actions">
        <GlassButton v-if="teamRoom.host" accent :disabled="loading" @click="emit('start')">
          开始游戏 · 选关
        </GlassButton>
        <GlassButton :disabled="loading" @click="emit('leave')">离开队伍</GlassButton>
      </div>
    </template>

    <div v-else class="no-team">
      <p>你还没有加入队伍</p>
      <span>请先到「联机大厅」创建或加入房间</span>
    </div>

    <InviteFriendModal
      :visible="inviteModalVisible"
      :friends="friends"
      :team-room="teamRoom"
      :loading="loading"
      @close="closeInviteModal"
      @invite="onInviteFromModal"
    />
  </section>
</template>

<style scoped>
.team-section {
  padding: 24px 28px;
  border-radius: 22px;
  border: 1px solid rgba(255, 160, 80, 0.22);
  background:
    linear-gradient(145deg, rgba(255, 140, 60, 0.06), transparent 45%),
    rgba(10, 14, 26, 0.88);
  backdrop-filter: blur(12px);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.35);
}

.team-section.compact {
  padding: 16px 20px;
  border-radius: 18px;
}

.team-section.fill {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.team-section.compact .team-header {
  margin-bottom: 12px;
}

.team-section.compact .team-header h2 {
  font-size: 1.15rem;
}

.team-section.fill .member-grid {
  flex: 1;
  align-content: center;
}

.team-section.compact .member-slot {
  min-height: 96px;
  padding: 12px 8px;
}

.team-section.fill .team-actions {
  margin-top: auto;
  padding-top: 12px;
}

.team-section.compact .no-team {
  padding: 28px 16px;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0;
  font-size: 0.7rem;
  letter-spacing: 0.18em;
  color: rgba(255, 180, 120, 0.8);
}

.team-header h2 {
  margin: 6px 0 0;
  font-size: 1.5rem;
}

.room-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.room-name {
  font-size: 1.05rem;
  font-weight: 700;
  color: #ffe0c8;
}

.tag {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.72rem;
  background: rgba(255, 255, 255, 0.08);
}

.tag.host {
  color: #ffc9a0;
  background: rgba(255, 140, 60, 0.15);
}

.tag.playing {
  color: #a8f0c8;
  background: rgba(80, 200, 140, 0.18);
}

.tag.lobby {
  color: #9ecaff;
  background: rgba(88, 166, 255, 0.18);
}

.member-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.member-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-height: 120px;
  padding: 16px 10px;
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.02);
  text-align: center;
}

.member-slot.filled {
  border-style: solid;
  border-color: rgba(88, 166, 255, 0.25);
  background: rgba(88, 166, 255, 0.06);
}

.member-slot.host {
  border-color: rgba(255, 160, 80, 0.45);
  background: rgba(255, 140, 60, 0.08);
}

.member-slot.inviteable {
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.member-slot.inviteable:hover {
  border-color: rgba(120, 200, 140, 0.45);
  background: rgba(120, 200, 140, 0.08);
}

.slot-empty {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.4rem;
  color: rgba(200, 210, 230, 0.35);
  border: 1px dashed rgba(255, 255, 255, 0.15);
}

.member-slot strong {
  font-size: 0.88rem;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.slot-role {
  font-size: 0.72rem;
  color: #ffc9a0;
}

.slot-role.muted {
  color: var(--text-muted);
}

.member-count {
  margin: 14px 0 0;
  text-align: center;
  font-size: 0.82rem;
  color: var(--text-muted);
}

.team-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.no-team {
  padding: 40px 20px;
  text-align: center;
  border-radius: 16px;
  border: 1px dashed rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.02);
}

.no-team p {
  margin: 0 0 8px;
  font-size: 1.05rem;
  font-weight: 600;
}

.no-team span {
  font-size: 0.86rem;
  color: var(--text-muted);
}

@media (max-width: 720px) {
  .member-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
