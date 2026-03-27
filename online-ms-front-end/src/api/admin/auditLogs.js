import { API_get } from '@/api/index.js'

const buildAuditLogQuery = (params = {}) => {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }
    if (Array.isArray(value)) {
      value
        .filter(item => item !== undefined && item !== null && item !== '')
        .forEach(item => query.append(key, item))
      return
    }
    query.append(key, value)
  })
  const queryString = query.toString()
  return queryString ? `?${queryString}` : ''
}

export const getAdminAuditLogs = (params = {}) => {
  return new Promise((resolve, reject) => {
    API_get(`/admin/audit-logs${buildAuditLogQuery(params)}`, resolve, reject)
  })
}
