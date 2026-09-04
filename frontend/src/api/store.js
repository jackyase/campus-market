import request from './request'

export const openStore = (data) => request.post('/store/open', data)
export const getMyStore = () => request.get('/store/me')
export const updateStore = (data) => request.put('/store/me', data)
export const getStore = (id) => request.get(`/store/${id}`)
export const closeStore = () => request.post('/store/close')
