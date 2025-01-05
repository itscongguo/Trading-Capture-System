import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { UserInfo } from '@/types'

interface AuthState {
  isAuthenticated: boolean
  accessToken: string | null
  refreshToken: string | null
  userInfo: UserInfo | null
  login: (accessToken: string, refreshToken: string, userInfo: UserInfo) => void
  logout: () => void
  updateToken: (accessToken: string) => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      isAuthenticated: false,
      accessToken: null,
      refreshToken: null,
      userInfo: null,

      login: (accessToken, refreshToken, userInfo) => {
        set({
          isAuthenticated: true,
          accessToken,
          refreshToken,
          userInfo,
        })
      },

      logout: () => {
        set({
          isAuthenticated: false,
          accessToken: null,
          refreshToken: null,
          userInfo: null,
        })
      },

      updateToken: (accessToken) => {
        set({ accessToken })
      },
    }),
    {
      name: 'tcs-auth-storage',
    }
  )
)
