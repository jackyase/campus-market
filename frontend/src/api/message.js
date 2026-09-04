import request from './request'

export const getConversations = () => request.get('/messages/conversations')
export const getHistory = (otherUserId) => request.get(`/messages/with/${otherUserId}`)
export const sendMessage = (data) => request.post('/messages', data)
export const getUnreadCount = () => request.get('/messages/unread-count')
