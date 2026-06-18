<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useUserManageController } from '@/controller/useUserManageController'
import HomeAmbientBackground from '@/component/home/HomeAmbientBackground.vue'
import GlassPanel from '@/component/common/GlassPanel.vue'
import GlassButton from '@/component/common/GlassButton.vue'

const {
  activeTab,
  profile,
  loading,
  error,
  success,
  displayName,
  oldPassword,
  newPassword,
  confirmPassword,
  switchTab,
  saveDisplayName,
  submitChangePassword,
  onAvatarSelected,
  avatarSrc,
} = useUserManageController()
</script>

<template>
  <div class="account-view">
    <HomeAmbientBackground />
    <div class="account-vignette" />

    <div class="account-shell">
      <nav class="account-nav">
        <RouterLink to="/" class="back-link">← 返回首页</RouterLink>
        <h1>我的账号</h1>
      </nav>

      <div class="tab-bar">
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'profile' }"
          @click="switchTab('profile')"
        >
          我的资料
        </button>
        <button
          type="button"
          class="tab"
          :class="{ active: activeTab === 'password' }"
          @click="switchTab('password')"
        >
          修改密码
        </button>
      </div>

      <p v-if="error" class="banner error">{{ error }}</p>
      <p v-if="success" class="banner success">{{ success }}</p>

      <GlassPanel v-if="activeTab === 'profile'" strong padding="24px" class="panel">
        <div v-if="profile" class="profile-head">
          <div class="avatar-wrap">
            <img
              v-if="avatarSrc(profile.avatarUrl)"
              :src="avatarSrc(profile.avatarUrl)!"
              alt="头像"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">{{ profile.displayName.slice(0, 1) }}</div>
            <label class="avatar-upload">
              <input type="file" accept="image/jpeg,image/png,image/webp" @change="onAvatarSelected" />
              更换头像
            </label>
          </div>
          <div class="profile-meta">
            <div class="meta-row"><span>用户名</span><strong>@{{ profile.username }}</strong></div>
            <div class="meta-row"><span>邮箱</span><strong>{{ profile.email || '—' }}</strong></div>
            <div class="meta-row"><span>注册时间</span><strong>{{ profile.createdAt?.slice(0, 19) || '—' }}</strong></div>
          </div>
        </div>

        <label class="field">
          <span>游戏昵称（联机显示）</span>
          <input v-model="displayName" maxlength="20" />
        </label>
        <GlassButton accent :disabled="loading" @click="saveDisplayName">保存昵称</GlassButton>
      </GlassPanel>

      <GlassPanel v-else-if="activeTab === 'password'" strong padding="24px" class="panel">
        <label class="field">
          <span>当前密码</span>
          <input v-model="oldPassword" type="password" autocomplete="current-password" />
        </label>
        <label class="field">
          <span>新密码</span>
          <input v-model="newPassword" type="password" autocomplete="new-password" placeholder="至少 6 位" />
        </label>
        <label class="field">
          <span>确认新密码</span>
          <input v-model="confirmPassword" type="password" autocomplete="new-password" />
        </label>
        <GlassButton accent :disabled="loading" @click="submitChangePassword">修改密码</GlassButton>
      </GlassPanel>
    </div>
  </div>
</template>

<style scoped>
.account-view {
  position: relative;
  min-height: 100vh;
}

.account-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(ellipse at center, transparent 50%, rgba(6, 8, 14, 0.7) 100%);
  z-index: 1;
}

.account-shell {
  position: relative;
  z-index: 2;
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 20px 40px;
}

.account-nav {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.account-nav h1 {
  margin: 0;
  font-size: 1.5rem;
}

.back-link {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.88rem;
}

.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tab {
  appearance: none;
  border: 1px solid rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-muted);
  border-radius: 999px;
  padding: 8px 16px;
  font: inherit;
  cursor: pointer;
}

.tab.active {
  border-color: rgba(88, 166, 255, 0.45);
  background: rgba(88, 166, 255, 0.15);
  color: #dbeaff;
}

.banner {
  margin: 0 0 12px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  font-size: 0.88rem;
}

.banner.error {
  background: rgba(220, 90, 90, 0.12);
  border: 1px solid rgba(220, 90, 90, 0.35);
  color: #ffb4b4;
}

.banner.success {
  background: rgba(80, 180, 120, 0.12);
  border: 1px solid rgba(80, 180, 120, 0.35);
  color: #b8f0cc;
}

.panel {
  border: 1px solid rgba(88, 166, 255, 0.2);
}

.profile-head {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-img,
.avatar-placeholder {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  display: grid;
  place-items: center;
  font-size: 2rem;
  font-weight: 700;
  background: rgba(88, 166, 255, 0.2);
  border: 2px solid rgba(88, 166, 255, 0.4);
  color: #dbeaff;
}

.avatar-upload {
  font-size: 0.8rem;
  color: #9ecaff;
  cursor: pointer;
}

.avatar-upload input {
  display: none;
}

.profile-meta {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-row {
  display: flex;
  gap: 12px;
  font-size: 0.88rem;
}

.meta-row span {
  color: var(--text-muted);
  min-width: 72px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 14px;
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

</style>
