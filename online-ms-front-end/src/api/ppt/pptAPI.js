import {API_post, API_get} from '../index.js'

// 统一管理后端的API基地址
const API_BASE_URL = 'http://localhost:8089/api';

/**
 * 与PPT Agent进行聊天交互
 * @param {Object} chatRequest - 聊天请求数据，包含用户消息
 * @param {string} chatRequest.message - 用户发送的聊天消息内容
 * @returns {Promise<Object>} - Promise对象，成功时返回ChatResponse或GenerationTask
 */
export const chatWithPptAgent = (chatRequest) => {
    return new Promise((resolve, reject) => {
        API_post(API_BASE_URL + '/ppt/chat',
            chatRequest, // 直接传递 chatRequest 对象作为请求体
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}

/**
 * 查询PPT生成任务的状态
 * @param {string} taskId - 任务ID
 * @returns {Promise<Object>} - Promise对象，成功时返回TaskStatus对象
 */
export const getPptTaskStatus = (taskId) => {
    return new Promise((resolve, reject) => {
        API_get(API_BASE_URL + `/ppt/task/${taskId}`,
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}

/**
 * 获取PPT Agent服务的健康状态
 * @returns {Promise<Object>} - Promise对象，成功时返回服务状态信息
 */
export const getPptAgentHealth = () => {
    return new Promise((resolve, reject) => {
        API_get(API_BASE_URL + '/ppt/health',
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}

/**
 * 获取所有活动中的PPT生成任务
 * @returns {Promise<Object>} - Promise对象，成功时返回活动任务列表
 */
export const getActivePptTasks = () => {
    return new Promise((resolve, reject) => {
        API_get(API_BASE_URL + '/ppt/tasks/active',
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}
