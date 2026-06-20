<script setup lang="ts">
/** 联机大厅：匹配中心 + 房间节点 + 玩家连线（高对比动态背景） */

const hubRings = [0, 1, 2]
const roomNodes = [
  { id: 1, x: 8, y: 26, label: 'ROOM A', delay: '0s' },
  { id: 2, x: 92, y: 22, label: 'ROOM B', delay: '0.6s' },
  { id: 3, x: 6, y: 68, label: 'ROOM C', delay: '1.1s' },
  { id: 4, x: 94, y: 64, label: 'ROOM D', delay: '0.3s' },
  { id: 5, x: 50, y: 78, label: 'LOBBY', delay: '0.9s' },
]

const avatars = [
  { id: 1, x: 32, y: 48, delay: '0.2s' },
  { id: 2, x: 68, y: 44, delay: '0.8s' },
  { id: 3, x: 44, y: 58, delay: '1.4s' },
  { id: 4, x: 58, y: 52, delay: '0.5s' },
]

const particles = Array.from({ length: 24 }, (_, index) => ({
  id: index,
  x: 8 + ((index * 19) % 84),
  y: 12 + ((index * 27) % 76),
  delay: `${(index % 10) * 0.35}s`,
}))
</script>

<template>
  <div class="lobby-ambient" aria-hidden="true">
    <div class="ambient-scaler">
    <div class="sky-layer" />
    <div class="hub-floor" />

    <svg class="link-layer" viewBox="0 0 100 100" preserveAspectRatio="none">
      <line x1="50" y1="14" x2="8" y2="26" class="link-line" />
      <line x1="50" y1="14" x2="92" y2="22" class="link-line delay-1" />
      <line x1="50" y1="14" x2="6" y2="68" class="link-line delay-2" />
      <line x1="50" y1="14" x2="94" y2="64" class="link-line delay-3" />
      <line x1="50" y1="14" x2="50" y2="78" class="link-line delay-4" />
    </svg>

    <div class="hub-core">
      <span v-for="ring in hubRings" :key="ring" class="hub-ring" :class="`ring-${ring}`" />
      <span class="hub-icon">
        <svg viewBox="0 0 48 48" aria-hidden="true">
          <circle cx="16" cy="18" r="5" fill="none" stroke="currentColor" stroke-width="2" />
          <circle cx="32" cy="18" r="5" fill="none" stroke="currentColor" stroke-width="2" />
          <path d="M8 36c0-5 4-9 10-9M30 27c6 0 10 4 10 9" fill="none" stroke="currentColor" stroke-width="2" />
        </svg>
      </span>
      <span class="hub-label">MATCH HUB</span>
    </div>

    <div
      v-for="node in roomNodes"
      :key="node.id"
      class="room-node"
      :style="{
        left: `${node.x}%`,
        top: `${node.y}%`,
        animationDelay: node.delay,
      }"
    >
      <span class="node-glow" />
      <span class="node-label">{{ node.label }}</span>
    </div>

    <div
      v-for="avatar in avatars"
      :key="avatar.id"
      class="avatar-dot"
      :style="{
        left: `${avatar.x}%`,
        top: `${avatar.y}%`,
        animationDelay: avatar.delay,
      }"
    />

    <span
      v-for="particle in particles"
      :key="particle.id"
      class="particle"
      :style="{
        left: `${particle.x}%`,
        top: `${particle.y}%`,
        animationDelay: particle.delay,
      }"
    />

    <div class="scan-line" />
    </div>
  </div>
</template>

<style scoped>
.lobby-ambient {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: linear-gradient(165deg, #1a2240 0%, #12182e 45%, #0e1424 100%);
}

.ambient-scaler {
  position: absolute;
  inset: 0;
  transform: none;
}

.sky-layer {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 50% 28%, rgba(88, 166, 255, 0.26) 0%, transparent 45%),
    radial-gradient(circle at 20% 20%, rgba(255, 170, 90, 0.14) 0%, transparent 35%),
    radial-gradient(circle at 80% 25%, rgba(160, 120, 255, 0.12) 0%, transparent 32%);
  animation: skyPulse 8s ease-in-out infinite alternate;
}

