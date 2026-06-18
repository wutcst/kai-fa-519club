<script setup lang="ts">
import { computed } from 'vue'
import type { MoveDirection } from '@/model/gameDisplayMode'

const props = withDefaults(
  defineProps<{
    moveDirection?: MoveDirection | null
    directionalExit?: boolean
    transitioning?: boolean
  }>(),
  {
    moveDirection: null,
    directionalExit: false,
    transitioning: false,
  },
)

const isWalking = computed(
  () => props.directionalExit || (props.transitioning && !!props.moveDirection),
)

/** 过肩视角：角色始终背对镜头，仅按移动方向微调肩线/位移 */
const strideClass = computed(() => {
  if (!isWalking.value) {
    return 'stride-idle'
  }
  const dir = props.moveDirection ?? 'north'
  if (dir === 'back') {
    return 'stride-south'
  }
  return `stride-${dir}`
})
</script>

<template>
  <div class="player-layer" aria-hidden="true">
    <div class="player-floor" />

    <div class="player-anchor" :class="[isWalking ? 'motion-walk' : 'motion-idle', strideClass]">
      <div class="player-body-wrap">
        <svg
          class="player-sprite"
          viewBox="0 0 120 160"
          xmlns="http://www.w3.org/2000/svg"
          role="img"
          aria-label="玩家角色背影"
        >
          <defs>
            <linearGradient id="player-hair-back" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#4a3220" />
              <stop offset="100%" stop-color="#2a1a10" />
            </linearGradient>
            <linearGradient id="player-blazer-back" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stop-color="#3d6298" />
              <stop offset="100%" stop-color="#243d66" />
            </linearGradient>
            <linearGradient id="player-bag-back" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#7a6040" />
              <stop offset="100%" stop-color="#4f3d24" />
            </linearGradient>
          </defs>

          <g class="player-legs">
            <g class="leg leg-left">
              <rect x="43" y="114" width="15" height="26" rx="6" fill="#1e2a3d" />
              <rect x="41" y="136" width="19" height="10" rx="4" fill="#141c28" />
            </g>
            <g class="leg leg-right">
              <rect x="62" y="114" width="15" height="26" rx="6" fill="#1e2a3d" />
              <rect x="60" y="136" width="19" height="10" rx="4" fill="#141c28" />
            </g>
          </g>

          <g class="player-torso-back">
            <path
              d="M30 74 C30 64 42 58 60 58 C78 58 90 64 90 74 L92 108 C92 116 84 122 60 122 C36 122 28 116 28 108 Z"
              fill="url(#player-blazer-back)"
            />
            <path
              d="M46 58 C52 54 68 54 74 58"
              fill="none"
              stroke="#8aaee0"
              stroke-width="3"
              stroke-linecap="round"
            />
            <rect x="22" y="84" width="12" height="20" rx="5" fill="#2f5088" />
            <rect x="86" y="84" width="12" height="20" rx="5" fill="#2f5088" />
          </g>

          <g class="player-backpack">
            <rect x="42" y="72" width="36" height="42" rx="10" fill="url(#player-bag-back)" />
            <rect x="48" y="78" width="24" height="8" rx="3" fill="#9a8058" />
            <rect x="56" y="66" width="8" height="12" rx="3" fill="#5c4728" />
            <rect x="50" y="92" width="20" height="3" rx="1.5" fill="#6b5434" opacity="0.8" />
          </g>

          <g class="player-head-back">
            <ellipse cx="60" cy="40" rx="27" ry="29" fill="url(#player-hair-back)" />
            <ellipse cx="60" cy="52" rx="18" ry="10" fill="#ffe8d6" opacity="0.35" />
            <path
              d="M34 28 C38 14 48 8 60 8 C72 8 82 14 86 28"
              fill="none"
              stroke="#241810"
              stroke-width="2"
              opacity="0.35"
            />
          </g>
        </svg>
      </div>
    </div>
  </div>
</template>

<style scoped>
.player-layer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: min(22vh, 190px);
  min-height: 132px;
  pointer-events: none;
  z-index: 8;
}

.player-floor {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 72%;
  background: linear-gradient(
    to top,
    rgba(6, 8, 14, 0.88) 0%,
    rgba(6, 8, 14, 0.45) 42%,
    transparent 100%
  );
}

.player-anchor {
  position: absolute;
  left: 50%;
  bottom: 6px;
  transform: translateX(-50%);
  transform-origin: center bottom;
  filter: drop-shadow(0 8px 18px rgba(0, 0, 0, 0.45));
}

.player-body-wrap {
  transform-origin: center bottom;
}

.player-sprite {
  width: min(118px, 16vw);
  height: auto;
  display: block;
}

.motion-idle {
  animation: player-idle 2.4s ease-in-out infinite;
}

.motion-idle .player-head-back {
  animation: player-head-sway 2.4s ease-in-out infinite;
}

.motion-walk {
  animation: player-walk-bob 0.34s ease-in-out infinite;
}

.motion-walk .leg-left {
  animation: player-leg-left 0.34s ease-in-out infinite;
}

.motion-walk .leg-right {
  animation: player-leg-right 0.34s ease-in-out infinite;
}

.stride-idle .player-body-wrap {
  transform: scale(1);
}

.stride-north .player-body-wrap {
  transform: scale(0.94) translateY(-2px);
}

.stride-south .player-body-wrap {
  transform: scale(1.04) translateY(2px);
}

.stride-east .player-body-wrap {
  transform: translateX(5px) rotate(4deg);
}

.stride-west .player-body-wrap {
  transform: translateX(-5px) rotate(-4deg);
}

@keyframes player-idle {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-4px);
  }
}

@keyframes player-head-sway {
  0%,
  100% {
    transform: rotate(0deg);
    transform-origin: 60px 44px;
  }
  50% {
    transform: rotate(-1.5deg);
    transform-origin: 60px 44px;
  }
}

@keyframes player-walk-bob {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-6px);
  }
}

@keyframes player-leg-left {
  0%,
  100% {
    transform: rotate(0deg);
    transform-origin: 50px 114px;
  }
  50% {
    transform: rotate(14deg);
    transform-origin: 50px 114px;
  }
}

@keyframes player-leg-right {
  0%,
  100% {
    transform: rotate(0deg);
    transform-origin: 70px 114px;
  }
  50% {
    transform: rotate(-14deg);
    transform-origin: 70px 114px;
  }
}
</style>
