import request from './request'

export const createBargain = (data) => request.post('/bargains', data)
export const getMyBargains = () => request.get('/bargains/mine')
export const getIncomingBargains = () => request.get('/bargains/incoming')
export const acceptBargain = (id) => request.post(`/bargains/${id}/accept`)
export const rejectBargain = (id) => request.post(`/bargains/${id}/reject`)
