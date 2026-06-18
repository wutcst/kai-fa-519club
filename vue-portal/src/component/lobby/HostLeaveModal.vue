<script setup lang="ts">
import type { RoomMember } from '@/model/types'
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  visible: boolean
  members: RoomMember[]
  fromGame?: boolean
}>()

const emit = defineEmits<{
  close: []
  dissolve: []
  transfer: [playerId: string]
  returnLobby: []
}>()
</script>

<template>
  <div v-if="visible" class="modal-backdrop" @click.self="emit('close')">
    <div class="modal-card" role="dialog" aria-labelledby="host-leave-title">
      <h3 id="host-leave-title">房主离开</h3>
      <p class="hint">
        {{ fromGame ? '返回组队界面后，所有队员将同步回到组队页面。' : '请选择如何处理当前房间：' }}
      </p>

      <div v-if="fromGame" class="actions">
        <GlassButton accent @click="emit('returnLobby')">返回组队界面</GlassButton>
        <GlassButton danger @click="emit('dissolve')">解散房间</GlassButton>
        <GlassButton @click="emit('close')">取消</GlassButton>
      </div>

      <template v-else>
        <div v-if="members.length" class="transfer-list">
          <p class="sub-hint">或选择新房主后离开：</p>
          <button
            v-for="member in members"
            :key="member.playerId"
            type="button"
            class="member-btn"
            @click="emit('transfer', member.playerId)"
          >
            转移给 {{ member.displayName }}
          </button>
        </div>
        <div class="actions">
          <GlassButton danger @click="emit('dissolve')">解散房间</GlassButton>
          <GlassButton @click="emit('close')">取消</GlassButton>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(6, 8, 14, 0.72);
  backdrop-filter: blur(4px);
}

.modal-card {
  width: min(420px, calc(100vw - 32px));
  padding: 24px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(14, 18, 30, 0.96);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.modal-card h3 {
  margin: 0 0 10px;
  font-size: 1.15rem;
}

.hint,
.sub-hint {
  margin: 0 0 14px;
  color: var(--text-muted);
  font-size: 0.88rem;
  line-height: 1.5;
}

.transfer-list {
  margin-bottom: 14px;
}

.member-btn {
  display: block;
  width: 100%;
  margin-top: 8px;
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid rgba(88, 166, 255, 0.3);
  background: rgba(88, 166, 255, 0.08);
  color: #dbeaff;
  font: inherit;
  cursor: pointer;
  text-align: left;
}

.member-btn:hover {
  background: rgba(88, 166, 255, 0.16);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
