import { apiClient } from './api'
import { LoginRequest, LoginResponse } from '@/types'

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post('/api/auth/login', credentials)
    return response.data
  },

  logout: async (): Promise<void> => {
    await apiClient.post('/api/auth/logout')
  },

  validateToken: async (token: string): Promise<boolean> => {
    try {
      const response = await apiClient.post('/api/auth/validate', { token })
      return response.data.valid
    } catch {
      return false
    }
  },

  refreshToken: async (refreshToken: string): Promise<string> => {
    const response = await apiClient.post('/api/auth/refresh', { refreshToken })
    return response.data.accessToken
  },
}
