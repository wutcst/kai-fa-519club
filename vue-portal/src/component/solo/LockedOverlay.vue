<script setup lang="ts">
defineProps<{
  message: string
}>()

const emit = defineEmits<{
  dismiss: []
}>()
</script>

<template>
  <div class="locked-layer" @click="emit('dismiss')">
    <div class="locked-card">
      <p>{{ message }}</p>
      <span class="hint">点击任意处继续</span>
    </div>
  </div>
</template>

<style scoped>
.locked-layer {
  position: absolute;
  inset: 0;
  z-index: 45;
  background: rgba(0, 0, 0, 0.82);
  display: grid;
  place-items: center;
  padding: 32px;
  cursor: pointer;
}

.locked-card {
  text-align: center;
  max-width: 420px;
}

.locked-card p {
  margin: 0 0 16px;
  font-size: 1.05rem;
  line-height: 1.6;
}

.hint {
  color: var(--text-muted);
  font-size: 0.82rem;
}
</style>
