<script setup lang="ts">
import { computed } from 'vue'
import type { FriendView, TeamRoom } from '@/model/types'
import UserAvatar from '@/component/common/UserAvatar.vue'
import GlassButton from '@/component/common/GlassButton.vue'
import { canInviteFriendToTeam, inviteFriendTitle } from '@/util/friendInvite'

const props = defineProps<{
  visible: boolean
  friends: FriendView[]
  teamRoom: TeamRoom | null
  loading: boolean
}>()

const emit = defineEmits<{
  close: []
  invite: [friend: FriendView]
}>()

const statusClass: Record<string, string> = {
  OFFLINE: 'status-offline',
  ONLINE: 'status-online',
  SOLO_PLAYING: 'status-solo',
  IN_ROOM: 'status-room',
  MULTIPLAYER_PLAYING: 'status-mp',
}

function canInvite(friend: FriendView) {
  return canInviteFriendToTeam(friend, props.teamRoom)
}

const inviteableFriends = computed(() =>
  props.friends.filter((friend) => canInvite(friend)),
)

function inviteHint(friend: FriendView) {
  return inviteFriendTitle(friend, props.teamRoom)
}
</script>

<template>
  <Transition name="fade">
    <div v-if="visible" class="modal-layer" @click.self="emit('close')">
      <div class="modal-card glass-hud" role="dialog" aria-labelledby="invite-title">
        <header class="modal-head">
          <h3 id="invite-title">邀请好友加入</h3>
          <p>选择在线好友发送组队邀请</p>
        </header>

        <ul v-if="inviteableFriends.length" class="friend-pick-list">
          <li v-for="friend in inviteableFriends" :key="friend.userId" class="friend-pick-item">
            <UserAvatar :display-name="friend.displayName" :avatar-url="friend.avatarUrl" :user-id="friend.userId" />
            <div class="friend-pick-main">
              <strong>{{ friend.displayName }}</strong>
              <span class="friend-user">@{{ friend.username }}</span>
              <span class="status" :class="statusClass[friend.status] ?? 'status-offline'">
                {{ friend.statusLabel }}
              </span>
            </div>
            <GlassButton accent :disabled="loading" @click="emit('invite', friend)">邀请</GlassButton>
          </li>
        </ul>

        <div v-else class="empty">
          <p>暂无可邀请的在线好友</p>
          <span v-if="friends.length">好友需在在线且未在本队中</span>
          <span v-else>请先在左侧发送好友申请并等待对方同意</span>
        </div>

        <ul v-if="friends.length && !inviteableFriends.length" class="blocked-list">
          <li v-for="friend in friends" :key="friend.userId" class="blocked-item">
            <UserAvatar :display-name="friend.displayName" :avatar-url="friend.avatarUrl" :user-id="friend.userId" :size="28" />
            <span>{{ friend.displayName }}</span>
            <span class="blocked-hint">{{ inviteHint(friend) }}</span>
          </li>
        </ul>

        <div class="modal-actions">
          <GlassButton :disabled="loading" @click="emit('close')">关闭</GlassButton>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-layer {
  position: fixed;
  inset: 0;
  z-index: 60;
  display: grid;
  place-items: center;
  background: rgba(4, 6, 12, 0.62);
  backdrop-filter: blur(4px);
}

.modal-card {
  width: min(420px, calc(100vw - 32px));
  max-height: min(70vh, 520px);
  display: flex;
  flex-direction: column;
  padding: 20px 22px 18px;
}

.modal-head h3 {
  margin: 0 0 4px;
  font-size: 1.05rem;
}

.modal-head p {
  margin: 0 0 14px;
  font-size: 0.84rem;
  color: var(--text-muted);
}

.friend-pick-list,
.blocked-list {
  list-style: none;
  margin: 0;
  padding: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-pick-item {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
}

.friend-pick-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.friend-pick-main strong {
  font-size: 0.88rem;
}

.friend-user {
  font-size: 0.72rem;
  color: var(--text-muted);
}

.status {
  display: inline-block;
  margin-top: 2px;
  padding: 1px 7px;
  border-radius: 999px;
  font-size: 0.68rem;
  width: fit-content;
}

.status-offline { background: rgba(120, 120, 120, 0.2); color: #aaa; }
.status-online { background: rgba(88, 166, 255, 0.18); color: #9ecaff; }
.status-solo { background: rgba(255, 180, 80, 0.18); color: #ffc9a0; }
.status-room { background: rgba(140, 120, 255, 0.18); color: #c9b8ff; }
.status-mp { background: rgba(80, 200, 140, 0.18); color: #a8f0c8; }

.empty {
  padding: 20px 12px;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.86rem;
}

.empty p {
  margin: 0 0 6px;
  color: var(--text-primary);
}

.blocked-list {
  margin-top: 10px;
  opacity: 0.75;
}

.blocked-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  padding: 4px 0;
}

.blocked-hint {
  margin-left: auto;
  font-size: 0.72rem;
  color: var(--text-muted);
}

.modal-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
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