.hub-floor {
  position: absolute;
  left: -20%;
  right: -20%;
  bottom: 0;
  height: 55%;
  background:
    linear-gradient(rgba(136, 198, 255, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(136, 198, 255, 0.14) 1px, transparent 1px);
  background-size: 52px 52px;
  transform: perspective(480px) rotateX(68deg);
  transform-origin: center bottom;
  mask-image: linear-gradient(to top, rgba(0, 0, 0, 0.85), transparent 92%);
}

.link-layer {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  opacity: 0.55;
}

.link-line {
  stroke: rgba(136, 198, 255, 0.35);
  stroke-width: 0.35;
  stroke-dasharray: 2 1.5;
  animation: linkFlow 3s linear infinite;
}

.link-line.delay-1 {
  animation-delay: 0.5s;
}
.link-line.delay-2 {
  animation-delay: 1s;
}
.link-line.delay-3 {
  animation-delay: 1.5s;
}
.link-line.delay-4 {
  animation-delay: 0.8s;
}

.hub-core {
  position: absolute;
  left: 50%;
  top: 14%;
  transform: translate(-50%, -50%);
  width: 160px;
  height: 160px;
  display: grid;
  place-items: center;
}

.hub-ring {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(136, 198, 255, 0.35);
  animation: hubRipple 4s ease-out infinite;
}

.ring-0 {
  width: 110px;
  height: 110px;
}
.ring-1 {
  width: 150px;
  height: 150px;
  animation-delay: 1.2s;
}
.ring-2 {
  width: 190px;
  height: 190px;
  animation-delay: 2.4s;
}

.hub-icon {
  width: 62px;
  height: 62px;
  color: #9ecaff;
  filter: drop-shadow(0 0 12px rgba(88, 166, 255, 0.6));
  z-index: 1;
}

.hub-label {
  position: absolute;
  bottom: -8px;
  font-size: 0.62rem;
  letter-spacing: 0.14em;
  color: rgba(180, 210, 255, 0.75);
}

.room-node {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 96px;
  height: 60px;
  border-radius: 12px;
  border: 1px solid rgba(255, 180, 100, 0.35);
  background: rgba(20, 26, 44, 0.75);
  display: grid;
  place-items: center;
  animation: nodeFloat 4s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(255, 160, 80, 0.12);
}

.node-glow {
  position: absolute;
  inset: 6px;
  border-radius: 8px;
  background: radial-gradient(circle, rgba(255, 170, 90, 0.2), transparent 70%);
}

.node-label {
  position: relative;
  font-size: 0.62rem;
  letter-spacing: 0.08em;
  color: #ffd4a8;
  font-weight: 600;
}

.avatar-dot {
  position: absolute;
  width: 14px;
  height: 14px;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 12px rgba(88, 166, 255, 0.7);
  animation: avatarPulse 2.8s ease-in-out infinite;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(180, 220, 255, 0.9);
  box-shadow: 0 0 8px rgba(136, 198, 255, 0.6);
  animation: particleDrift 5s ease-in-out infinite;
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  top: 30%;
  background: linear-gradient(90deg, transparent, rgba(136, 198, 255, 0.4), transparent);
  animation: scanSweep 6s linear infinite;
}

@keyframes skyPulse {
  from {
    opacity: 0.85;
  }
  to {
    opacity: 1;
  }
}

@keyframes hubRipple {
  0% {
    transform: scale(0.85);
    opacity: 0.7;
  }
  100% {
    transform: scale(1.15);
    opacity: 0;
  }
}

@keyframes nodeFloat {
  0%,
  100% {
    transform: translate(-50%, -50%);
  }
  50% {
    transform: translate(-50%, calc(-50% - 6px));
  }
}

@keyframes avatarPulse {
  0%,
  100% {
    opacity: 0.5;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.15);
  }
}

@keyframes particleDrift {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

@keyframes linkFlow {
  from {
    stroke-dashoffset: 0;
  }
  to {
    stroke-dashoffset: -12;
  }
}

@keyframes scanSweep {
  from {
    transform: translateY(0);
    opacity: 0.3;
  }
  50% {
    opacity: 0.8;
  }
  to {
    transform: translateY(220px);
    opacity: 0.2;
  }
}
</style>
