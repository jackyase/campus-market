import request from './request'

export const getNotifications = (page = 0, size = 20) =>
  request.get('/notifications', { params: { page, size } })
export const getUnreadCount = () => request.get('/notifications/unread-count')
export const markNotificationRead = (id) => request.put(`/notifications/${id}/read`)
export const markAllNotificationsRead = () => request.put('/notifications/read-all')
