import { API_delete, API_get, API_post } from './index.js'

/**
 * Promise 化 GET 请求。
 *
 * @param {string} url 请求地址
 * @returns {Promise<any>} 接口数据
 */
const getRequest = (url) => new Promise((resolve, reject) => {
    API_get(url,
        (data) => resolve(data),
        (message) => reject(new Error(message))
    )
})

/**
 * Promise 化 POST 请求。
 *
 * @param {string} url 请求地址
 * @param {object} payload 请求体
 * @returns {Promise<any>} 接口数据
 */
const postRequest = (url, payload) => new Promise((resolve, reject) => {
    API_post(url,
        payload,
        (data) => resolve(data),
        (message) => reject(new Error(message))
    )
})

/**
 * Promise 化 DELETE 请求。
 *
 * @param {string} url 请求地址
 * @returns {Promise<any>} 接口数据
 */
const deleteRequest = (url) => new Promise((resolve, reject) => {
    API_delete(url,
        (data) => resolve(data),
        (message) => reject(new Error(message))
    )
})

/**
 * 生成群聊消息历史查询参数。
 *
 * @param {object} options 查询参数
 * @returns {string} 查询字符串
 */
const buildMessageQuery = (options = {}) => {
    const query = new URLSearchParams()
    if (options.before) {
        query.append('before', options.before)
    }
    if (options.limit) {
        query.append('limit', String(options.limit))
    }
    const queryString = query.toString()
    return queryString ? `?${queryString}` : ''
}

/**
 * 构建指定角色的课程群聊接口集合。
 *
 * @param {string} basePath 角色基础路径
 * @returns {object} 群聊接口集合
 */
const createGroupChatApi = (basePath) => ({
    listGroups: () => getRequest(`${basePath}/courses`),
    getGroupDetail: (courseId) => getRequest(`${basePath}/course/${courseId}`),
    getGroupMessages: (courseId, options = {}) => getRequest(`${basePath}/course/${courseId}/messages${buildMessageQuery(options)}`),
    getGroupMembers: (courseId) => getRequest(`${basePath}/course/${courseId}/members`),
    markGroupAsRead: (courseId) => postRequest(`${basePath}/course/${courseId}/read`, {})
})

/**
 * 学生端课程群聊接口。
 */
export const studentGroupChatApi = createGroupChatApi('/student/group-chat')

/**
 * 教师端课程群聊接口。
 */
export const teacherGroupChatApi = {
    ...createGroupChatApi('/teacher/group-chat'),
    getMutedMembers: (courseId) => getRequest(`/teacher/group-chat/course/${courseId}/mute-members`),
    getMuteAllStatus: (courseId) => getRequest(`/teacher/group-chat/course/${courseId}/mute-all/status`),
    muteMember: (courseId, payload) => postRequest(`/teacher/group-chat/course/${courseId}/mute`, payload),
    unmuteMember: (courseId, targetUserId) => deleteRequest(`/teacher/group-chat/course/${courseId}/mute/${targetUserId}`),
    muteAllMembers: (courseId, payload) => postRequest(`/teacher/group-chat/course/${courseId}/mute-all`, payload),
    unmuteAllMembers: (courseId) => deleteRequest(`/teacher/group-chat/course/${courseId}/mute-all`),
    kickMember: (courseId, targetUserId) => postRequest(`/teacher/group-chat/course/${courseId}/members/${targetUserId}/kick`, {}),
    publishAnnouncement: (courseId, payload) => postRequest(`/teacher/group-chat/course/${courseId}/announcement`, payload)
}