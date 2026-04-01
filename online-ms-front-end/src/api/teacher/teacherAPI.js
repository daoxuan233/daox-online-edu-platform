import {API_post, API_get, API_put, API_delete, getCurrentUserId} from '../index.js'

/**
 * 修改头像
 * @param avatarUrl - 头像地址
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateAvatarTeacher = (avatarUrl) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('avatarUrl', avatarUrl)

        API_post('/teacher/avatar',
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
 * 获取教师个人信息
 * @param {string} identifier - 工号
 * @param {function} success - 成功回调函数
 * @param {function} [failure] - 失败回调函数（可选）
 */
export function getTeacherProfile(identifier, success, failure) {
    API_get(`/teacher/profile?identifier=${identifier}`, success, failure)
}

/**
 * 获取当前用户课程列表
 * @param {function} success - 成功回调函数
 * @param {function} [failure] - 失败回调函数（可选）
 */
export function getMyCourseList(success, failure) {
    API_get('/teacher/courses/list', success, failure)
}

/**
 * 获取教师待办事项列表
 * @returns {Promise<Array>} - Promise对象，返回待办事项数组
 */
export const getTeacherTodoList = () => {
    return new Promise((resolve, reject) => {
        API_get('/teacher/todos',
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
 * 获取教师工作台本周概览数据
 * @returns {Promise<object>} - Promise对象，返回本周概览统计结果
 */
export const getTeacherWeeklyOverview = () => {
    return new Promise((resolve, reject) => {
        API_get('/teacher/dashboard/weekly-overview',
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
 * 新增教师待办事项
 * @param {object} todoData - 待办事项数据
 * @returns {Promise<object>} - Promise对象，返回新增后的待办事项
 */
export const createTeacherTodo = (todoData) => {
    return new Promise((resolve, reject) => {
        API_post('/teacher/todos',
            todoData,
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
 * 更新教师待办事项
 * @param {string} todoId - 待办事项ID
 * @param {object} todoData - 待办事项更新数据
 * @returns {Promise<object>} - Promise对象，返回更新后的待办事项
 */
export const updateTeacherTodo = (todoId, todoData) => {
    return new Promise((resolve, reject) => {
        API_put(`/teacher/todos/${todoId}`,
            todoData,
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
 * 删除教师待办事项
 * @param {string} todoId - 待办事项ID
 * @returns {Promise<boolean>} - Promise对象，返回删除结果
 */
export const deleteTeacherTodo = (todoId) => {
    return new Promise((resolve, reject) => {
        API_delete(`/teacher/todos/${todoId}`,
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
 * 上传课程封面
 * @param file  - 文件
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateCoverTeacher = (file) => {
    const formData = new FormData()
    formData.append('file', file)

    return new Promise((resolve, reject) => {
        API_post('/teacher/courses/upload/cover', formData,
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
        API_get('/teacher/chat/history',
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
        API_get('/teacher/chat/friend',
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
        API_get(`/teacher/chat/history/detail?friendId=${friendId}`,
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
        API_post('/teacher/chat/read',
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
 * 修改课程封面
 * @param {string} coverUrl - 课程封面图片URL
 * @param {string} courseId - 课程ID
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateCourseCover = (coverUrl, courseId) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('coverUrl', coverUrl)
        params.append('courseId', courseId)

        API_post('/teacher/courses/update/cover',
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
 * 获取课程详情
 * @param {string} courseId - 课程ID
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseDetail = (courseId) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('courseId', courseId)

        API_post('/teacher/courses/detail',
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
 * 更新课程信息
 * @param {string} courseId - 课程ID
 * @param {object} courseVo - 课程基本信息对象
 * @param {object} coursePropertiesVo - 课程属性对象
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateCourseInfo = (courseId, courseVo, coursePropertiesVo) => {
    return new Promise((resolve, reject) => {
        const params = new URLSearchParams()
        params.append('courseId', courseId)

        // 将对象转换为JSON字符串
        params.append('courseVo', JSON.stringify(courseVo))
        params.append('coursePropertiesVo', JSON.stringify(coursePropertiesVo))

        // params.append('courseVo', courseVo)
        // params.append('coursePropertiesVo', coursePropertiesVo)

        API_post('/teacher/courses/course/properties',
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
 * 创建课程 (课程主体 + 课程属性)
 * @param {object} courseCoreInfo - 包含课程核心信息的 DTO
 * @returns {Promise<unknown>} - Promise对象
 */
export const createCourse = (courseCoreInfo) => {
    return new Promise((resolve, reject) => {
        API_post('/teacher/courses',
            courseCoreInfo,
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
 * 增量更新指定课程的完整大纲 (章节 + 小节)
 * @param {string} courseId - 要更新大纲的课程的 ID
 * @param {object} outlineDto - 包含新的完整大纲结构的 DTO
 * @returns {Promise<unknown>} - Promise对象
 */
export const updateCourseOutline = (courseId, outlineDto) => {
    return new Promise((resolve, reject) => {
        API_post(`/teacher/courses/${courseId}/outline`,
            outlineDto,
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
 * 获取课程大纲
 * @param {string} courseId - 课程ID
 * @returns {Promise<unknown>} - Promise对象
 */
export const getCourseOutline = (courseId) => {
    return new Promise((resolve, reject) => {
        API_get(`/teacher/courses/${courseId}/outline`,
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
 * 上传课程资料
 * @param {object} materialInfo - 资料信息对象
 * @param {File} materialInfo.file - 文件本身
 * @param {string} materialInfo.courseId - 课程ID
 * @param {string} materialInfo.title - 资料标题
 * @param {string} [materialInfo.chapterId] - 章节ID (可选)
 * @param {string} [materialInfo.sectionId] - 小节ID (可选)
 * @param {boolean} [materialInfo.allowDownload=true] - 是否允许下载 (可选)
 * @returns {Promise<unknown>} - Promise对象
 */
export const uploadCourseMaterial = (materialInfo) => {
    return new Promise((resolve, reject) => {
        const formData = new FormData();
        formData.append('file', materialInfo.file);
        formData.append('courseId', materialInfo.courseId);
        formData.append('title', materialInfo.title);

        if (materialInfo.chapterId) {
            formData.append('chapterId', materialInfo.chapterId);
        }
        if (materialInfo.sectionId) {
            formData.append('sectionId', materialInfo.sectionId);
        }
        if (materialInfo.allowDownload !== null && materialInfo.allowDownload !== undefined) {
            formData.append('allowDownload', materialInfo.allowDownload);
        }

        API_post('/teacher/materials/upload',
            formData,
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
 * 创建题目
 * @param {object} questionData - 题目数据, 对应后端 Question 实体
 * @param {string} questionData.courseId - 课程ID
 * @param {string} questionData.creatorId - 创建者ID
 * @param {string} questionData.type - 题目类型 (SINGLE_CHOICE, MULTI_CHOICE, TRUE_FALSE, SHORT_ANSWER, FILL_IN_BLANKS, PROGRAMMING)
 * @param {string} questionData.stem - 题干
 * @param {Array} [questionData.options] - 选项数组 (选择题用)，对象包含key和text
 * @param {string|Array|boolean} questionData.answer - 正确答案
 * @param {string} [questionData.analysis] - 答案解析
 * @param {Array<string>} [questionData.tags] - 标签数组
 * @returns {Promise<unknown>} - Promise对象
 */
export const createQuestion = (questionData) => {
    return new Promise((resolve, reject) => {
        API_post('/teacher/questions/create',
            questionData,
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
 * 获取所有题目
 * @returns {Promise<unknown>} - Promise对象, 返回所有题目的列表 (QuestionVo)
 */
export const getQuestionsList = () => {
    return new Promise((resolve, reject) => {
        API_get('/teacher/questions',
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
 * 统一文件上传接口
 * @param {object} fileInfo - 文件信息对象
 * @param {File} fileInfo.file - 文件本身
 * @param {string} fileInfo.courseId - 课程ID
 * @param {string} fileInfo.title - 资料标题
 * @param {string} [fileInfo.chapterId] - 章节ID (可选)
 * @param {string} [fileInfo.sectionId] - 小节ID (可选)
 * @param {number} [fileInfo.durationSeconds] - 视频时长 (可选)
 * @param {boolean} [fileInfo.allowDownload] - 是否允许下载 (可选)
 * @returns {Promise<unknown>} - Promise对象
 */
export const uploadAndAssociateFile = (fileInfo) => {
    return new Promise((resolve, reject) => {
        const formData = new FormData();
        formData.append('file', fileInfo.file);
        formData.append('courseId', fileInfo.courseId);
        formData.append('title', fileInfo.title);

        if (fileInfo.chapterId) {
            formData.append('chapterId', fileInfo.chapterId);
        }
        if (fileInfo.sectionId) {
            formData.append('sectionId', fileInfo.sectionId);
        }
        if (fileInfo.durationSeconds) {
            formData.append('durationSeconds', fileInfo.durationSeconds);
        }
        if (fileInfo.allowDownload !== null && fileInfo.allowDownload !== undefined) {
            formData.append('allowDownload', fileInfo.allowDownload);
        }

        API_post('/teacher/courses/files/upload',
            formData,
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
 * 创建测评
 * @param {object} assessmentData - 测评创建信息 DTO (Data Transfer Object)
 * @param {string} assessmentData.courseId - 课程ID (必填)
 * @param {string} assessmentData.assessmentType - 测评类型 (必填), 后端要求:`ClassroomExam` 对应 [课堂作业]、`ChapterExam` 对应  [章节作业]、`MidtermExam` 对应  [期中考试]、`FinalExam` 对应  [期末考试]、`homework` 对应  [作业]
 * @param {string} assessmentData.title - 测评标题 (必填), 例如："第一章 数据库基础 课后作业"
 * @param {string} assessmentData.startTime - 测评开始时间 (必填), 需为 'YYYY-MM-DDTHH:mm:ss' 格式的字符串
 * @param {string} assessmentData.endTime - 测评结束时间 (必填), 需为 'YYYY-MM-DDTHH:mm:ss' 格式的字符串
 * @param {number} assessmentData.durationMinutes - 答题时长(分钟) (必填)
 * @param {number} assessmentData.isPublished - 是否发布 (必填), 0-暂存为草稿, 1-正式发布
 * @returns {Promise<unknown>} - Promise对象, 成功时返回创建的测评信息
 */
export const createAssessment = (assessmentData) => {
    return new Promise((resolve, reject) => {
        // 从token自动获取创建者ID
        const creatorId = getCurrentUserId();
        if (!creatorId) {
            reject(new Error('无法获取用户信息，请重新登录'));
            return;
        }

        // 构建完整的请求数据
        const requestData = {
            ...assessmentData,
            creatorId: creatorId
        };

        API_post('/teacher/assessments/create',
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
 * 创建试卷 (定义一次测评活动的具体试卷构成)
 * @param {object} paperData - 试卷数据对象, 对应后端 MongoDB 中的 Paper 文档结构。
 * @param {string} paperData.assessmentId - 关联的测评ID (必填)。将此试卷与一个具体的测评活动绑定。
 * @param {string} paperData.courseId - 课程ID (必填)。
 * @param {string} paperData.creatorId - 创建者ID (必填)。
 * @param {string} [paperData.title] - 试卷标题 (选填)。
 * @param {string} [paperData.description] - 试卷描述 (选填)。
 * @param {number} paperData.totalScore - 试卷总分 (必填)。由所有题目分数累加得到。
 * @param {Array<object>} paperData.sections - 试卷的题目分组列表 (必填)。如果试卷不分组，则列表只包含一个分组元素。
 * @param {string} paperData.sections[].title - 分组标题 (必填), 例如: "第一部分：选择题"。
 * @param {string} [paperData.sections[].description] - 分组描述 (选填)。
 * @param {Array<object>} paperData.sections[].questions - 该分组下的题目列表 (必填)。
 * @param {string} paperData.sections[].questions[].questionId - 题目ID (必填)。关联到 'questions' 集合中的题目 `_id`。
 * @param {number} paperData.sections[].questions[].score - 该题目的分值 (必填)。
 * @param {number} paperData.sections[].questions[].orderIndex - 题目在分组内的显示顺序 (必填, 从0开始)。
 * @returns {Promise<unknown>} - Promise对象, 成功时返回创建后的试卷信息。
 */
export const createPaper = (paperData) => {
    return new Promise((resolve, reject) => {
        API_post('/teacher/paper',
            paperData,
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
 * 获取测评详情
 * 根据测评ID获取单个测评活动的详细信息。
 * @param {string} assessmentId - 测评的唯一ID (必填)
 * @returns {Promise<object>} - Promise对象。成功时，返回一个包含测评详细信息的对象 (对应后端的 Assessments DO 实体)。
 */
export const getAssessmentDetails = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('获取测评详情时，必须提供测评ID'));
        }
        API_get(`/teacher/assessments/detail?assessmentId=${assessmentId}`,
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
 * 获取指定课程下的所有题目
 * 根据课程ID获取该课程关联的所有题目列表。
 * @param {string} courseId - 课程的唯一ID (必填)
 * @returns {Promise<Array<object>>} - Promise对象。成功时，返回一个包含该课程所有题目信息的对象数组 (对应后端的 Question 实体)
 */
export const getCourseQuestions = (courseId) => {
    return new Promise((resolve, reject) => {
        if (!courseId) {
            return reject(new Error('获取课程题目时，必须提供课程ID'));
        }
        API_get(`/teacher/questions/${courseId}`,
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
 * 获取所有测评
 * @returns {Promise<unknown>} - Promise对象。成功时，返回一个包含所有测评信息的对象数组 (对应后端的 AssessmentTeacherDTO )
 */
export const getAllAssessments = () => {
    return new Promise((resolve, reject) => {
        API_get('/teacher/assessments',
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
 * 获取单个测评的结果汇总
 * @param {string} assessmentId - 测评ID（必填）
 * @returns {Promise<object>} Promise对象，返回测评结果汇总
 * @property {string} assessmentId - 测评ID
 * @property {number} actualParticipantCount - 实际参考人数
 * @property {number} shouldParticipantCount - 应参考人数（课程学习人数）
 * @property {number} averageScore - 平均分（测评总分/应参考人数）
 * @property {number} completionRate - 完成率（实际参考人数/应参考人数）
 */
export const getAssessmentResultSummary = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('获取测评结果汇总时，必须提供测评ID'));
        }
        API_get(`/teacher/assessments/${assessmentId}/result-summary`,
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
 * 发布测评
 * 将指定ID的测评状态从未发布更新为已发布。
 * @param {string} assessmentId - 要发布的测评的唯一ID (必填)
 * @returns {Promise<string>} - Promise对象。成功时，返回一个成功消息字符串，例如："发布测评成功"。
 */
// export const publishAssessment = (assessmentId) => {
//     return new Promise((resolve, reject) => {
//         if (!assessmentId) {
//             return reject(new Error('发布测评时，必须提供测评ID'));
//         }
//
//         const params = new URLSearchParams();
//         params.append('assessmentId', assessmentId);
//
//         API_post('/teacher/assessments/publish',
//             params,
//             (data) => {
//                 resolve(data);
//             },
//             (message, code) => {
//                 reject(new Error(message));
//             }
//         );
//     });
// }

/**
 * 发布测评
 * 将指定ID的测评状态从未发布更新为已发布。
 * @param {string} assessmentId - 要发布的测评的唯一ID (必填)
 * @returns {Promise<string>} - Promise对象。成功时，返回一个成功消息字符串，例如："发布测评成功"。
 */
export const publishAssessment = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('发布测评时，必须提供测评ID'));
        }

        // 后端接口使用 @RequestBody PublishAssessmentRequest 接收，发送JSON对象
        API_post('/teacher/assessments/publish',
            { assessmentId: assessmentId },
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
 * 查询测评的试卷
 * @param assessmentId 测评ID
 * @returns {Promise<unknown>} Promise对象
 * @description 根据测评ID查询测评的试卷信息。 (对应后端的 PaperDTO 实体)
 */
export const getPaperByAssessmentId = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('查询试卷时，必须提供测评ID'));
        }
        API_get(`/teacher/paper/${assessmentId}`,
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
 * 获取当前登录教师的所有待批阅测评任务
 * @returns {Promise<Array<object>>} - Promise对象，返回待批阅任务列表 (GradingTaskDTO 数组)
 */
export const getGradingTasks = () => {
    return new Promise((resolve, reject) => {
        API_get('/teacher/grading/tasks',
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
 * 获取单个测评的批阅详情（主观题列表及进度）
 * @param {string} assessmentId - 测评ID。
 * @returns {Promise<object>} - Promise对象，成功时返回阅卷详情。
 * @property {object} assessmentInfo - 测评基本信息。
 * @property {string} assessmentInfo.id - 测评ID。
 * @property {string} assessmentInfo.courseId - 课程ID。
 * @property {string} assessmentInfo.creatorId - 创建者ID。
 * @property {string} assessmentInfo.assessmentType - 测评类型。
 * @property {string} assessmentInfo.title - 测评标题。
 * @property {string} assessmentInfo.startTime - 测评开始时间。
 * @property {string} assessmentInfo.endTime - 测评结束时间。
 * @property {number} assessmentInfo.durationMinutes - 答题时长(分钟)。
 * @property {number} assessmentInfo.isPublished - 是否发布 (0-暂存为草稿，1-正式发布，2-删除)。
 * @property {Array<object>} subjectiveQuestions - 主观题批阅进度列表。
 * @property {string} subjectiveQuestions[].questionId - 题目ID。
 * @property {string} subjectiveQuestions[].stem - 题干预览。
 * @property {number} subjectiveQuestions[].score - 该题总分。
 * @property {number} subjectiveQuestions[].gradedCount - 已批阅数。
 * @property {number} subjectiveQuestions[].totalCount - 总需批阅数。
 */
export const getGradingTaskDetail = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('获取阅卷任务详情时，必须提供测评ID'));
        }
        API_get(`/teacher/grading/tasks/${assessmentId}`,
            (data) => {
                resolve(data);
            },
            (message, code) => {
                reject(new Error(message));
            }
        );
    });
};

export const getGradingTaskStatus = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('获取阅卷任务状态时，必须提供测评ID'));
        }
        API_get(`/teacher/grading/tasks/${assessmentId}/status`,
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
 * 分页获取单个主观题的所有学生答案
 * @param {string} assessmentId - 测评ID
 * @param {string} questionId - 题目ID
 * @param {number} [page=0] - 页码 (从0开始)
 * @param {number} [size=10] - 每页数量
 * @returns {Promise<object>} - Promise对象，成功时返回包含学生答案片段DTO的分页对象
 *   @property {Array<object>} content - 学生答案片段列表
 *     @property {string} content[].submissionId - 作答记录的 id
 *     @property {string} content[].studentId - 学生 id
 *     @property {string} content[].studentName - 学生姓名或其他匿名标识
 *     @property {object} content[].response - 学生对该题的回答
 *     @property {number} content[].currentScore - 当前得分
 *     @property {string} content[].comment - 当前评语
 *   @property {number} totalPages - 总页数
 *   @property {number} totalElements - 总元素数量
 *   @property {number} size - 每页数量
 *   @property {number} number - 当前页码
 */
export const getStudentAnswers = (assessmentId, questionId, page = 0, size = 10) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId || !questionId) {
            return reject(new Error('获取学生答案时，必须提供测评ID和题目ID'));
        }
        API_get(`/teacher/grading/tasks/${assessmentId}/questions/${questionId}/answers?page=${page}&size=${size}`,
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
 * 获取题目详情
 * 根据题目ID获取单个题目的详细信息。
 * @param {string} questionId - 题目的唯一ID (必填)
 * @returns {Promise<object>} - Promise对象。成功时，返回一个包含题目详细信息的对象。
 *   @property {string} id - 题目ID
 *   @property {string} type - 题目类型 (例如: SINGLE_CHOICE, MULTI_CHOICE, TRUE_FALSE, SHORT_ANSWER, FILL_IN_BLANKS, PROGRAMMING)
 *   @property {string} stem - 题干
 *   @property {Array<object>} [options] - 选项列表 (仅适用于选择题)
 *     @property {string} key - 选项的键 (例如: 'A', 'B')
 *     @property {string} text - 选项的文本内容
 */
export const getQuestionDetail = (questionId) => {
    return new Promise((resolve, reject) => {
        if (!questionId) {
            return reject(new Error('获取题目详情时，必须提供题目ID'));
        }
        API_get(`/teacher/questions/detail?questionId=${questionId}`,
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
 * 提交对单个答案的评分
 * @param {object} request - 评分请求体
 * @param {string} request.submissionId - 答卷ID
 * @param {string} request.questionId - 题目ID
 * @param {number} request.score - 分数
 * @param {string} [request.comment] - 评语 (可选)
 * @returns {Promise<object>} - Promise对象，成功时返回包含更新后总分的对象
 *   @property {number} updatedTotalScore - 更新后的答卷总分
 */
export const gradeAnswer = (request) => {
    return new Promise((resolve, reject) => {
        if (!request || !request.submissionId || !request.questionId || request.score === undefined) {
            return reject(new Error('提交评分时，必须提供答卷ID、题目ID和分数'));
        }
        API_post('/teacher/grading/answers/grade',
            request,
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
 * 完成并归档整个测评的批阅工作
 * @param {string} assessmentId - 测评ID
 * @returns {Promise<object>} - Promise对象，成功时返回包含处理结果的对象。
 *   注意: FinalizeGradingResultDTO 的具体结构未知，请根据后端实际定义进行更新。
 */
export const finalizeGrading = (assessmentId) => {
    return new Promise((resolve, reject) => {
        if (!assessmentId) {
            return reject(new Error('完成批阅时，必须提供测评ID'));
        }
        API_post(`/teacher/grading/tasks/${assessmentId}/finalize`,
            {}, // POST请求体为空，因为assessmentId在路径中
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
 * 教师提交课程审核（替代直接发布）
 * @param {string} courseId 课程ID
 */
export const submitCourseReview = (courseId) => {
    return new Promise((resolve, reject) => {
        const formData = new FormData();
        formData.append('courseId', courseId);
        API_post(`/teacher/courses/course/review/submit`, formData,
            (data) => resolve(data),
            (message) => reject(new Error(message))
        );
    });
};

/**
 * 教师下架或归档课程
 * @param {string} courseId 课程ID
 */
export const archiveTeacherCourse = (courseId) => {
    return new Promise((resolve, reject) => {
        const formData = new FormData();
        formData.append('courseId', courseId);
        API_post(`/teacher/courses/course/archive`, formData,
            (data) => resolve(data),
            (message) => reject(new Error(message))
        );
    });
};
