import type { ApiResponse } from '@/model/types'
import { getStoredAuthSession } from '@/model/authModel'

const JSON_HEADERS = {
  'Content-Type': 'application/json',
}

export function buildAuthHeaders(extra?: Record<string, string>): Record<string, string> {
  const headers: Record<string, string> = { ...JSON_HEADERS, ...extra }
  const session = getStoredAuthSession()
  if (session?.token) {
    headers['X-Auth-Token'] = session.token
  }
  return headers
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, init)
  const payload = (await response.json()) as ApiResponse<T>
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

export function apiGet<T>(path: string): Promise<T> {
  return apiRequest<T>(path, { headers: buildAuthHeaders() })
}

export function jsonPost<T>(
  path: string,
  body: unknown,
  extraHeaders?: Record<string, string>,
): Promise<T> {
  return apiRequest<T>(path, {
    method: 'POST',
    headers: buildAuthHeaders(extraHeaders),
    body: JSON.stringify(body),
  })
}

export async function uploadMultipart<T>(path: string, file: File, fieldName = 'file'): Promise<T> {
  const form = new FormData()
  form.append(fieldName, file)
  const headers: Record<string, string> = {}
  const session = getStoredAuthSession()
  if (session?.token) {
    headers['X-Auth-Token'] = session.token
  }
  const response = await fetch(path, { method: 'POST', headers, body: form })
  const payload = (await response.json()) as ApiResponse<T>
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || '上传失败')
  }
  return payload.data
}
