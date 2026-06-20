<script setup lang="ts">
import type { AuthSession } from '@/model/authModel'
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  accountDisplayName: AuthSession | null
  roomName: string
  loading: boolean
  error?: string
  compact?: boolean
}>()

const emit = defineEmits<{
  'update:roomName': [value: string]
  create: []
}>()
</script>

<template>
  <GlassPanel strong :padding="compact ? '16px 18px' : '22px 24px'" class="create-panel" :class="{ compact }">
    <div class="panel-head">
      <svg v-if="!compact" class="panel-icon" viewBox="0 0 48 48" aria-hidden="true">
        <rect x="8" y="14" width="32" height="24" rx="4" fill="none" stroke="currentColor" stroke-width="2.2" />
        <path d="M16 14V10h16v4" fill="none" stroke="currentColor" stroke-width="2.2" />
        <path d="M24 22v8M20 26h8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
      </svg>
      <div>
        <h2>创建房间</h2>
        <p v-if="!compact" class="muted">成为房主，邀请队友一起赶归寝</p>
      </div>
    </div>

    <div class="account-badge" :class="{ inline: compact }">
      <span v-if="!compact" class="badge-label">联机昵称（已绑定账号）</span>
      <strong class="badge-name">{{ accountDisplayName?.displayName ?? '未登录' }}</strong>
      <span v-if="accountDisplayName" class="badge-user">@{{ accountDisplayName.username }}</span>
    </div>

    <label class="field">
      <span>房间名称</span>
      <input
        :value="roomName"
        maxlength="24"
        placeholder="例如：519 夜归小队"
        @input="emit('update:roomName', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <ul v-if="!compact" class="tips">
      <li>最多 4 人同房间</li>
      <li>房主离开大厅将自动解散空房</li>
      <li>共享熄灯倒计时</li>
    </ul>
    <p v-else class="tips-compact">最多 4 人 · 共享熄灯倒计时</p>

    <GlassButton accent class="cta" :disabled="loading" @click="emit('create')">
      {{ loading ? '创建中…' : '创建房间' }}
    </GlassButton>

    <p v-if="error" class="error">{{ error }}</p>
  </GlassPanel>
</template>

<style scoped>
.create-panel {
  border: 1px solid rgba(255, 160, 80, 0.18);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.create-panel.compact .panel-head {
  margin-bottom: 12px;
}

.create-panel.compact .panel-head h2 {
  font-size: 1rem;
}

.panel-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.panel-icon {
  width: 44px;
  height: 44px;
  color: #ffb080;
  flex-shrink: 0;
}

.panel-head h2 {
  margin: 0;
  font-size: 1.12rem;
}

.muted {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 0.84rem;
}

.account-badge {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(88, 166, 255, 0.28);
  background: rgba(88, 166, 255, 0.08);
}

.badge-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.badge-name {
  font-size: 1.05rem;
  color: #dbeaff;
}

.badge-user {
  font-size: 0.78rem;
  color: var(--text-muted);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 0.82rem;
  color: var(--text-muted);
}

.field input {
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.field input:focus {
  outline: none;
  border-color: rgba(255, 160, 80, 0.45);
  box-shadow: 0 0 0 3px rgba(255, 140, 60, 0.1);
}

.tips {
  margin: 0 0 16px;
  padding-left: 18px;
  color: var(--text-muted);
  font-size: 0.8rem;
  line-height: 1.6;
}

.account-badge.inline {
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px 10px;
  margin-bottom: 10px;
  padding: 8px 12px;
}

.tips-compact {
  margin: 0 0 12px;
  font-size: 0.76rem;
  color: var(--text-muted);
}

.cta {
  width: 100%;
  margin-top: auto;
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
