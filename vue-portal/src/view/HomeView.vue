<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { saveSoloSession, updateSoloViewState } from '@/model/soloSessionModel'
import * as soloService from '@/service/soloService'
import { unlockGameBgm } from '@/service/gameBgm'
import HomeAmbientBackground from '@/component/home/HomeAmbientBackground.vue'
import DisplayModeToggle from '@/component/game/DisplayModeToggle.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const router = useRouter()
const playerName = ref('玩家')
const loading = ref(false)
const error = ref('')

async function startSolo() {
  unlockGameBgm()
  loading.value = true
  error.value = ''
  try {
    const created = await soloService.createSoloSession(playerName.value.trim() || '玩家')
    saveSoloSession({
      sessionId: created.sessionId,
      playerName: playerName.value.trim() || '玩家',
    })
    updateSoloViewState(created.state)
    router.push('/solo')
  } catch (exception) {
    error.value = exception instanceof Error ? exception.message : '无法连接服务端，请先 mvn spring-boot:run'
  } finally {
    loading.value = false
  }
}

function openMultiplayer() {
  router.push('/multiplayer')
}
</script>

<template>
  <div class="home-view">
    <HomeAmbientBackground />
    <div class="home-vignette" />

    <div class="home-shell">
      <header class="hero">
        <div class="hero-badge">
          <span class="badge-mark">519</span>
          <span class="badge-text">CLUB</span>
        </div>

        <h1 class="hero-title">
          <span class="title-line">熄灯前</span>
          <span class="title-line accent">归寝</span>
        </h1>

        <p class="hero-sub">武汉理工大学 · 五关限时闯关 · 23:00 前回到寝室</p>

        <div class="hero-tags">
          <span class="tag">L1 → L5</span>
          <span class="tag">沉浸式 HUD</span>
          <span class="tag">联机协作</span>
        </div>

        <div class="countdown-strip">
          <span class="countdown-dot" />
          <span>熄灯倒计时 · 每一秒都很珍贵</span>
        </div>
      </header>

      <section class="mode-section">
        <article class="mode-card mode-solo">
          <div class="card-glow" />
          <div class="card-header">
            <svg class="card-icon" viewBox="0 0 48 48" aria-hidden="true">
              <path
                d="M24 6L8 16v20c0 8 7 12 16 14 9-2 16-6 16-14V16L24 6z"
                fill="none"
                stroke="currentColor"
                stroke-width="2.2"
                stroke-linejoin="round"
              />
              <path d="M24 18v14M18 24h12" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" />
            </svg>
            <div>
              <h2>单人闯关</h2>
              <p>五关剧情 · 拾取解谜 · 赶归寝</p>
            </div>
          </div>

          <ul class="card-features">
            <li>全屏场景探索</li>
            <li>背包 · NPC · 结局弹层</li>
            <li>与 Enhanced 玩法一致</li>
          </ul>

          <label class="name-field">
            <span>玩家昵称</span>
            <input v-model="playerName" maxlength="20" placeholder="输入你的名字" />
          </label>

          <GlassButton accent class="card-cta" :disabled="loading" @click="startSolo">
            {{ loading ? '正在进入…' : '开始单人游戏' }}
          </GlassButton>
        </article>

        <article class="mode-card mode-multi">
          <div class="card-glow" />
          <div class="card-header">
            <svg class="card-icon" viewBox="0 0 48 48" aria-hidden="true">
              <circle cx="16" cy="18" r="6" fill="none" stroke="currentColor" stroke-width="2.2" />
              <circle cx="32" cy="18" r="6" fill="none" stroke="currentColor" stroke-width="2.2" />
              <path
                d="M8 38c0-6 5-10 12-10M28 28c7 0 12 4 12 10"
                fill="none"
                stroke="currentColor"
                stroke-width="2.2"
                stroke-linecap="round"
              />
            </svg>
            <div>
              <h2>多人联机</h2>
              <p>创建房间 · 与同学一起归寝</p>
            </div>
          </div>

          <ul class="card-features">
            <li>最多 4 人同房间</li>
            <li>实时状态同步</li>
            <li>共享熄灯倒计时</li>
          </ul>

          <div class="multi-preview">
            <span /><span /><span /><span />
          </div>

          <GlassButton class="card-cta" :disabled="loading" @click="openMultiplayer">
            进入联机大厅
          </GlassButton>
        </article>
      </section>

      <footer class="home-footer">
        <DisplayModeToggle />
        <p v-if="error" class="error">{{ error }}</p>
        <p class="hint">需先启动 Java 服务端：<code>mvn spring-boot:run</code></p>
        <p class="copyright">World of Zuul Extended · Vue 3 MVC Client</p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.home-view {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.home-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, transparent 20%, rgba(6, 8, 14, 0.75) 100%),
    linear-gradient(to bottom, rgba(6, 8, 14, 0.35), transparent 30%, rgba(6, 8, 14, 0.5));
  z-index: 1;
}

