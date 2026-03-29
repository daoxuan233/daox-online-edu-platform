import { API_get, API_post } from '@/api/index.js'

/**
 * 将普通对象转为查询字符串。
 *
 * @param {Record<string, any>} params 查询参数对象
 * @returns {string} 拼接后的查询字符串
 */
const buildQueryString = (params = {}) => {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    searchParams.append(key, value)
  })
  const queryString = searchParams.toString()
  return queryString ? `?${queryString}` : ''
}

/**
 * 将基于回调的 GET 请求包装为 Promise，方便在 Composition API 中组合调用。
 *
 * @param {string} url 请求地址
 * @returns {Promise<any>} 请求结果
 */
const requestGet = (url) => new Promise((resolve, reject) => {
  API_get(url, resolve, reject)
})

/**
 * 将基于回调的 POST 请求包装为 Promise。
 *
 * @param {string} url 请求地址
 * @param {any} payload 请求体
 * @returns {Promise<any>} 请求结果
 */
const requestPost = (url, payload = {}) => new Promise((resolve, reject) => {
  API_post(url, payload, resolve, reject)
})

/**
 * 获取管理员用户列表。
 *
 * @returns {Promise<any[]>} 用户列表
 */
export const getAdminUsers = () => requestGet('/admin/users')

/**
 * 获取管理员用户详情。
 *
 * @param {string} userId 用户ID
 * @returns {Promise<any>} 用户详情
 */
export const getAdminUserDetail = (userId) => requestGet(`/admin/user/detail${buildQueryString({ userId })}`)

/**
 * 管理员创建用户。
 *
 * @param {Record<string, any>} payload 创建参数
 * @returns {Promise<any>} 创建后的用户
 */
export const createAdminUser = (payload) => requestPost('/admin/user/create', payload)

/**
 * 管理员更新用户。
 *
 * @param {string} userId 用户ID
 * @param {Record<string, any>} payload 更新参数
 * @returns {Promise<any>} 更新后的用户
 */
export const updateAdminUser = (userId, payload) => requestPost(`/admin/user/update${buildQueryString({ userId })}`, payload)

/**
 * 管理员禁用用户。
 *
 * @param {string} userId 用户ID
 * @returns {Promise<any>} 更新后的用户
 */
export const disableAdminUser = (userId) => requestPost(`/admin/user/status${buildQueryString({ userId })}`)

/**
 * 管理员恢复用户。
 *
 * @param {string} userId 用户ID
 * @returns {Promise<any>} 更新后的用户
 */
export const restoreAdminUser = (userId) => requestPost(`/admin/user/restore${buildQueryString({ userId })}`)

/**
 * 管理员重置用户密码。
 *
 * @param {string} userId 用户ID
 * @returns {Promise<string>} 重置结果提示
 */
export const resetAdminUserPassword = (userId) => requestPost(`/admin/user/password${buildQueryString({ userId })}`)

/**
 * 获取管理员用户统计。
 *
 * @returns {Promise<any[]>} 统计结果数组
 */
export const getAdminUserStatistics = () => Promise.all([
  requestGet('/admin/statistics/users/valid'),
  requestGet('/admin/statistics/users/student/valid'),
  requestGet('/admin/statistics/users/teacher/valid'),
  requestGet('/admin/statistics/users/admin/valid')
])

/**
 * 获取管理员课程统计。
 *
 * @returns {Promise<any[]>} 课程统计
 */
export const getAdminCourseStatistics = () => Promise.all([
  requestGet('/admin/statistics/courses'),
  requestGet('/admin/statistics/courses/detailed')
])

/**
 * 获取管理员课程列表。
 *
 * @returns {Promise<any[]>} 课程列表
 */
export const getAdminCourseList = () => requestGet('/admin/course/list')

/**
 * 获取管理员公告列表。
 *
 * @param {{pageNum?: number, pageSize?: number}} params 分页参数
 * @returns {Promise<any[]>} 公告列表
 */
export const getAdminSystemNotices = (params = {}) => requestGet(`/admin/sys/notice/list${buildQueryString(params)}`)

/**
 * 获取管理员课程分类树。
 *
 * @returns {Promise<any[]>} 课程分类树
 */
export const getAdminCategoryTree = () => requestGet('/admin/course/categories/tree')

/**
 * 创建课程分类。
 *
 * @param {Record<string, any>} payload 分类参数
 * @returns {Promise<string>} 后端提示信息
 */
export const createAdminCategory = (payload) => requestPost('/admin/course/categories/create', payload)

/**
 * 更新课程分类。
 *
 * @param {string} id 分类ID
 * @param {Record<string, any>} payload 分类参数
 * @returns {Promise<string>} 后端提示信息
 */
export const updateAdminCategory = (id, payload) => requestPost(`/admin/course/categories/update${buildQueryString({ id })}`, payload)

/**
 * 删除课程分类。
 *
 * @param {string} id 分类ID
 * @returns {Promise<string>} 后端提示信息
 */
export const deleteAdminCategory = (id) => requestPost(`/admin/course/categories/delete${buildQueryString({ id })}`)

/**
 * 预览课程分类删除影响范围。
 *
 * @param {string} id 分类ID
 * @returns {Promise<any>} 删除预览结果
 */
export const previewAdminCategoryDelete = (id) => requestGet(`/admin/course/categories/delete/preview${buildQueryString({ id })}`)

/**
 * 执行课程分类紧急删除。
 *
 * @param {{categoryId: string, reason?: string}} payload 删除请求
 * @returns {Promise<any>} 删除结果
 */
export const emergencyDeleteAdminCategory = (payload) => requestPost('/admin/course/categories/delete/emergency', payload)

/**
 * 提交课程分类常规删除申请。
 *
 * @param {{categoryId: string, reason?: string}} payload 删除请求
 * @returns {Promise<any>} 删除结果
 */
export const regularDeleteAdminCategory = (payload) => requestPost('/admin/course/categories/delete/regular', payload)

/**
 * 预览课程分类迁移影响范围。
 *
 * @param {string} id 分类ID
 * @returns {Promise<any>} 迁移预览结果
 */
export const previewAdminCategoryMigration = (id) => requestGet(`/admin/course/categories/migration/preview${buildQueryString({ id })}`)

/**
 * 执行课程分类迁移。
 *
 * @param {{sourceCategoryId: string, targetCategoryId: string, reason?: string}} payload 迁移请求
 * @returns {Promise<any>} 迁移结果
 */
export const migrateAdminCategoryCourses = (payload) => requestPost('/admin/course/categories/migration/execute', payload)

/**
 * 获取管理员首页概览数据。
 *
 * @param {{days?: number}} params 趋势统计参数
 * @returns {Promise<any>} 管理员首页概览数据
 */
export const getAdminDashboardOverview = (params = {}) => requestGet(`/admin/dashboard/overview${buildQueryString(params)}`)
