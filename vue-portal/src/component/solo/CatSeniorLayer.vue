<script setup lang="ts">
import { ref, watch } from 'vue'
import { CAT_SENIOR_ANCHOR } from '@/model/roomLayoutDefaults'
import { itemImageUrl } from '@/model/assetCatalog'

const props = defineProps<{
  visible: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  feed: []
}>()

const focused = ref(false)
const imageBroken = ref(false)

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      focused.value = false
    }
  },
)

function onCatClick() {
  if (props.disabled) {
    return
  }
  focused.value = true
}

function closeFocus() {
  focused.value = false
}

function startFeed() {
  emit('feed')
  focused.value = false
}
</script>

<template>
  <div v-if="visible" class="cat-layer">
    <button
      type="button"
      class="cat-marker"
      :class="{ focused }"
      :style="{
        left: `${CAT_SENIOR_ANCHOR.x * 100}%`,
        top: `${CAT_SENIOR_ANCHOR.y * 100}%`,
      }"
      aria-label="猫学长"
      :disabled="disabled"
      @click.stop="onCatClick"
    >
      <img
        v-if="!imageBroken"
        :src="itemImageUrl('一张猫学长的照片')"
        alt="猫学长"
        class="cat-image"
        @error="imageBroken = true"
      />
      <span v-else class="cat-fallback">猫</span>
      <span class="cat-glow" />
    </button>

    <Transition name="cat-pop">
      <div
        v-if="focused"
        class="cat-action-bubble"
        :style="{
          left: `${Math.min(CAT_SENIOR_ANCHOR.x * 100 + 8, 82)}%`,
          top: `${CAT_SENIOR_ANCHOR.y * 100}%`,
        }"
      >
        <button type="button" class="feed-btn" @click="startFeed">喂养</button>
      </div>
    </Transition>

    <div v-if="focused" class="cat-dismiss-layer" @click="closeFocus" />
  </div>
</template>

<style scoped>
.cat-layer {
  position: absolute;
  inset: 0;
  z-index: 15;
  pointer-events: none;
}

.cat-marker {
  position: absolute;
  transform: translate(-50%, -78%);
  pointer-events: auto;
  width: 58px;
  height: 58px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  animation: catFloat 3.2s ease-in-out infinite;
}

.cat-marker:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.cat-image {
  width: 52px;
  height: 52px;
  object-fit: contain;
  display: block;
  border-radius: 10px;
  filter: drop-shadow(0 0 10px rgba(255, 190, 120, 0.55));
}

.cat-fallback {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: rgba(15, 18, 28, 0.7);
  color: #ffd4a8;
  font-size: 1rem;
  font-weight: 700;
}

.cat-glow {
  position: absolute;
  inset: 2px;
  border-radius: 12px;
  border: 1px solid rgba(255, 190, 120, 0.45);
  box-shadow:
    0 0 12px rgba(255, 170, 90, 0.35),
    0 0 24px rgba(255, 170, 90, 0.18);
  pointer-events: none;
  animation: catGlowPulse 2.4s ease-in-out infinite;
}

.cat-marker.focused .cat-glow {
  border-color: rgba(255, 210, 150, 0.85);
}

.cat-action-bubble {
  position: absolute;
  transform: translateY(-50%);
  z-index: 3;
  pointer-events: auto;
}

.feed-btn {
  min-width: 68px;
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(255, 180, 100, 0.5);
  background: rgba(15, 18, 28, 0.88);
  color: #ffe2c4;
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
  backdrop-filter: blur(8px);
  box-shadow: 0 0 16px rgba(255, 170, 90, 0.28);
}

.feed-btn:hover {
  background: rgba(255, 170, 90, 0.22);
}

.cat-dismiss-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: auto;
}

.cat-pop-enter-active,
.cat-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.cat-pop-enter-from,
.cat-pop-leave-to {
  opacity: 0;
  transform: translateY(-50%) translateX(-8px);
}

@keyframes catFloat {
  0%,
  100% {
    transform: translate(-50%, -78%);
  }
  50% {
    transform: translate(-50%, calc(-78% - 4px));
  }
}

@keyframes catGlowPulse {
  0%,
  100% {
    opacity: 0.75;
  }
  50% {
    opacity: 1;
  }
}
</style>