.home-shell {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 24px 24px;
  gap: 36px;
}

.hero {
  text-align: center;
  max-width: 640px;
  animation: heroIn 0.8s ease both;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px 6px 8px;
  border-radius: 999px;
  border: 1px solid rgba(136, 198, 255, 0.35);
  background: rgba(15, 20, 36, 0.55);
  backdrop-filter: blur(8px);
  margin-bottom: 18px;
}

.badge-mark {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 0.72rem;
  font-weight: 800;
  background: linear-gradient(135deg, #1f3b66, var(--accent));
}

.badge-text {
  font-size: 0.78rem;
  letter-spacing: 0.18em;
  color: var(--text-muted);
}

.hero-title {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px 16px;
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: 0.04em;
}

.title-line {
  display: inline-block;
  text-shadow: 0 0 40px rgba(88, 166, 255, 0.25);
}

.title-line.accent {
  color: #9ecaff;
  animation: titleGlow 3s ease-in-out infinite;
}

.hero-sub {
  margin: 14px 0 0;
  color: var(--text-muted);
  font-size: 0.95rem;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 18px;
}

.tag {
  padding: 5px 12px;
  border-radius: 999px;
  font-size: 0.75rem;
  color: #c8d8f0;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.04);
}

.countdown-strip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 8px 16px;
  border-radius: 999px;
  font-size: 0.82rem;
  color: #ffc9a0;
  background: rgba(255, 140, 60, 0.08);
  border: 1px solid rgba(255, 160, 80, 0.22);
}

.countdown-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ff9a5a;
  box-shadow: 0 0 10px rgba(255, 140, 60, 0.8);
  animation: dotBlink 1.4s ease-in-out infinite;
}

.mode-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  width: min(920px, 100%);
  animation: cardsIn 0.9s ease 0.15s both;
}

.mode-card {
  position: relative;
  padding: 24px 22px 22px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(12, 16, 28, 0.72);
  backdrop-filter: blur(16px);
  overflow: hidden;
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.mode-card:hover {
  transform: translateY(-4px);
  border-color: rgba(136, 198, 255, 0.35);
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
}

.card-glow {
  position: absolute;
  inset: -40% auto auto -20%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.35;
  pointer-events: none;
}

.mode-solo .card-glow {
  background: rgba(88, 166, 255, 0.5);
}

.mode-multi .card-glow {
  background: rgba(160, 120, 255, 0.45);
  left: auto;
  right: -20%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.card-icon {
  width: 48px;
  height: 48px;
  color: var(--accent);
  flex-shrink: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 1.15rem;
}

.card-header p {
  margin: 4px 0 0;
  font-size: 0.82rem;
  color: var(--text-muted);
}

.card-features {
  margin: 0 0 18px;
  padding-left: 18px;
  color: var(--text-muted);
  font-size: 0.84rem;
  line-height: 1.7;
}

.name-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
  font-size: 0.82rem;
  color: var(--text-muted);
}

.name-field input {
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.name-field input:focus {
  outline: none;
  border-color: rgba(88, 166, 255, 0.5);
  box-shadow: 0 0 0 3px rgba(88, 166, 255, 0.12);
}

.multi-preview {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.multi-preview span {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.14);
  background: rgba(255, 255, 255, 0.06);
  animation: avatarPulse 2.4s ease-in-out infinite;
}

.multi-preview span:nth-child(2) {
  animation-delay: 0.3s;
}
.multi-preview span:nth-child(3) {
  animation-delay: 0.6s;
}
.multi-preview span:nth-child(4) {
  animation-delay: 0.9s;
}

.card-cta {
  width: 100%;
}

.home-footer {
  text-align: center;
  animation: heroIn 1s ease 0.3s both;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.error {
  margin: 0 0 10px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  background: rgba(220, 90, 90, 0.12);
  border: 1px solid rgba(220, 90, 90, 0.35);
  color: #ffb4b4;
  font-size: 0.88rem;
}

.hint {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.copyright {
  margin: 8px 0 0;
  font-size: 0.72rem;
  color: rgba(180, 188, 204, 0.55);
}

code {
  color: #9ecaff;
}

@keyframes heroIn {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardsIn {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes titleGlow {
  0%,
  100% {
    text-shadow: 0 0 24px rgba(136, 198, 255, 0.35);
  }
  50% {
    text-shadow: 0 0 36px rgba(136, 198, 255, 0.65);
  }
}

@keyframes dotBlink {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

@keyframes avatarPulse {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-3px);
    opacity: 1;
  }
}

@media (max-width: 640px) {
  .home-shell {
    padding-top: 24px;
    gap: 28px;
  }

  .hero-title {
    font-size: 1.85rem;
  }
}
</style>
