<script setup lang="ts">
import type { FriendView } from '@/model/types'
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  friends: FriendView[]
  friendUsername: string
  loading: boolean
  canInvite: boolean
}>()

const emit = defineEmits<{
  'update:friendUsername': [value: string]
  add: []
  invite: [friend: FriendView]
}>()

const statusClass: Record<string, string> = {
  OFFLINE: 'status-offline',
  ONLINE: 'status-online',
  SOLO_PLAYING: 'status-solo',
  IN_ROOM: 'status-room',
  MULTIPLAYER_PLAYING: 'status-mp',
}
</script>

<template>
  <GlassPanel strong padding="22px 24px" class="friends-panel">
    <div class="panel-head">
      <h2>好友</h2>
      <p class="muted">查看状态并邀请进房</p>
    </div>

    <div class="add-row">
      <input
        :value="friendUsername"
        maxlength="32"
        placeholder="输入好友用户名"
        @input="emit('update:friendUsername', ($event.target as HTMLInputElement).value)"
      />
      <GlassButton :disabled="loading" @click="emit('add')">添加</GlassButton>
    </div>

    <ul v-if="friends.length" class="friend-list">
      <li v-for="friend in friends" :key="friend.userId" class="friend-item">
        <div class="friend-main">
          <strong>{{ friend.displayName }}</strong>
          <span class="friend-user">@{{ friend.username }}</span>
          <span class="status" :class="statusClass[friend.status] ?? 'status-offline'">
            {{ friend.statusLabel }}
          </span>
        </div>
        <GlassButton
          v-if="canInvite"
          :disabled="loading || friend.status === 'OFFLINE'"
          @click="emit('invite', friend)"
        >
          邀请
        </GlassButton>
      </li>
    </ul>
    <p v-else class="empty">暂无好友，先添加同学账号吧</p>
  </GlassPanel>
</template>

<style scoped>
.friends-panel {
  border: 1px solid rgba(120, 200, 140, 0.2);
}

.panel-head h2 {
  margin: 0;
  font-size: 1.12rem;
}

.muted {
  margin: 4px 0 14px;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.add-row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.add-row input {
  flex: 1;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
}

.friend-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 360px;
  overflow: auto;
}

.friend-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
}

.friend-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.friend-user {
  font-size: 0.76rem;
  color: var(--text-muted);
}

.status {
  display: inline-block;
  margin-top: 4px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.72rem;
}

.status-offline { background: rgba(120, 120, 120, 0.2); color: #bbb; }
.status-online { background: rgba(88, 166, 255, 0.18); color: #9ecaff; }
.status-solo { background: rgba(255, 180, 80, 0.18); color: #ffc9a0; }
.status-room { background: rgba(140, 120, 255, 0.18); color: #c9b8ff; }
.status-mp { background: rgba(80, 200, 140, 0.18); color: #a8f0c8; }

.empty {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.86rem;
}
</style>
