import request from './request'

export const getPosts = (params) => request.get('/posts', { params })
export const createPost = (data) => request.post('/posts', data)
export const deletePost = (id) => request.delete(`/posts/${id}`)
export const likePost = (id) => request.post(`/posts/${id}/like`)
export const getComments = (postId) => request.get(`/posts/${postId}/comments`)
export const addComment = (postId, content) => request.post(`/posts/${postId}/comments`, { content })
export const deleteComment = (id) => request.delete(`/comments/${id}`)
export const followUser = (id) => request.post(`/users/${id}/follow`)
export const getUserPosts = (id) => request.get(`/users/${id}/posts`)
