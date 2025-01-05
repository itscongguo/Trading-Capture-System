import { apiClient } from './api'
import { Order, CreateOrderRequest } from '@/types'

export const orderService = {
  createOrder: async (request: CreateOrderRequest): Promise<Order> => {
    const response = await apiClient.post('/api/orders', request)
    return response.data
  },

  getOrders: async (params?: {
    status?: string
    symbol?: string
    page?: number
    size?: number
  }): Promise<{ content: Order[]; totalElements: number }> => {
    const response = await apiClient.get('/api/orders', { params })
    return response.data
  },

  getOrderById: async (orderId: string): Promise<Order> => {
    const response = await apiClient.get(`/api/orders/${orderId}`)
    return response.data
  },

  cancelOrder: async (orderId: string): Promise<Order> => {
    const response = await apiClient.post(`/api/orders/${orderId}/cancel`)
    return response.data
  },
}
