import request from './request'

export const getFavorites = () => request.get('/favorites')
export const getFavoriteIds = () => request.get('/favorites/ids')
export const toggleFavorite = (productId) => request.post(`/favorites/${productId}/toggle`)
