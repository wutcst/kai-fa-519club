<script setup lang="ts">
import { ref, watch } from 'vue'
import { itemImageUrl } from '@/model/assetCatalog'

const props = withDefaults(
  defineProps<{
    name: string
    size?: number
  }>(),
  { size: 40 },
)

const broken = ref(false)

watch(
  () => props.name,
  () => {
    broken.value = false
  },
)
</script>

<template>
  <img
    v-if="!broken"
    class="item-icon"
    :src="itemImageUrl(name)"
    :alt="name"
    :style="{ width: `${size}px`, height: `${size}px` }"
    @error="broken = true"
  />
  <span
    v-else
    class="item-icon-fallback"
    :style="{ width: `${size}px`, height: `${size}px`, fontSize: `${Math.round(size * 0.42)}px` }"
    :title="name"
  >
    {{ name.slice(0, 1) }}
  </span>
</template>

<style scoped>
.item-icon {
  object-fit: contain;
  display: block;
}

.item-icon-fallback {
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-muted);
  font-weight: 600;
}
</style>
