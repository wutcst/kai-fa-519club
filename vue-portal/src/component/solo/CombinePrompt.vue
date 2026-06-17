<script setup lang="ts">
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <Transition name="fade">
    <div v-if="visible" class="combine-layer">
      <div class="combine-card glass-hud">
        <p>背包已有棍子、石头、绳子，是否合成锤子？</p>
        <div class="actions">
          <GlassButton accent @click="emit('confirm')">合成</GlassButton>
          <GlassButton @click="emit('cancel')">稍后</GlassButton>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.combine-layer {
  position: absolute;
  inset: 0;
  z-index: 35;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.4);
}

.combine-card {
  padding: 22px 26px;
  text-align: center;
  max-width: 360px;
}

.actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 16px;
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
}
</style>
