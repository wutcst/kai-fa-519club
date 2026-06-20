<script setup lang="ts">
defineProps<{
  accent?: boolean
  danger?: boolean
  disabled?: boolean
}>()
</script>

<template>
  <button
    type="button"
    class="glass-btn"
    :class="{ accent, danger }"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>

<style scoped>
.glass-btn {
  appearance: none;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border-radius: 999px;
  padding: 9px 16px;
  font: inherit;
  font-size: 0.88rem;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease, transform 0.12s ease;
}

.glass-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.24);
}

.glass-btn:active:not(:disabled) {
  transform: scale(0.97);
}

.glass-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.glass-btn.accent {
  background: rgba(88, 166, 255, 0.2);
  border-color: rgba(88, 166, 255, 0.45);
  color: #dbeaff;
}

.glass-btn.danger {
  background: rgba(220, 90, 90, 0.14);
  border-color: rgba(220, 90, 90, 0.35);
  color: #ffc9c9;
}
</style>
