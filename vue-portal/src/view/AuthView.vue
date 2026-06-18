<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthController } from '@/controller/useAuthController'
import HomeAmbientBackground from '@/component/home/HomeAmbientBackground.vue'
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const {
  mode,
  username,
  password,
  confirmPassword,
  displayName,
  email,
  verificationCode,
  loading,
  sendingCode,
  codeCooldown,
  codeHint,
  error,
  switchMode,
  sendCode,
  submit,
} = useAuthController()

const isSignin = computed(() => mode.value === 'signin')
</script>

<template>
  <div class="auth-view">
    <HomeAmbientBackground />
    <div class="auth-vignette" />

    <div class="auth-shell">
      <RouterLink to="/" class="back-link">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M15 18l-6-6 6-6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
        </svg>
        返回首页
      </RouterLink>

      <GlassPanel strong padding="28px 28px 24px" class="auth-card">
        <header class="auth-header">
          <div class="auth-badge">ACCOUNT</div>
          <h1>{{ isSignin ? '登录账号' : '注册账号' }}</h1>
          <p class="muted">
            {{
              isSignin
                ? '登录后联机昵称将自动绑定到你的账号'
                : '需先向邮箱发送验证码，验证通过后才能完成注册'
            }}
          </p>
        </header>

        <div class="mode-tabs">
          <button type="button" class="tab" :class="{ active: isSignin }" @click="switchMode('signin')">
            登录
          </button>
          <button type="button" class="tab" :class="{ active: !isSignin }" @click="switchMode('signup')">
            注册
          </button>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <label class="field">
            <span>用户名</span>
            <input v-model="username" maxlength="32" autocomplete="username" placeholder="至少 3 个字符" />
          </label>

          <label class="field">
            <span>密码</span>
            <input
              v-model="password"
              type="password"
              maxlength="64"
              :autocomplete="isSignin ? 'current-password' : 'new-password'"
              placeholder="至少 6 位"
            />
          </label>

          <template v-if="!isSignin">
            <label class="field">
              <span>确认密码</span>
              <input
                v-model="confirmPassword"
                type="password"
                maxlength="64"
                autocomplete="new-password"
                placeholder="再次输入密码"
              />
            </label>

            <label class="field">
              <span>游戏昵称</span>
              <input
                v-model="displayName"
                maxlength="20"
                autocomplete="nickname"
                placeholder="联机时显示的名字"
              />
            </label>

            <label class="field">
              <span>邮箱</span>
              <input v-model="email" type="email" maxlength="128" autocomplete="email" placeholder="用于接收验证码" />
            </label>

            <label class="field code-field">
              <span>邮箱验证码</span>
              <div class="code-row">
                <input v-model="verificationCode" maxlength="8" placeholder="6 位数字" />
                <GlassButton
                  type="button"
                  :disabled="sendingCode || codeCooldown > 0 || loading"
                  @click="sendCode"
                >
                  {{
                    sendingCode
                      ? '发送中…'
                      : codeCooldown > 0
                        ? `${codeCooldown}s`
                        : '发送验证码'
                  }}
                </GlassButton>
              </div>
            </label>

            <p v-if="codeHint" class="hint">{{ codeHint }}</p>
          </template>

          <p v-if="error" class="error">{{ error }}</p>

          <GlassButton accent class="submit-btn" :disabled="loading" @click="submit">
            {{ loading ? '处理中…' : isSignin ? '登录' : '注册并登录' }}
          </GlassButton>
        </form>
      </GlassPanel>
    </div>
  </div>
</template>

<style scoped>
.auth-view {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.auth-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(ellipse at center, transparent 20%, rgba(6, 8, 14, 0.78) 100%),
    linear-gradient(to bottom, rgba(6, 8, 14, 0.4), transparent 30%, rgba(6, 8, 14, 0.55));
  z-index: 1;
}

.auth-shell {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  gap: 16px;
}

.back-link {
  align-self: flex-start;
  margin-left: max(0px, calc(50% - 240px));
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.88rem;
}

.back-link svg {
  width: 18px;
  height: 18px;
}

.back-link:hover {
  color: var(--text-primary);
}

.auth-card {
  width: min(480px, 100%);
  border: 1px solid rgba(88, 166, 255, 0.22);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.45);
}

.auth-header {
  text-align: center;
  margin-bottom: 20px;
}

.auth-badge {
  display: inline-block;
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 0.72rem;
  letter-spacing: 0.16em;
  color: #9ecaff;
  border: 1px solid rgba(88, 166, 255, 0.3);
  background: rgba(88, 166, 255, 0.08);
  margin-bottom: 12px;
}

.auth-header h1 {
  margin: 0;
  font-size: 1.6rem;
  font-weight: 800;
}

.muted {
  margin: 10px 0 0;
  color: var(--text-muted);
  font-size: 0.86rem;
  line-height: 1.5;
}

.mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 18px;
  padding: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.tab {
  appearance: none;
  border: none;
  background: transparent;
  color: var(--text-muted);
  border-radius: 999px;
  padding: 10px 12px;
  font: inherit;
  font-size: 0.9rem;
  cursor: pointer;
}

.tab.active {
  background: rgba(88, 166, 255, 0.2);
  color: #dbeaff;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 0.82rem;
  color: var(--text-muted);
}

.field input {
  padding: 11px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
  font: inherit;
}

.field input:focus {
  outline: none;
  border-color: rgba(88, 166, 255, 0.5);
}

.code-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.hint {
  margin: 0 0 10px;
  font-size: 0.8rem;
  color: #9ecaff;
  line-height: 1.5;
}

.error {
  margin: 4px 0 8px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  background: rgba(220, 90, 90, 0.12);
  border: 1px solid rgba(220, 90, 90, 0.35);
  color: #ffb4b4;
  font-size: 0.88rem;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
}
</style>
