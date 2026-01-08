import {API_post, API_get} from '../index.js'

/**
 * 获取学生个人信息
 * @param {string} identifier - 学号/工号
 * @param {function} success - 成功回调函数
 * @param {function} [failure] - 失败回调函数（可选）
 */
export function getStudentProfile(identifier, success, failure) {
    API_get(`/student/profile?identifier=${identifier}`, success, failure)
}

/**
 * 修改头像
 * @param avatarUrl - 头像地址
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateAvatar = (avatarUrl) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('avatarUrl', avatarUrl)

        API_post('/student/avatar',
            params,
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
 * 更新个人资料
 * @param {Object} profileData - 个人资料数据对象
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateProfile = (profileData) => {
    return new Promise((resolve, reject) => {
        API_post('/student/update',
            profileData,
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
 * 获取已完成课程数量
 * @returns {Promise<number>} - Promise对象，返回已完成课程数量
 */
export const getCompletedCoursesCount = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/course/completed-courses-count',
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
 * 获取指定用户的总学习时长
 * @returns {Promise<Object>} - Promise对象，返回包含总学习时长的对象
 */
export const getTotalLearningTime = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/learning/total/time',
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
 * 获取总体学习进度
 * @returns {Promise<number>} - Promise对象，返回总体学习进度百分比
 */
export const getOverallProgress = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/learning/overall/progress',
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
 * 获取我的课程列表
 * @returns {Promise<Array>} - Promise对象，返回我的课程列表
 */
export const getMyCourseList = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/course/list',
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
 * 获取课程详细信息
 * @param {string} courseId - 课程ID
 * @returns {Promise<Object>} - Promise对象，返回课程详细信息
 */
