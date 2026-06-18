import { apiGet, jsonPost, uploadMultipart } from '@/service/httpClient'
import type { AuthSession, UserProfile } from '@/model/authModel'
import { getStoredAuthSession } from '@/model/authModel'

export interface RegisterCodeResponse {
  message: string
}

export interface AvatarUploadResponse {
  avatarUrl: string
  session: AuthSession
}

export function sendRegisterCode(email: string): Promise<RegisterCodeResponse> {
  return jsonPost<RegisterCodeResponse>('/api/auth/register/code', { email })
}

export function signin(username: string, password: string): Promise<AuthSession> {
  return jsonPost<AuthSession>('/api/auth/signin', { username, password })
}

export function signup(payload: {
  username: string
  password: string
  confirmPassword: string
  displayName: string
  email: string
  verificationCode: string
}): Promise<AuthSession> {
  return jsonPost<AuthSession>('/api/auth/signup', payload)
}

export async function signout(): Promise<void> {
  const session = getStoredAuthSession()
  if (!session) {
    return
  }
  await jsonPost<string>('/api/auth/signout', {}, { 'X-Auth-Token': session.token })
}

export function fetchProfile(): Promise<UserProfile> {
  return apiGet<UserProfile>('/api/auth/profile')
}

export function updateDisplayName(displayName: string): Promise<AuthSession> {
  return jsonPost<AuthSession>('/api/auth/profile/display-name', { displayName })
}

export function changePassword(
  oldPassword: string,
  newPassword: string,
  confirmPassword: string,
): Promise<AuthSession> {
  return jsonPost<AuthSession>('/api/auth/profile/password', {
    oldPassword,
    newPassword,
    confirmPassword,
  })
}

export function uploadAvatar(file: File): Promise<AvatarUploadResponse> {
  return uploadMultipart<AvatarUploadResponse>('/api/auth/avatar', file)
}
