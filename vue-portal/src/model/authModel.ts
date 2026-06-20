import { ref } from 'vue'

/** 登录会话（与后端 /api/auth 响应对齐） */
export interface AuthSession {
  userId: number
  username: string
  displayName: string
  email?: string | null
  avatarUrl?: string | null
  token: string
  expiresAt: string
}

/** 用户公开资料 */
export interface UserProfile {
  userId: number
  username: string
  displayName: string
  email: string | null
  avatarUrl: string | null
  createdAt: string | null
}

const STORAGE_KEY = 'zuul-auth-session'

function loadAuthSession(): AuthSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return null
    }
    const session = JSON.parse(raw) as AuthSession
    if (!session.token || !session.displayName) {
      return null
    }
    if (session.expiresAt && new Date(session.expiresAt).getTime() < Date.now()) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }
    return session
  } catch {
    return null
  }
}

/** 全局认证状态（Model 层单例） */
export const authSessionModel = ref<AuthSession | null>(loadAuthSession())

export function saveAuthSession(value: AuthSession): void {
  authSessionModel.value = value
  localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
}

export function mergeAuthSession(partial: Partial<AuthSession>): void {
  const current = getStoredAuthSession()
  if (!current) {
    return
  }
  saveAuthSession({ ...current, ...partial })
}

export function clearAuthSession(): void {
  authSessionModel.value = null
  localStorage.removeItem(STORAGE_KEY)
}

export function getStoredAuthSession(): AuthSession | null {
  return authSessionModel.value ?? loadAuthSession()
}

export function isLoggedIn(): boolean {
  return getStoredAuthSession() !== null
}

/** 解析头像等静态资源 URL（走 Vite 代理到后端） */
export function resolveMediaUrl(url: string | null | undefined, cacheBust?: number): string | null {
  if (!url || !url.trim()) {
    return null
  }
  const trimmed = url.trim()
  if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('blob:')) {
    return cacheBust ? `${trimmed}${trimmed.includes('?') ? '&' : '?'}t=${cacheBust}` : trimmed
  }
  const path = trimmed.startsWith('/') ? trimmed : `/${trimmed}`
  return cacheBust ? `${path}?t=${cacheBust}` : path
}
