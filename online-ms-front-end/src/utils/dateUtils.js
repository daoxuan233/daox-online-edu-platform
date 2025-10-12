/**
 * 格式化时间函数
 * @param dateTimeStr 时间字符串
 * @returns {string} 格式化后的时间字符串
 */
export const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return '未知'
    const date = new Date(dateTimeStr)
    if (isNaN(date.getTime())) return '未知'
    // 格式化为 yyyy-MM-dd HH:mm:ss
    const pad = (n) => n.toString().padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}
