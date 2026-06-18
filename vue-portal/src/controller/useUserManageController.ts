import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { UserProfile } from '@/model/authModel'
import { authSessionModel, mergeAuthSession, resolveMediaUrl, saveAuthSession } from '@/model/authModel'
import * as authService from '@/service/authService'

type ManageTab = 'profile' | 'password'

/**
 * 用户管理 Controller：个人资料、改密、头像。
 */
export function useUserManageController() {
  const router = useRouter()
  const activeTab = ref<ManageTab>('profile')
  const profile = ref<UserProfile | null>(null)
  const loading = ref(false)
  const error = ref('')
  const success = ref('')

  const displayName = ref('')
  const oldPassword = ref('')
  const newPassword = ref('')
  const confirmPassword = ref('')

  const avatarVersion = ref(0)

  function clearMessages() {
    error.value = ''
    success.value = ''
  }

  async function loadProfile() {
    loading.value = true
    clearMessages()
    try {
      profile.value = await authService.fetchProfile()
      displayName.value = profile.value.displayName
      mergeAuthSession({
        displayName: profile.value.displayName,
        email: profile.value.email,
        avatarUrl: profile.value.avatarUrl,
      })
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '加载资料失败'
    } finally {
      loading.value = false
    }
  }

  async function saveDisplayName() {
    if (!displayName.value.trim()) {
      error.value = '昵称不能为空'
      return
    }
    loading.value = true
    clearMessages()
    try {
      const session = await authService.updateDisplayName(displayName.value.trim())
      saveAuthSession(session)
      mergeAuthSession({ displayName: session.displayName })
      success.value = '昵称已更新'
      await loadProfile()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '更新昵称失败'
    } finally {
      loading.value = false
    }
  }

  async function submitChangePassword() {
    if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
      error.value = '请填写完整密码信息'
      return
    }
    loading.value = true
    clearMessages()
    try {
      await authService.changePassword(oldPassword.value, newPassword.value, confirmPassword.value)
      oldPassword.value = ''
      newPassword.value = ''
      confirmPassword.value = ''
      success.value = '密码修改成功'
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '修改密码失败'
    } finally {
      loading.value = false
    }
  }

  async function onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    if (!file) {
      return
    }
    loading.value = true
    clearMessages()
    try {
      const result = await authService.uploadAvatar(file)
      saveAuthSession(result.session)
      mergeAuthSession({ avatarUrl: result.avatarUrl })
      avatarVersion.value = Date.now()
      success.value = '头像上传成功'
      await loadProfile()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '头像上传失败'
    } finally {
      loading.value = false
      input.value = ''
    }
  }

  function switchTab(tab: ManageTab) {
    activeTab.value = tab
    clearMessages()
  }

  function avatarSrc(url: string | null | undefined): string | null {
    return resolveMediaUrl(url, avatarVersion.value || profile.value?.userId)
  }

  onMounted(() => {
    if (!authSessionModel.value) {
      router.replace({ name: 'auth', query: { redirect: '/account' } })
      return
    }
    void loadProfile()
  })

  return {
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
  }
}
