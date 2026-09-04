import request from './request'

export const createOrder = (data) => request.post('/orders', data)
export const checkoutCart = (cartItemIds) => request.post('/orders/checkout', { cartItemIds })
export const getOrders = (params) => request.get('/orders', { params })
export const getOrderCounts = () => request.get('/orders/counts')
export const getOrder = (id) => request.get(`/orders/${id}`)
export const payOrder = (id, method = 'MOCK') =>
  request.post(`/orders/${id}/pay`, null, { params: { method } })
export const cancelOrder = (id) => request.post(`/orders/${id}/cancel`)
export const shipOrder = (id) => request.post(`/orders/${id}/ship`)
export const confirmOrder = (id) => request.post(`/orders/${id}/confirm`)
export const getSoldOrders = () => request.get('/orders/sold')
