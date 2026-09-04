import request from './request'

export const getCart = () => request.get('/cart')
export const addCart = (productId, quantity = 1) =>
  request.post('/cart', null, { params: { productId, quantity } })
export const updateCart = (id, quantity) =>
  request.put(`/cart/${id}`, null, { params: { quantity } })
export const removeCart = (id) => request.delete(`/cart/${id}`)
