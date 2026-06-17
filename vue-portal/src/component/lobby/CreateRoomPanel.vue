<script setup lang="ts">
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  displayName: string
  roomName: string
  loading: boolean
}>()

const emit = defineEmits<{
  'update:displayName': [value: string]
  'update:roomName': [value: string]
  create: []
}>()
</script>

<template>
  <GlassPanel strong padding="22px 24px" class="create-panel">
    <div class="panel-head">
      <svg class="panel-icon" viewBox="0 0 48 48" aria-hidden="true">
        <rect x="8" y="14" width="32" height="24" rx="4" fill="none" stroke="currentColor" stroke-width="2.2" />
        <path d="M16 14V10h16v4" fill="none" stroke="currentColor" stroke-width="2.2" />
        <path d="M24 22v8M20 26h8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
      </svg>
      <div>
        <h2>创建房间</h2>
        <p class="muted">成为房主，邀请队友一起赶归寝</p>
      </div>
    </div>

    <label class="field">
      <span>你的昵称</span>
      <input
        :value="displayName"
        maxlength="20"
        placeholder="例如：刘晶"
        @input="emit('update:displayName', ($event.target as HTMLInputElement).value)"
      />
    </label>
    <label class="field">
      <span>房间名称</span>
      <input
        :value="roomName"
        maxlength="24"
        placeholder="例如：519 夜归小队"
        @input="emit('update:roomName', ($event.target as HTMLInputElement).value)"
      />
    </label>

    <ul class="tips">
      <li>最多 4 人同房间</li>
      <li>共享熄灯倒计时</li>
    </ul>

    <GlassButton accent class="cta" :disabled="loading" @click="emit('create')">
      {{ loading ? '创建中…' : '创建并进入' }}
    </GlassButton>
  </GlassPanel>
</template>

<style scoped>
.create-panel {
  border: 1px solid rgba(255, 160, 80, 0.18);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
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

.cta {
  width: 100%;
}
</style>
