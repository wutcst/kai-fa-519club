<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { GameDisplayMode, MoveDirection } from '@/model/gameDisplayMode'
import { roomImageUrl } from '@/model/assetCatalog'
import PlayerCharacterLayer from '@/component/game/PlayerCharacterLayer.vue'

const props = withDefaults(
  defineProps<{
    roomId: string | null | undefined
    roomDescription: string
    transitioning: boolean
    displayMode?: GameDisplayMode
    moveDirection?: MoveDirection | null
    directionalExit?: boolean
  }>(),
  {
    displayMode: 'classic',
    moveDirection: null,
    directionalExit: false,
  },
)

const imageBroken = ref(false)

const imageSrc = computed(() => roomImageUrl(props.roomId))

const isImmersive = computed(() => props.displayMode === 'immersive')

const transitionName = computed(() => {
  if (!isImmersive.value || !props.moveDirection) {
    return 'room-fade'
  }
  if (props.moveDirection === 'back') {
    return 'room-back'
  }
  return `room-slide-${props.moveDirection}`
})

const directionalActive = computed(
  () => isImmersive.value && (props.directionalExit || props.transitioning) && !!props.moveDirection,
)

watch(
  () => props.roomId,
  () => {
    imageBroken.value = false
  },
)
</script>

<template>
  <div class="scene-root" :class="{ immersive: isImmersive, classic: !isImmersive }">
    <Transition :name="transitionName" mode="out-in">
      <div :key="roomId || 'gate'" class="scene-layer">
        <img
          v-if="!imageBroken"
          :src="imageSrc"
          class="scene-image"
          alt="房间场景"
          @error="imageBroken = true"
        />
        <div v-else class="scene-fallback">{{ roomId || 'gate' }}</div>
      </div>
    </Transition>

    <div class="scene-vignette" />

    <div
      v-if="isImmersive"
      class="scene-directional-vignette"
      :class="[
        moveDirection ? `dir-${moveDirection}` : '',
        { active: directionalActive },
      ]"
    />

    <div
      class="scene-dim"
      :class="{ active: transitioning || (isImmersive && directionalExit) }"
    />

    <PlayerCharacterLayer
      v-if="isImmersive"
      :move-direction="moveDirection"
      :directional-exit="directionalExit"
      :transitioning="transitioning"
    />

    <div class="scene-caption glass-caption">
      <p class="caption-title">{{ roomDescription || '探索中…' }}</p>
    </div>
  </div>
</template>

<style scoped>
.scene-root {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: var(--window-bg);
}

.scene-layer {
  width: 100%;
  height: 100%;
}

.scene-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.scene-root.classic .scene-image {
  animation: sceneKenBurns 18s ease-in-out infinite alternate;
}

.scene-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 1.4rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.scene-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(ellipse at center, transparent 35%, rgba(0, 0, 0, 0.62) 100%);
}

.scene-root.immersive .scene-vignette {
  background: radial-gradient(ellipse at center 38%, transparent 36%, rgba(0, 0, 0, 0.52) 100%);
}

.scene-directional-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.28s ease;
}

.scene-directional-vignette.active {
  opacity: 1;
}

.scene-directional-vignette.dir-north.active {
  background:
    linear-gradient(to bottom, rgba(0, 0, 0, 0.82) 0%, rgba(0, 0, 0, 0.35) 28%, transparent 58%),
    linear-gradient(to right, rgba(0, 0, 0, 0.25) 0%, transparent 18%, transparent 82%, rgba(0, 0, 0, 0.25) 100%);
}

.scene-directional-vignette.dir-south.active {
  background:
    linear-gradient(to top, rgba(0, 0, 0, 0.82) 0%, rgba(0, 0, 0, 0.35) 28%, transparent 58%),
    linear-gradient(to right, rgba(0, 0, 0, 0.25) 0%, transparent 18%, transparent 82%, rgba(0, 0, 0, 0.25) 100%);
}

.scene-directional-vignette.dir-east.active {
  background:
    linear-gradient(to left, rgba(0, 0, 0, 0.82) 0%, rgba(0, 0, 0, 0.35) 28%, transparent 58%),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.25) 0%, transparent 18%, transparent 82%, rgba(0, 0, 0, 0.25) 100%);
}

.scene-directional-vignette.dir-west.active {
  background:
    linear-gradient(to right, rgba(0, 0, 0, 0.82) 0%, rgba(0, 0, 0, 0.35) 28%, transparent 58%),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.25) 0%, transparent 18%, transparent 82%, rgba(0, 0, 0, 0.25) 100%);
}

.scene-directional-vignette.dir-back.active {
  background:
    radial-gradient(ellipse at center, transparent 18%, rgba(0, 0, 0, 0.72) 100%),
    linear-gradient(to bottom, rgba(0, 0, 0, 0.35), rgba(0, 0, 0, 0.35));
}

.scene-dim {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: rgba(0, 0, 0, 0);
  transition: background 0.28s ease;
}

.scene-root.classic .scene-dim.active {
  background: rgba(0, 0, 0, 0.45);
}

.scene-root.immersive .scene-dim.active {
  background: rgba(0, 0, 0, 0.38);
}

.glass-caption {
  position: absolute;
  left: 50%;
  top: 118px;
  transform: translateX(-50%);
  padding: 8px 18px;
  border-radius: 999px;
  background: rgba(10, 12, 20, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.caption-title {
  margin: 0;
  font-size: 0.92rem;
  color: var(--text-primary);
  white-space: nowrap;
  max-width: min(420px, 80vw);
  overflow: hidden;
  text-overflow: ellipsis;
}

.room-fade-enter-active,
.room-fade-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.room-fade-enter-from {
  opacity: 0;
  transform: scale(1.04);
}

.room-fade-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

.room-slide-north-enter-active,
.room-slide-north-leave-active,
.room-slide-south-enter-active,
.room-slide-south-leave-active,
.room-slide-east-enter-active,
.room-slide-east-leave-active,
.room-slide-west-enter-active,
.room-slide-west-leave-active,
.room-back-enter-active,
.room-back-leave-active {
  transition: opacity 0.38s ease, transform 0.38s ease;
}

.room-slide-north-leave-to {
  opacity: 0;
  transform: translateY(10%) scale(1.02);
}

.room-slide-north-enter-from {
  opacity: 0;
  transform: translateY(-10%) scale(0.98);
}

.room-slide-south-leave-to {
  opacity: 0;
  transform: translateY(-10%) scale(1.02);
}

.room-slide-south-enter-from {
  opacity: 0;
  transform: translateY(10%) scale(0.98);
}

.room-slide-east-leave-to {
  opacity: 0;
  transform: translateX(-10%) scale(1.02);
}

.room-slide-east-enter-from {
  opacity: 0;
  transform: translateX(10%) scale(0.98);
}

.room-slide-west-leave-to {
  opacity: 0;
  transform: translateX(10%) scale(1.02);
}

.room-slide-west-enter-from {
  opacity: 0;
  transform: translateX(-10%) scale(0.98);
}

.room-back-leave-to {
  opacity: 0;
  transform: scale(0.94);
}

.room-back-enter-from {
  opacity: 0;
  transform: scale(1.06);
}

@keyframes sceneKenBurns {
  from {
    transform: scale(1);
  }
  to {
    transform: scale(1.06);
  }
}
</style>
