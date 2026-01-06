import {API_post, API_get, API_put, API_delete} from '../index.js'

/**
 * 获取灵感池笔记
 * @param pageable 分页参数
 * @returns {Promise<unknown>} 分页结果
 */
export const getInboxNotes = (pageable) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        if (pageable) {
            if (pageable.page !== undefined) params.append('page', pageable.page)
            if (pageable.size !== undefined) params.append('size', pageable.size)
            if (pageable.sort) params.append('sort', pageable.sort)
        }
        const query = params.toString()
        const url = `/student/learning/notes/inbox${query ? `?${query}` : ''}`

        API_get(url,
            (data) => resolve(data),
            (message, code) => reject(new Error(message || `获取灵感池笔记失败(${code})`)))
    })
}

/**
 * 创建灵感池笔记
 * @param noteData 笔记数据
 * @returns {Promise<unknown>} 笔记数据
 */
export const createInboxNote = (noteData) => {
    return new Promise((resolve, reject) => {
        API_post('/student/learning/note/inbox',
            noteData,
            (data) => resolve(data),
            (message, code) => reject(new Error(message || `创建灵感池笔记失败(${code})`)))
    })
}
/**
 * 归档灵感池笔记
 * @param noteId 笔记ID
 * @param archiveData 归档数据
 * @returns {Promise<unknown>} 归档结果
 */
export const archiveNote = (noteId, archiveData) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        if (archiveData) {
            if (archiveData.courseId) params.append('courseId', archiveData.courseId)
            if (archiveData.chapterId) params.append('chapterId', archiveData.chapterId)
            if (archiveData.sectionId) params.append('sectionId', archiveData.sectionId)
        }
        const query = params.toString()
        const url = `/student/learning/notes/${noteId}/archive${query ? `?${query}` : ''}`

        API_put(url,
            {},
            (data) => resolve(data),
            (message, code) => reject(new Error(message || `归档笔记失败(${code})`)))
    })
}

