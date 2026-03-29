import { API_get, API_post } from './index.js'

/**
 * 统一封装基于回调风格的 GET 请求。
 *
 * @param {string} url 请求地址
 * @returns {Promise<any>} 响应数据
 */
const requestGet = (url) => new Promise((resolve, reject) => {
  API_get(
    url,
    (data) => resolve(data),
    (message) => reject(new Error(message))
  )
})

/**
 * 统一封装基于回调风格的 POST 请求。
 *
 * @param {string} url 请求地址
 * @returns {Promise<any>} 响应数据
 */
const requestPost = (url) => new Promise((resolve, reject) => {
  API_post(
    url,
    {},
    (data) => resolve(data),
    (message) => reject(new Error(message))
  )
})

/**
 * 获取当前登录用户的通知列表。
 *
 * @param {{pageNum?: number, pageSize?: number, unreadOnly?: boolean, notificationType?: string}} params 查询参数
 * @returns {Promise<object>} 通知分页数据
 */
export const getNotificationPage = (params = {}) => {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, String(value))
  })
  const queryString = searchParams.toString()
  return requestGet(`/notifications${queryString ? `?${queryString}` : ''}`)
}

/**
 * 获取当前登录用户的未读通知数量。
 *
 * @returns {Promise<{unreadCount: number}>} 未读数量
 */
export const getUnreadNotificationCount = () => requestGet('/notifications/unread-count')

/**
 * 将指定通知标记为已读。
 *
 * @param {string} notificationId 通知 ID
 * @returns {Promise<any>} 操作结果
 */
export const markNotificationAsRead = (notificationId) => requestPost(`/notifications/${notificationId}/read`)

/**
 * 将当前用户的全部通知标记为已读。
 *
 * @returns {Promise<{updatedCount: number}>} 更新结果
 */
export const markAllNotificationsAsRead = () => requestPost('/notifications/read-all')