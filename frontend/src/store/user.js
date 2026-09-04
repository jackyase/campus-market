import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  getters: {
    isLoggedIn: (s) => !!s.token
  },
  actions: {
    async login(form) {
      const res = await loginApi(form)
      const data = res.data
      this.token = data.token
      this.user = {
        id: data.userId,
        username: data.username,
        nickname: data.nickname,
        role: data.role
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
      return res
    },
    async register(form) {
      return await registerApi(form)
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
