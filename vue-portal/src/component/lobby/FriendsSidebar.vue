<script setup lang="ts">
import { ref } from 'vue'
import type { FriendRequest, FriendView, TeamRoom } from '@/model/types'
import UserAvatar from '@/component/common/UserAvatar.vue'
import GlassButton from '@/component/common/GlassButton.vue'
import { canInviteFriendToTeam, inviteFriendTitle, isFriendInTeam } from '@/util/friendInvite'

const props = defineProps<{
  friends: FriendView[]
  friendRequests: FriendRequest[]
  friendUsername: string
  teamRoom: TeamRoom | null
  loading: boolean
  canInvite: boolean
}>()

const emit = defineEmits<{
  'update:friendUsername': [value: string]
  add: []
  invite: [friend: FriendView]
  acceptRequest: [request: FriendRequest]
  rejectRequest: [request: FriendRequest]
}>()

const expanded = ref(true)

const statusClass: Record<string, string> = {
  OFFLINE: 'status-offline',
  ONLINE: 'status-online',
  SOLO_PLAYING: 'status-solo',
  IN_ROOM: 'status-room',
  MULTIPLAYER_PLAYING: 'status-mp',
}

function canInviteFriend(friend: FriendView) {
  return canInviteFriendToTeam(friend, props.teamRoom)
}

function inviteTitle(friend: FriendView) {
  return inviteFriendTitle(friend, props.teamRoom)
}
</script>

<template>
  <aside class="friends-sidebar" :class="{ collapsed: !expanded }">
    <button
      type="button"
      class="sidebar-toggle"
      :aria-label="expanded ? '收起好友栏' : '展开好友栏'"
      @click="expanded = !expanded"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true" :class="{ flipped: !expanded }">
        <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
      </svg>
      <span v-if="!expanded" class="toggle-label">好友</span>
    </button>

    <div v-show="expanded" class="sidebar-body">
      <header class="sidebar-head">
        <h2>好友</h2>
        <p>发送申请、处理邀请并邀请进队</p>
      </header>

      <div v-if="friendRequests.length" class="request-block">
        <p class="block-title">好友申请</p>
        <ul class="request-list">
          <li v-for="request in friendRequests" :key="request.userId" class="request-item">
            <UserAvatar
              :display-name="request.displayName"
              :avatar-url="request.avatarUrl"
              :user-id="request.userId"
              :size="32"
            />
            <div class="request-main">
              <strong>{{ request.displayName }}</strong>
              <span class="friend-user">@{{ request.username }}</span>
            </div>
            <div class="request-actions">
              <button type="button" class="accept-btn" :disabled="loading" @click="emit('acceptRequest', request)">
                接受
              </button>
              <button type="button" class="reject-btn" :disabled="loading" @click="emit('rejectRequest', request)">
                拒绝
              </button>
            </div>
          </li>
        </ul>
      </div>

      <div class="add-row">
        <input
          :value="friendUsername"
          maxlength="32"
          placeholder="用户名"
          @input="emit('update:friendUsername', ($event.target as HTMLInputElement).value)"
          @keyup.enter="emit('add')"
        />
        <GlassButton :disabled="loading" @click="emit('add')">申请</GlassButton>
      </div>

      <ul v-if="friends.length" class="friend-list">
        <li v-for="friend in friends" :key="friend.userId" class="friend-item">
          <UserAvatar
            :display-name="friend.displayName"
            :avatar-url="friend.avatarUrl"
            :user-id="friend.userId"
          />
          <div class="friend-main">
            <strong>{{ friend.displayName }}</strong>
            <span class="friend-user">@{{ friend.username }}</span>
            <span class="status" :class="statusClass[friend.status] ?? 'status-offline'">
              {{ isFriendInTeam(friend, teamRoom) ? '本队中' : friend.statusLabel }}
            </span>
          </div>
          <button
            v-if="canInvite"
            type="button"
            class="invite-btn"
            :disabled="loading || !canInviteFriend(friend)"
            :title="inviteTitle(friend)"
            @click="emit('invite', friend)"
          >
            邀请
          </button>
        </li>
      </ul>
      <p v-else class="empty">暂无好友，发送申请后需对方同意</p>
    </div>
  </aside>
</template>

<style scoped>
.friends-sidebar {
  position: relative;
  flex-shrink: 0;
  width: 280px;
  height: 100%;
  border-right: 1px solid rgba(120, 200, 140, 0.15);
  background: rgba(8, 14, 24, 0.82);
  backdrop-filter: blur(14px);
  transition: width 0.28s ease;
  display: flex;
  flex-direction: column;
  z-index: 2;
}

.friends-sidebar.collapsed {
  width: 52px;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 14px 12px;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(120, 200, 140, 0.06);
  color: #b8e8c8;
  cursor: pointer;
  font: inherit;
  font-size: 0.85rem;
}

.sidebar-toggle svg {
  width: 20px;
  height: 20px;
  transition: transform 0.28s ease;
}

.sidebar-toggle svg.flipped {
  transform: rotate(180deg);
}

.toggle-label {
  writing-mode: vertical-rl;
  letter-spacing: 0.12em;
  font-size: 0.78rem;
}

.sidebar-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  overflow: hidden;
  min-height: 0;
}

.sidebar-head h2 {
  margin: 0;
  font-size: 1.05rem;
}

.sidebar-head p {
  margin: 4px 0 14px;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.request-block {
  margin-bottom: 12px;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 200, 120, 0.25);
  background: rgba(255, 180, 80, 0.06);
}

.block-title {
  margin: 0 0 8px;
  font-size: 0.75rem;
  color: #ffc9a0;
  letter-spacing: 0.06em;
}

.request-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.request-item {
  display: grid;
  grid-template-columns: 32px 1fr auto;
  gap: 8px;
  align-items: center;
}

.request-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.request-main strong {
  font-size: 0.84rem;
}

.request-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.accept-btn,
.reject-btn {
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 0.7rem;
  cursor: pointer;
  border: 1px solid transparent;
}

.accept-btn {
  background: rgba(120, 200, 140, 0.18);
  border-color: rgba(120, 200, 140, 0.35);
  color: #b8e8c8;
}

.reject-btn {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 255, 255, 0.1);
  color: var(--text-muted);
}

.accept-btn:disabled,
.reject-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.add-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.add-row input {
  flex: 1;
  min-width: 0;
  padding: 9px 11px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-primary);
  font: inherit;
  font-size: 0.86rem;
}

.friend-list {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-item {
  display: grid;
  grid-template-columns: 36px 1fr auto;
  gap: 10px;
  align-items: center;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.03);
}

.friend-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.friend-main strong {
  font-size: 0.88rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.invite-btn {
  padding: 5px 10px;
  border-radius: 8px;
  border: 1px solid rgba(120, 200, 140, 0.35);
  background: rgba(120, 200, 140, 0.12);
  color: #b8e8c8;
  font-size: 0.72rem;
  cursor: pointer;
}

.invite-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.empty {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.84rem;
}

@media (max-width: 900px) {
  .friends-sidebar {
    width: 100%;
    height: auto;
    max-height: 34vh;
    flex-shrink: 0;
    border-right: none;
    border-bottom: 1px solid rgba(120, 200, 140, 0.15);
  }

  .friends-sidebar.collapsed {
    width: 100%;
    max-height: none;
  }

  .friends-sidebar.collapsed .sidebar-body {
    display: none;
  }
}
</style>
