import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearAuthSession, saveAuthSession } from '@/model/authModel'
import { clearSession } from '@/model/sessionModel'
import * as authService from '@/service/authService'
import * as presenceService from '@/service/presenceService'
import { releaseMultiplayerRoom } from '@/service/multiplayerExit'

type AuthMode = 'signin' | 'signup'

const CODE_COOLDOWN_SEC = 60

/**
 * 认证 Controller：登录 / 注册表单与跳转（MVC 之 C）。
 */
export function useAuthController() {
  const router = useRouter()
  const route = useRoute()
  const mode = ref<AuthMode>('signin')
  const username = ref('')
  const password = ref('')
  const confirmPassword = ref('')
  const displayName = ref('')
  const email = ref('')
  const verificationCode = ref('')
  const loading = ref(false)
  const sendingCode = ref(false)
  const codeCooldown = ref(0)
  const codeHint = ref('')
  const error = ref('')
  let cooldownTimer: ReturnType<typeof setInterval> | null = null

  function switchMode(next: AuthMode) {
    mode.value = next
    error.value = ''
    codeHint.value = ''
  }

  function startCooldown() {
    codeCooldown.value = CODE_COOLDOWN_SEC
    if (cooldownTimer) {
      clearInterval(cooldownTimer)
    }
    cooldownTimer = setInterval(() => {
      codeCooldown.value -= 1
      if (codeCooldown.value <= 0 && cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    }, 1000)
  }

  function redirectAfterAuth() {
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.replace(redirect || '/')
  }

  async function sendCode() {
    if (!email.value.trim()) {
      error.value = '请先填写邮箱'
      return
    }
    sendingCode.value = true
    error.value = ''
    codeHint.value = ''
    try {
      const result = await authService.sendRegisterCode(email.value.trim())
      codeHint.value = result.message
      startCooldown()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '验证码发送失败'
    } finally {
      sendingCode.value = false
    }
  }

  async function submitSignin() {
    loading.value = true
    error.value = ''
    try {
      const session = await authService.signin(username.value.trim(), password.value)
      saveAuthSession(session)
      try {
        await presenceService.sendPresenceHeartbeat('ONLINE')
      } catch {
        // 忽略
      }
      redirectAfterAuth()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '登录失败'
    } finally {
      loading.value = false
    }
  }

  async function submitSignup() {
    if (!confirmPassword.value) {
      error.value = '请确认密码'
      return
    }
    if (password.value !== confirmPassword.value) {
      error.value = '两次输入的密码不一致'
      return
    }
    if (!verificationCode.value.trim()) {
      error.value = '请输入邮箱验证码'
      return
    }
    loading.value = true
    error.value = ''
    try {
      const session = await authService.signup({
        username: username.value.trim(),
        password: password.value,
        confirmPassword: confirmPassword.value,
        displayName: displayName.value.trim(),
        email: email.value.trim(),
        verificationCode: verificationCode.value.trim(),
      })
      saveAuthSession(session)
      try {
        await presenceService.sendPresenceHeartbeat('ONLINE')
      } catch {
        // 忽略
      }
      redirectAfterAuth()
    } catch (exception) {
      error.value = exception instanceof Error ? exception.message : '注册失败'
    } finally {
      loading.value = false
    }
  }

  async function submit() {
    if (!username.value.trim() || !password.value) {
      error.value = '请填写用户名和密码'
      return
    }
    if (mode.value === 'signup') {
      if (!displayName.value.trim()) {
        error.value = '请填写游戏昵称'
        return
      }
      if (!email.value.trim()) {
        error.value = '请填写邮箱'
        return
      }
      await submitSignup()
      return
    }
    await submitSignin()
  }

  async function logout() {
    loading.value = true
    error.value = ''
    try {
      try {
        await presenceService.sendPresenceHeartbeat('OFFLINE')
      } catch {
        // 忽略
      }
      await releaseMultiplayerRoom()
      await authService.signout()
    } catch {
      // 本地仍清除会话
    } finally {
      clearAuthSession()
      clearSession()
      loading.value = false
      router.replace('/')
    }
  }

  return {
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
    logout,
  }
}
