import {API_post, API_get} from './index.js'

/**
 * 上传头像
 * @param file  - 文件
 * @returns {Promise<unknown>} - Promise对象
 */
export const uploadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)

    return new Promise((resolve, reject) => {
        API_post('/public/upload/avatar', formData,
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
 * 获取课程分类信息 - 树形结构
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseCategoryTree = () => {
    return new Promise((resolve, reject) => {
        API_get('/public/categories',
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
 * 获取课程列表
 * @param pageSize - 每页数量
 * @param offset - 偏移量
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseList = (pageSize, offset) => {
    return new Promise((resolve, reject) => {
        API_get(`/public/courses?pageSize=${pageSize}&offset=${offset}`,
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
 * 获取课程分类信息 - 树形结构 - 学生
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseCategoryTreeStu = () => {
    return new Promise((resolve, reject) => {
        API_get('/public/categories/tree',
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}