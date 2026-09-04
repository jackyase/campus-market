import request from './request'

export const reportTarget = (data) => request.post('/reports', data)