export const getCourseDetail = (courseId) => {
    return new Promise((resolve, reject) => {
        API_get(`/student/course/courses/detail?courseId=${courseId}`,
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
 * 获取好友列表
 * @returns {Promise<Array>} - Promise对象，返回好友列表
 */
export const getFriendList = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/chat/friend',
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
 * 获取用户的聊天会话列表
 * @returns {Promise<Array>} - Promise对象，返回会话列表
 */
export const getHistoryList = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/chat/history',
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
 * 获取和指定对象的聊天记录详情
 * @param {string} friendId - 对方ID
 * @returns {Promise<Array>} - Promise对象，返回聊天记录详情
 */
export const getHistoryDetail = (friendId) => {
    return new Promise((resolve, reject) => {
        API_get(`/student/chat/history/detail?friendId=${friendId}`,
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
 * 将与指定好友的聊天消息标记为已读
 * @param {string} friendId - 好友ID
 * @returns {Promise<void>} - Promise对象
 */
export const markAsRead = (friendId) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('friendId', friendId)
        API_post('/student/chat/read',
            params,
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
 * 课程搜索 - 通过分类
 * @param {string} categoryId - 分类id
 * @returns {Promise<Object>} - Promise对象，返回课程预览信息
 */
export const searchCoursesByCategory = (categoryId) => {
    return new Promise((resolve, reject) => {
        API_get(`/student/course/search/category?categoryId=${categoryId}`,
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
 * 创建学习笔记 - 自由
 * @param {Object} noteData - 笔记数据，对应后端的 CreateNoteDTO
 * @returns {Promise<Object>} - Promise对象，返回创建的笔记信息
 */
export const createFreedomNote = (noteData) => {
    return new Promise((resolve, reject) => {
        API_post('/student/learning/note/free',
            noteData,
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
 * 更新学习笔记 - 覆盖式
 * @param {string} noteId - 笔记ID
 * @param {Object} noteData - 笔记数据，对应后端的 UpdateNoteDTO
 * @returns {Promise<Object>} - Promise对象，返回更新后的笔记信息
 */
export const updateNote = (noteId, noteData) => {
    return new Promise((resolve, reject) => {
        API_post(`/student/learning/note/${noteId}`,
            noteData,
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
 * 获取学习笔记 - 所有
 * @returns {Promise<Array>} - Promise对象，返回该用户的所有学习笔记列表
 */
export const getAllLearningNotes = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/learning/notes',
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
 * 获取一个学生的所有课程笔记 (分页)
 * @param {Object} [pageable] - 分页参数, e.g., { page: 0, size: 15, sort: 'updatedAt,desc' }
 * @returns {Promise<Object>} - Promise对象，返回分页后的课程笔记列表
 */
export const getCourseNotes = (pageable) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams();
        if (pageable) {
            if (pageable.page !== undefined) params.append('page', pageable.page);
            if (pageable.size !== undefined) params.append('size', pageable.size);
            if (pageable.sort) params.append('sort', pageable.sort);
        }

        const queryString = params.toString();
        const url = `/student/learning/notes/course${queryString ? '?' + queryString : ''}`;

        API_get(url,
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
 * 学生端获取课程内容大纲接口
 * @param {string} courseId - 课程ID
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseContent = (courseId) => {
    return new Promise((resolve, reject) => {
        API_get(`/student/learning/${courseId}/content`,
            (data) => {
                resolve(data);
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
}

/**
 * 更新视频学习进度
 * 前端应定期或在关键事件包括但不限于暂停、关闭等时调用此接口，更新用户在课程中的学习进度。
 * @param {string} sectionId - 小节ID
 * @param {number} progressSeconds - 当前观看秒数
 * @returns {Promise<void>} - Promise对象
 */
export const updateVideoProgress = (sectionId, progressSeconds) => {
    return new Promise((resolve, reject) => {
        const requestData = {
            sectionId: sectionId,
            progressSeconds: progressSeconds
        };
        API_post('/student/learning/update',
            requestData,
            (data) => {
                resolve(data);
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
}

/**
 * 获取我的测评列表
 * @param {string} [status] - 测评状态过滤条件（可选），例如 'not_started', 'in_progress', 'submitted', 'graded'。
 * @returns {Promise<Array<object>>} Promise对象，成功时返回一个包含学生测评信息的数组。
 * @property {string} assessmentId - 测评ID。
 * @property {string} title - 测评标题。
 * @property {string} startTime - 测评开始时间 (格式: 'YYYY-MM-DDTHH:mm:ss')。
 * @property {string} endTime - 测评结束时间 (格式: 'YYYY-MM-DDTHH:mm:ss')。
 * @property {number} durationMinutes - 答题时长(分钟)。
 * @property {string} courseId - 考核所属课程ID。
 * @property {string} courseTitle - 课程标题。
 * @property {string} courseCover - 课程封面URL。
 * @property {string} status - 测评状态 ('not_started': 未开始, 'in_progress': 进行中, 'submitted': 已提交, 'graded': 已批阅)。
 * @property {string} submitTime - 测评提交时间 (格式: 'YYYY-MM-DDTHH:mm:ss')。
 * @property {number} score - 测评得分。
 * @property {boolean} canStart - 是否可以开始测评。
 */
export const getMyAssessments = (status) => {
    return new Promise((resolve, reject) => {
        const queryString = status ? `?status=${status}` : '';
        API_get(`/student/assessments/list${queryString}`,
            (data) => {
                resolve(data);
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

/**
 * 开始测评，创建测评会话
 * @param {object} request - 请求体，包含测评ID。
 * @param {string} request.assessmentId - 测评ID。
 * @returns {Promise<void>} - Promise对象，成功时无返回值，失败时返回错误信息。
 */
export const startAssessment = (request) => {
    return new Promise((resolve, reject) => {
        API_post('/student/assessments/start',
            request,
            () => {
                resolve();
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

/**
 * 获取试卷内容
 * @param {string} assessmentId - 测评ID。
 * @returns {Promise<object>} - Promise对象，成功时返回试卷内容。
 * @property {string} id - 试卷ID。
 * @property {string} title - 试卷标题。
 * @property {string} [description] - 试卷描述。
 * @property {number} totalScore - 试卷总分。
 * @property {string} createdAt - 试卷创建时间 (格式: 'YYYY-MM-DDTHH:mm:ss')。
 * @property {string} updatedAt - 试卷最后更新时间 (格式: 'YYYY-MM-DDTHH:mm:ss')。
 * @property {Array<object>} sections - 试卷题目分组列表。
 * @property {string} sections[].title - 分组标题。
 * @property {string} [sections[].description] - 分组描述。
 * @property {Array<object>} sections[].questions - 该分组下的题目列表。
 * @property {object} sections[].questions[].question - 题目信息。
 * @property {string} sections[].questions[].question.id - 题目ID。
 * @property {string} sections[].questions[].question.type - 题目类型 (e.g., 'SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'SHORT_ANSWER')。
 * @property {string} sections[].questions[].question.content - 题目内容。
 * @property {Array<string>} [sections[].questions[].question.options] - 选择题选项列表。
 * @property {Array<string>} [sections[].questions[].question.answer] - 参考答案。
 * @property {number} sections[].questions[].score - 该题目在本试卷中的分值。
 * @property {number} sections[].questions[].orderIndex - 题目在本分组内的显示顺序，从0开始。
 */
export const getPaperContent = (assessmentId) => {
    return new Promise((resolve, reject) => {
        API_get(`/student/assessments/${assessmentId}/paper`,
            (data) => {
                resolve(data);
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

/**
 * 实时保存学生答案
 * @param {string} assessmentId - 测评ID。
 * @param {object} answerData - 包含问题ID和答案的请求体。
 * @param {string} answerData.questionId - 问题ID。
 * @param {string} answerData.response - 学生答案。
 * @returns {Promise<void>} - Promise对象，成功时无返回值，失败时返回错误信息。
 */
export const submitAnswer = (assessmentId, answerData) => {
    return new Promise((resolve, reject) => {
        API_post(`/student/assessments/${assessmentId}/answer`,
            answerData,
            () => {
                resolve();
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

/**
 * 提交试卷
 * @param {string} assessmentId - 测评ID。
 * @returns {Promise<void>} - Promise对象，成功时无返回值，失败时返回错误信息。
 */
export const submitAssessment = (assessmentId) => {
    return new Promise((resolve, reject) => {
        API_post(`/student/assessments/${assessmentId}/submit`,
            {}, // 提交试卷通常不需要请求体，或者请求体为空对象
            () => {
                resolve();
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

/**
 * 查询好友
 * @param {string} friendIdentifier - 好友标识符（学号/工号）
 * @returns {Promise<Object>} - Promise对象，返回查询到的好友信息
 */
export const queryFriend = (friendIdentifier) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('friend_identifier', friendIdentifier)
        API_post('/student/chat/friend',
            params,
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
 * 添加好友
 * @param {string} targetUserId - 好友ID
 * @param {string} [remark] - 好友备注（可选）
 * @returns {Promise<string>} - Promise对象，返回操作结果信息
 */
export const addFriend = (targetUserId, remark) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('targetUserId', targetUserId)
        if (remark) {
            params.append('remark', remark)
        }
        API_post('/student/chat/friend/add',
            params,
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
 * 确认好友申请
 * @param {string} targetUserId - 好友ID
 * @param {string} [remark] - 好友备注（可选）
 * @returns {Promise<string>} - Promise对象，返回操作结果信息
 */
export const confirmFriendRequest = (targetUserId, remark) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('targetUserId', targetUserId)
        if (remark) {
            params.append('remark', remark)
        }
        API_post('/student/chat/friend/confirm',
            params,
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
 * 获取待确认好友申请列表
 * @returns {Promise<Array>} - Promise对象，返回待确认好友申请列表
 */
export const getPendingFriendRequests = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/chat/friend/pending',
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
 * 统计用户待处理好友申请数
 * @returns {Promise<number>} - Promise对象，返回待处理好友申请数
 */
export const getPendingFriendRequestsCount = () => {
    return new Promise((resolve, reject) => {
        API_get('/student/chat/friend/pending/count',
            (data) => {
                resolve(data)
            },
            (message, code) => {
                reject(new Error(message))
            }
        )
    })
}