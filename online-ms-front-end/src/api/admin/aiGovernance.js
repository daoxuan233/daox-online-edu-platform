import { API_get, API_post } from '@/api/index.js'

// 统一管理后端的API基地址
const API_BASE_URL = 'http://localhost:8088/api/admin/ai/governance';

/**
 * 将查询参数对象拼接为查询字符串。
 *
 * @param {Record<string, any>} params 查询参数对象
 * @returns {string} 查询字符串
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
 * 将基于回调的 GET 请求包装为 Promise。
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
 * 获取 AI 治理总览数据。
 *
 * @returns {Promise<any>} 总览数据
 */
export const getAiGovernanceOverview = () => requestGet(`${API_BASE_URL}/overview`)

/**
 * 查询 AI 调用记录。
 *
 * @param {Record<string, any>} params 查询条件
 * @returns {Promise<any[]>} 调用记录列表
 */
export const getAiGovernanceRecords = (params = {}) => {
  return requestGet(`${API_BASE_URL}/records${buildQueryString(params)}`)
}

/**
 * 获取 AI 配额策略列表。
 *
 * @returns {Promise<any[]>} 配额策略列表
 */
export const getAiQuotaPolicies = () => requestGet(`${API_BASE_URL}/quota-policies`)

/**
 * 保存单条 AI 配额策略。
 *
 * @param {Record<string, any>} payload 策略请求体
 * @returns {Promise<any>} 保存后的策略对象
 */
export const saveAiQuotaPolicy = (payload) => requestPost(`${API_BASE_URL}/quota-policies`, payload)

/**
 * 获取当前运行时治理策略。
 *
 * @returns {Promise<any>} 运行时策略
 */
export const getAiRuntimeStrategy = () => requestGet(`${API_BASE_URL}/runtime-strategy`)

/**
 * 保存运行时治理策略。
 *
 * @param {Record<string, any>} payload 运行时策略请求体
 * @returns {Promise<any>} 保存后的运行时策略
 */
export const saveAiRuntimeStrategy = (payload) => requestPost(`${API_BASE_URL}/runtime-strategy`, payload)

/**
 * 提交 AI 调用记录审查结论。
 *
 * @param {string} recordId 记录 ID
 * @param {Record<string, any>} payload 审查请求体
 * @returns {Promise<any>} 更新后的调用记录
 */
export const reviewAiGovernanceRecord = (recordId, payload) => {
  return requestPost(`${API_BASE_URL}/reviews/${recordId}`, payload)
}
