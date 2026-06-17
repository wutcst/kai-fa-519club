<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { RouterView } from 'vue-router'
import GameBgmControl from '@/component/game/GameBgmControl.vue'

const route = useRoute()
const immersive = computed(() =>
  route.name === 'solo' || route.name === 'multiplayer-room',
)
</script>

<template>
  <div class="app-root" :class="{ immersive }">
    <RouterView />
    <GameBgmControl />
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
}

.app-root.immersive {
  overflow: hidden;
}
</style>
