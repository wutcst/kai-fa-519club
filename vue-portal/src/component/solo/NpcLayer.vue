<script setup lang="ts">
import { ref, watch } from 'vue'
import { getNpcDoorAnchor } from '@/model/roomLayoutDefaults'
import { npcImageUrl } from '@/model/assetCatalog'

const props = defineProps<{
  roomId: string
  visible: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  talk: []
}>()

const npcFocused = ref(false)
const imageBroken = ref(false)

const anchor = () => getNpcDoorAnchor(props.roomId)

watch(
  () => props.roomId,
  () => {
    npcFocused.value = false
    imageBroken.value = false
  },
)

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      npcFocused.value = false
    }
  },
)

function onNpcClick() {
  if (props.disabled) {
    return
  }
  npcFocused.value = true
}

function closeFocus() {
  npcFocused.value = false
}

function startTalk() {
  emit('talk')
  npcFocused.value = false
}
</script>

<template>
  <div v-if="visible && anchor()" class="npc-layer">
    <button
      type="button"
      class="npc-marker"
      :class="{ focused: npcFocused }"
      :style="{
        left: `${anchor()!.x * 100}%`,
        top: `${anchor()!.y * 100}%`,
        '--npc-scale': anchor()!.scale ?? 1,
      }"
      :aria-label="anchor()!.title"
      :disabled="disabled"
      @click.stop="onNpcClick"
    >
      <img
        v-if="!imageBroken"
        :src="npcImageUrl(roomId)"
        :alt="anchor()!.title"
        class="npc-image"
        @error="imageBroken = true"
      />
      <span v-else class="npc-fallback">{{ anchor()!.title.slice(0, 1) }}</span>
      <span class="npc-glow" />
    </button>

    <Transition name="npc-pop">
      <div
        v-if="npcFocused"
        class="npc-action-bubble"
        :style="{
          left: `${Math.min(anchor()!.x * 100 + 6, 78)}%`,
          top: `${anchor()!.y * 100}%`,
        }"
      >
        <button type="button" class="talk-btn" @click="startTalk">对话</button>
      </div>
    </Transition>

    <div v-if="npcFocused" class="npc-dismiss-layer" @click="closeFocus" />
  </div>
</template>

<style scoped>
.npc-layer {
  position: absolute;
  inset: 0;
  z-index: 14;
  pointer-events: none;
}

.npc-marker {
  position: absolute;
  transform: translate(-50%, -85%);
  pointer-events: auto;
  width: calc(72px * var(--npc-scale, 1));
  height: calc(88px * var(--npc-scale, 1));
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  animation: npcFloat 3.6s ease-in-out infinite;
}

.npc-marker:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.npc-image {
  width: calc(64px * var(--npc-scale, 1));
  height: calc(80px * var(--npc-scale, 1));
  object-fit: contain;
  display: block;
  filter: drop-shadow(0 0 10px rgba(88, 166, 255, 0.55));
}

.npc-fallback {
  width: calc(64px * var(--npc-scale, 1));
  height: calc(80px * var(--npc-scale, 1));
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(15, 18, 28, 0.7);
  color: var(--accent);
  font-size: 1.4rem;
  font-weight: 700;
}

.npc-glow {
  position: absolute;
  inset: 4px 0 auto;
  height: calc(72px * var(--npc-scale, 1));
  border-radius: 16px;
  border: 1px solid rgba(136, 198, 255, 0.55);
  box-shadow:
    0 0 14px rgba(88, 166, 255, 0.45),
    0 0 28px rgba(88, 166, 255, 0.22);
  pointer-events: none;
  animation: npcGlowPulse 2.4s ease-in-out infinite;
}

.npc-marker.focused .npc-glow {
  border-color: rgba(160, 220, 255, 0.85);
  box-shadow:
    0 0 18px rgba(88, 166, 255, 0.65),
    0 0 36px rgba(88, 166, 255, 0.35);
}

.npc-action-bubble {
  position: absolute;
  transform: translateY(-50%);
  z-index: 3;
  pointer-events: auto;
}

.talk-btn {
  min-width: 68px;
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid rgba(88, 166, 255, 0.5);
  background: rgba(15, 18, 28, 0.88);
  color: #dbeaff;
  font: inherit;
  font-size: 0.82rem;
  cursor: pointer;
  backdrop-filter: blur(8px);
  box-shadow: 0 0 16px rgba(88, 166, 255, 0.35);
}

.talk-btn:hover {
  background: rgba(88, 166, 255, 0.28);
}

.npc-dismiss-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
  pointer-events: auto;
}

.npc-pop-enter-active,
.npc-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.npc-pop-enter-from,
.npc-pop-leave-to {
  opacity: 0;
  transform: translateY(-50%) translateX(-8px);
}

@keyframes npcFloat {
  0%,
  100% {
    transform: translate(-50%, -85%);
  }
  50% {
    transform: translate(-50%, calc(-85% - 5px));
  }
}

@keyframes npcGlowPulse {
  0%,
  100% {
    opacity: 0.75;
  }
  50% {
    opacity: 1;
  }
}
</style>
