/**
 * 角色相关工具函数
 */

/**
 * 将英文角色转换为中文显示名称
 * @param {string} role - 英文角色名称
 * @returns {string} 中文角色显示名称
 */
export const getRoleDisplayName = (role) => {
  const roleMap = {
    'student': '学生',
    'teacher': '教师',
    'admin': '管理员',
    'assistant': '助教'
  }
  return roleMap[role] || role
}

/**
 * 获取所有可用角色列表
 * @returns {Array} 角色列表数组
 */
export const getAllRoles = () => {
  return [
    { key: 'student', name: '学生' },
    { key: 'teacher', name: '教师' },
    { key: 'admin', name: '管理员' },
    { key: 'assistant', name: '助教' }
  ]
}

/**
 * 验证角色是否有效
 * @param {string} role - 角色名称
 * @returns {boolean} 是否为有效角色
 */
export const isValidRole = (role) => {
  const validRoles = ['student', 'teacher', 'admin', 'assistant']
  return validRoles.includes(role)
}