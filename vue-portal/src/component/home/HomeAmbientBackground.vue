<script setup lang="ts">
/** 首页动态氛围背景：夜色渐变 + 浮动光点 + 远景微光 */
const particles = Array.from({ length: 36 }, (_, index) => ({
  id: index,
  left: `${6 + ((index * 17) % 88)}%`,
  top: `${8 + ((index * 23) % 72)}%`,
  size: 2 + (index % 4),
  delay: `${(index % 12) * 0.45}s`,
  duration: `${4 + (index % 5) * 1.2}s`,
}))
</script>

<template>
  <div class="ambient" aria-hidden="true">
    <div class="ambient-gradient" />
    <div class="ambient-horizon" />
    <div class="ambient-grid" />

    <span
      v-for="particle in particles"
      :key="particle.id"
      class="particle"
      :style="{
        left: particle.left,
        top: particle.top,
        width: `${particle.size}px`,
        height: `${particle.size}px`,
        animationDelay: particle.delay,
        animationDuration: particle.duration,
      }"
    />

    <div class="light-beam beam-a" />
    <div class="light-beam beam-b" />
    <div class="moon-glow" />
  </div>
</template>

<style scoped>
.ambient {
  position: absolute;
  inset: 0;
  overflow: hidden;
  background: #060810;
}

.ambient-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 120% 80% at 50% 100%, rgba(30, 48, 88, 0.55) 0%, transparent 55%),
    radial-gradient(circle at 20% 20%, rgba(88, 120, 200, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 80% 15%, rgba(120, 80, 180, 0.1) 0%, transparent 35%),
    linear-gradient(180deg, #0a0e1a 0%, #12182c 45%, #0c1018 100%);
  animation: gradientShift 14s ease-in-out infinite alternate;
}

.ambient-horizon {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 18%;
  height: 2px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(136, 198, 255, 0.25),
    rgba(255, 200, 120, 0.2),
    rgba(136, 198, 255, 0.25),
    transparent
  );
  filter: blur(1px);
  animation: horizonPulse 6s ease-in-out infinite;
}

.ambient-grid {
  position: absolute;
  left: -10%;
  right: -10%;
  bottom: 0;
  height: 42%;
  background:
    linear-gradient(rgba(88, 166, 255, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(88, 166, 255, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  transform: perspective(420px) rotateX(68deg);
  transform-origin: center bottom;
  mask-image: linear-gradient(to top, rgba(0, 0, 0, 0.5), transparent 85%);
  opacity: 0.35;
}

.particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(180, 210, 255, 0.85);
  box-shadow: 0 0 8px rgba(136, 198, 255, 0.6);
  animation: particleFloat 5s ease-in-out infinite;
}

.particle:nth-child(3n) {
  background: rgba(255, 210, 140, 0.75);
  box-shadow: 0 0 10px rgba(255, 190, 100, 0.45);
}

.light-beam {
  position: absolute;
  width: 280px;
  height: 480px;
  filter: blur(40px);
  opacity: 0.18;
  animation: beamDrift 10s ease-in-out infinite alternate;
}

.beam-a {
  left: 12%;
  top: 10%;
  background: radial-gradient(circle, rgba(88, 166, 255, 0.5), transparent 70%);
}

.beam-b {
  right: 8%;
  top: 22%;
  background: radial-gradient(circle, rgba(160, 120, 255, 0.4), transparent 70%);
  animation-delay: -4s;
}

.moon-glow {
  position: absolute;
  top: 8%;
  right: 14%;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(220, 230, 255, 0.14) 0%, transparent 70%);
  animation: moonPulse 8s ease-in-out infinite;
}

@keyframes gradientShift {
  from {
    filter: hue-rotate(0deg) brightness(1);
  }
  to {
    filter: hue-rotate(8deg) brightness(1.06);
  }
}

@keyframes horizonPulse {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

@keyframes particleFloat {
  0%,
  100% {
    transform: translateY(0) scale(1);
    opacity: 0.35;
  }
  50% {
    transform: translateY(-12px) scale(1.15);
    opacity: 0.9;
  }
}

@keyframes beamDrift {
  from {
    transform: translate(0, 0);
  }
  to {
    transform: translate(24px, 18px);
  }
}

@keyframes moonPulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.08);
    opacity: 1;
  }
}
</style>
