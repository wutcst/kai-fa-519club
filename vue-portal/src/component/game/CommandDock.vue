<script setup lang="ts">
import GlassButton from '@/component/common/GlassButton.vue'

defineProps<{
  modelValue: string
  busy: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  submit: []
  leave: []
}>()
</script>

<template>
  <footer class="command-dock glass-hud">
    <input
      class="command-input"
      :value="modelValue"
      placeholder="命令：take / use / sleep / help …"
      :disabled="busy"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      @keyup.enter="emit('submit')"
    />
    <GlassButton accent :disabled="busy" @click="emit('submit')">发送</GlassButton>
    <GlassButton danger @click="emit('leave')">离开</GlassButton>
  </footer>
</template>

<style scoped>
.command-dock {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 16px;
  z-index: 25;
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 10px 12px;
}

.command-input {
  flex: 1;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-soft);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
}

.command-input:focus {
  outline: none;
  border-color: rgba(88, 166, 255, 0.5);
}

.glass-hud {
  background: var(--hud-bg-strong);
  border: 1px solid var(--border-soft);
  border-radius: var(--radius);
  backdrop-filter: blur(14px);
}
</style>
