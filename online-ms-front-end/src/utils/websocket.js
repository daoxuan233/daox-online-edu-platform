import axios from 'axios'
import { takeAccessToken } from '@/api/index.js'
import { ElMessage } from 'element-plus'

/**
 * 解析后端 WebSocket 基础地址。
 *
 * @returns {string} 例如 ws://localhost:8080
 */
function resolveWebSocketBaseUrl() {
    try {
        const baseUrl = axios.defaults.baseURL || window.location.origin
        const parsedUrl = new URL(baseUrl, window.location.origin)
        const protocol = parsedUrl.protocol === 'https:' ? 'wss:' : 'ws:'
        return `${protocol}//${parsedUrl.host}`
    } catch (error) {
        console.warn('解析 WebSocket 基础地址失败，回退到当前站点:', error)
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        return `${protocol}//${window.location.host}`
    }
}

/**
 * 创建带鉴权能力的 WebSocket 连接。
 *
 * @param {string} path 端点路径
 * @param {object} handlers 生命周期回调
 * @param {object} options 连接提示配置
 * @returns {WebSocket|null} WebSocket 实例
 */
function createAuthenticatedWebSocket(path, handlers = {}, options = {}) {
    const token = takeAccessToken()

    if (!token) {
        ElMessage.error('错误：未找到登录凭证，无法建立 WebSocket 连接。')
        console.error('WebSocket connection failed: No access token found.')
        return null
    }

    const socket = new WebSocket(`${resolveWebSocketBaseUrl()}${path}`, [token])
    const mergedOptions = {
        showConnectedMessage: false,
        showDisconnectedMessage: false,
        showErrorMessage: true,
        connectedMessage: 'WebSocket 已连接。',
        disconnectedMessage: 'WebSocket 已断开。',
        errorMessage: 'WebSocket 连接失败。',
        ...options
    }

    socket.onopen = (event) => {
        console.log('WebSocket 连接已成功建立:', path)
        if (mergedOptions.showConnectedMessage) {
            ElMessage.success(mergedOptions.connectedMessage)
        }
        handlers.onOpen?.(event, socket)
    }

    socket.onmessage = (event) => {
        console.log('收到 WebSocket 消息:', event.data)
        handlers.onMessage?.(event, socket)
    }

    socket.onclose = (event) => {
        console.log('WebSocket 连接已关闭:', event)
        if (mergedOptions.showDisconnectedMessage && !event.wasClean) {
            ElMessage.warning(mergedOptions.disconnectedMessage)
        }
        handlers.onClose?.(event, socket)
    }

    socket.onerror = (error) => {
        console.error('WebSocket 发生错误:', error)
        if (mergedOptions.showErrorMessage) {
            ElMessage.error(mergedOptions.errorMessage)
        }
        handlers.onError?.(error, socket)
    }

    return socket
}

/**
 * 创建单聊 WebSocket 连接。
 *
 * @param {Function} onMessageCallback 收到消息后的回调
 * @returns {WebSocket|null} WebSocket 实例
 */
function createWebSocket(onMessageCallback) {
    return createAuthenticatedWebSocket('/single', {
        onMessage: (event) => {
            if (onMessageCallback) {
                onMessageCallback(event.data)
            }
        }
    }, {
        showConnectedMessage: true,
        showDisconnectedMessage: true,
        showErrorMessage: true,
        connectedMessage: '实时通知服务已连接！',
        disconnectedMessage: '实时通知服务已断开，请刷新页面重试。',
        errorMessage: '实时通知服务连接失败。'
    })
}

/**
 * 创建课程群聊 WebSocket 连接。
 *
 * @param {string} courseId 课程ID
 * @param {object} handlers 生命周期回调
 * @returns {WebSocket|null} WebSocket 实例
 */
function createGroupChatWebSocket(courseId, handlers = {}) {
    if (!courseId) {
        ElMessage.error('课程群聊连接缺少课程 ID。')
        return null
    }

    return createAuthenticatedWebSocket(`/group/${courseId}`, handlers, {
        showConnectedMessage: false,
        showDisconnectedMessage: false,
        showErrorMessage: false
    })
}

export { createWebSocket, createGroupChatWebSocket }