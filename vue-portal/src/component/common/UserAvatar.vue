<script setup lang="ts">
import { computed } from 'vue'
import { resolveMediaUrl } from '@/model/authModel'

const props = withDefaults(
  defineProps<{
    displayName: string
    avatarUrl?: string | null
    userId?: number
    size?: number
  }>(),
  { size: 36 },
)

const src = computed(() => resolveMediaUrl(props.avatarUrl, props.userId))
const initial = computed(() => (props.displayName || '?').slice(0, 1))
</script>

<template>
  <div class="user-avatar" :style="{ width: `${size}px`, height: `${size}px` }">
    <img v-if="src" :src="src" :alt="displayName" class="avatar-img" />
    <span v-else class="avatar-fallback">{{ initial }}</span>
  </div>
</template>

<style scoped>
.user-avatar {
  flex-shrink: 0;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(88, 166, 255, 0.35), rgba(120, 200, 140, 0.25));
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  font-weight: 700;
  font-size: 0.9rem;
  color: #dbeaff;
}
</style>
