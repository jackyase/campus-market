import request from './request'

export const createReview = (data) => request.post('/reviews', data)
export const getProductReviews = (productId) => request.get(`/reviews/product/${productId}`)
export const getSellerReviews = (sellerId) => request.get(`/reviews/seller/${sellerId}`)
