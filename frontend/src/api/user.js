import request from './request'

export const updateProfile = (data) => request.put('/user/profile', data)
export const getPublicProfile = (id) => request.get(`/users/${id}`)
export const getUserProducts = (id) => request.get(`/users/${id}/products`)
export const getUserReviews = (id) => request.get(`/users/${id}/reviews`)
export const deleteAccount = () => request.delete('/user/account')
