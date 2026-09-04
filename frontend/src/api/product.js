import request from './request'

export const getProducts = (params) => request.get('/products', { params })
export const getProduct = (id) => request.get(`/products/${id}`)
export const createProduct = (data) => request.post('/products', data)
export const getMyProducts = () => request.get('/products/mine')
export const updateProductStatus = (id, status) =>
  request.put(`/products/${id}/status`, null, { params: { status } })
