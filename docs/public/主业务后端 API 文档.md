# 在线教育平台 - 主业务后端 API 文档

| Version | Update Time | Status | Author | Description |
| --- | --- | --- | --- | --- |
| v2026-04-02 | 2026-04-02 | curated | @daoxuan233 | 面向公开仓库整理的主业务后端接口说明 |

> 说明
>
> - 本文档对应模块 `OnlineMSBackEnd`
> - 默认示例地址使用 `http://localhost:8080`
> - 内容基于当前仓库中已验证的控制器路由整理，不再直接展示完整 smart-doc 导出结果
> - 覆盖公共接口、学生端、教师端、管理端和统一通知中心的主入口能力

## 服务信息

| 项目 | 说明 |
| --- | --- |
| 服务模块 | `OnlineMSBackEnd` |
| 默认端口 | `8080` |
| 鉴权方式 | `Authorization: Bearer <token>` |
| 主要响应形式 | 统一 JSON 包装对象 |
| 主要路由前缀 | `/api/public`、`/api/student/**`、`/api/teacher/**`、`/api/admin/**`、`/api/notifications` |

## 返回与鉴权约定

- 除公开接口外，多数接口依赖 JWT 身份信息。
- 主业务后端多数控制器使用统一 `RestBean<T>` 响应包装。
- 文件上传接口通常使用 `multipart/form-data`。
- 分页接口常见参数为 `pageNum`、`pageSize` 或 `page`、`size`，聊天历史接口还会使用时间上界 `before`。

## 公共接口

公共接口前缀统一为 `/api/public`，用于匿名访问或通用能力调用。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/upload` | 通用文件上传入口 |
| GET | `/download` | 通过对象名下载文件 |
| GET | `/file/**` | 代理访问对象存储中的公开文件 |
| GET | `/courses` | 分页查询公开课程列表 |
| GET | `/courses/{courseId}` | 获取课程基础详情 |
| GET | `/categories` | 获取课程分类树 |
| GET | `/categories/tree` | 获取前端组件友好的分类树 |
| GET | `/announcements` | 获取系统公告列表 |
| POST | `/upload/avatar` | 当前登录用户上传头像 |
| GET | `/auth/ask-code` | 请求注册/重置/激活验证码 |
| POST | `/auth/register` | 邮箱注册 |
| POST | `/reset-confirm` | 密码重置确认 |
| POST | `/reset-password` | 重置密码 |
| POST | `/update-password` | 已登录用户修改密码 |
| GET | `/courses/ratings/statistics` | 查询课程评分统计 |
| GET | `/teachers/ratings/statistics` | 查询教师评分统计 |
| GET | `/courses/top-rated` | 获取高评分课程排行 |
| GET | `/courses/ratings/permission` | 检查当前用户是否可评价课程 |

## 学生端接口

### 账户与资料

学生资料接口前缀为 `/api/student`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/profile` | 获取学生个人资料 |
| POST | `/avatar` | 更新头像 |
| POST | `/update` | 更新基础资料 |

### 选课与课程浏览

学生课程接口前缀为 `/api/student/course`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/list` | 获取我的课程列表 |
| GET | `/search/keyword` | 按关键字搜索课程 |
| GET | `/search/category` | 按分类查询课程 |
| GET | `/search/level` | 按难度查询课程 |
| GET | `/courses/detail` | 获取课程详情 |
| POST | `/enroll` | 报名课程 |
| POST | `/unenroll` | 取消选课 |
| GET | `/getCourseChapters` | 获取课程章节信息 |
| GET | `/completed-courses-count` | 获取已完成课程数量 |

### 学习与笔记

学生学习接口前缀为 `/api/student/learning`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/video` | 获取学习视频/节内容 |
| POST | `/update` | 更新学习进度 |
| GET | `/files/{fileId}` | 下载学习文件 |
| GET | `/{courseId}/content` | 获取课程学习内容总览 |
| GET | `/materials/all` | 获取课程全部资料 |
| GET | `/materials/chapters` | 按章节查询资料 |
| GET | `/materials/sections` | 按小节查询资料 |
| GET | `/notes` | 查询笔记列表 |
| GET | `/notes/course` | 按课程查询笔记 |
| GET | `/notes/inbox` | 查询收纳箱笔记 |
| PUT | `/notes/{id}/archive` | 归档笔记 |
| GET | `/notes/course/specify` | 查询指定课程笔记 |
| GET | `/notes/course/chapter` | 查询章节笔记 |
| GET | `/notes/course/sections` | 查询小节笔记 |
| POST | `/create/note` | 创建课程笔记 |
| POST | `/note/free` | 创建自由笔记 |
| POST | `/note/inbox` | 创建收纳箱笔记 |
| POST | `/note/{id}` | 查看或更新单条笔记 |
| POST | `/update/note` | 更新笔记 |
| GET | `/total/time` | 获取学习总时长 |
| GET | `/overall/progress` | 获取整体学习进度 |

### 测评与答题

学生测评接口前缀为 `/api/student/assessments`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/list` | 获取我的测评列表 |
| GET | `/{assessmentId}` | 获取测评详情 |
| GET | `/can-start/{assessmentId}` | 检查是否可开始作答 |
| POST | `/start` | 创建测评会话 |
| GET | `/{assessmentId}/paper` | 获取学生试卷 |
| POST | `/{assessmentId}/answer` | 保存单题答案 |
| POST | `/{assessmentId}/submit` | 提交整份试卷 |

### 讨论区

学生讨论接口前缀为 `/api/student/discussions`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/course/{courseId}` | 获取课程讨论概览 |
| GET | `/course/{courseId}/pinned` | 获取置顶讨论 |
| GET | `/course/{courseId}/questions` | 获取问题列表 |
| GET | `/course/{courseId}/questions/unresolved` | 获取未解决问题 |
| POST | `` | 创建讨论主题 |
| GET | `/{id}` | 获取讨论详情 |
| GET | `/{id}/replies` | 获取回复列表 |
| POST | `/{id}/replies` | 发表回复 |
| POST | `/replies/{id}/like` | 点赞回复 |

### 单聊与好友

学生私聊接口前缀为 `/api/student/chat`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/friend` | 获取好友列表 |
| GET | `/history` | 获取聊天会话列表 |
| GET | `/history/detail` | 获取与指定好友的聊天记录 |
| POST | `/read` | 将指定好友会话标记已读 |
| GET | `/unread-count` | 获取私聊未读总数 |
| POST | `/friend` | 通过学号/工号查询好友 |
| POST | `/friend/add` | 发送好友申请 |
| POST | `/friend/confirm` | 确认好友申请 |
| GET | `/friend/pending` | 获取待处理好友申请 |
| GET | `/friend/pending/count` | 获取待处理好友申请数量 |

### 课程群聊

学生课程群聊接口前缀为 `/api/student/group-chat`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/courses` | 获取可访问的课程群列表 |
| GET | `/course/{courseId}` | 获取课程群详情 |
| GET | `/course/{courseId}/messages` | 获取课程群历史消息 |
| GET | `/course/{courseId}/members` | 获取课程群成员列表 |
| POST | `/course/{courseId}/read` | 将群聊标记为已读 |

## 教师端接口

### 账户与工作台

教师基础接口前缀为 `/api/teacher` 与 `/api/teacher/dashboard`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/teacher/profile` | 获取教师资料 |
| POST | `/api/teacher/avatar` | 更新教师头像 |
| GET | `/api/teacher/dashboard/weekly-overview` | 获取教师周概览 |

### 课程与大纲管理

教师课程接口前缀为 `/api/teacher/courses` 与 `/api/teacher/courses/chapters`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| ANY | `/api/teacher/courses/list` | 获取我的课程列表 |
| POST | `/api/teacher/courses` | 创建课程 |
| POST | `/api/teacher/courses/{courseId}` | 更新课程核心信息 |
| POST | `/api/teacher/courses/{courseId}/outline` | 全量更新课程大纲 |
| GET | `/api/teacher/courses/{courseId}/outline` | 获取课程大纲 |
| POST | `/api/teacher/courses/update/cover` | 修改课程封面地址 |
| POST | `/api/teacher/courses/detail` | 获取课程详情 |
| POST | `/api/teacher/courses/course/properties` | 更新课程及属性信息 |
| POST | `/api/teacher/courses/course/delete` | 删除课程 |
| POST | `/api/teacher/courses/course/review/submit` | 提交课程审核 |
| POST | `/api/teacher/courses/course/publish` | 兼容旧入口，语义同提交审核 |
| POST | `/api/teacher/courses/course/archive` | 归档课程 |
| POST | `/api/teacher/courses/course/students` | 获取选课学生列表 |
| POST | `/api/teacher/courses/upload/cover` | 上传课程封面 |
| GET | `/api/teacher/courses/chapters/list` | 获取章节列表 |
| POST | `/api/teacher/courses/chapters/create` | 创建章节 |
| POST | `/api/teacher/courses/chapters/updateChapter` | 更新章节 |
| GET | `/api/teacher/courses/chapters/sections` | 获取小节列表 |
| POST | `/api/teacher/courses/chapters/createSection` | 创建小节 |

### 文件管理

教师文件接口挂在 `/api/teacher` 下。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/courses/files/upload` | 上传课程文件 |
| POST | `/materials/upload` | 上传课程资料 |
| GET | `/files/{fileId}` | 下载文件 |

### 题库、试卷与测评

| 前缀 | 说明 |
| --- | --- |
| `/api/teacher/questions` | 题库查询、创建、详情、更新、删除 |
| `/api/teacher/paper` | 创建试卷、按测评获取试卷 |
| `/api/teacher/assessments` | 测评列表、创建、发布、详情、更新、删除、试卷结构、结果汇总 |
| `/api/teacher/grading` | 阅卷任务列表、题目答案拉取、人工评分、测评完结 |

教师测评相关关键路径如下：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/teacher/assessments` | 获取教师测评列表 |
| GET | `/api/teacher/assessments/{courseID}` | 获取某课程测评 |
| POST | `/api/teacher/assessments/create` | 创建测评 |
| POST | `/api/teacher/assessments/publish` | 发布测评 |
| GET | `/api/teacher/assessments/detail` | 获取测评详情 |
| POST | `/api/teacher/assessments/update` | 更新测评 |
| POST | `/api/teacher/assessments/delete` | 删除测评 |
| GET | `/api/teacher/assessments/paper-structure` | 获取试卷结构 |
| GET | `/api/teacher/assessments/{assessmentId}/result-summary` | 获取结果汇总 |
| GET | `/api/teacher/grading/tasks` | 获取待阅卷任务 |
| GET | `/api/teacher/grading/tasks/{assessmentId}` | 获取指定测评阅卷任务详情 |
| GET | `/api/teacher/grading/tasks/{assessmentId}/status` | 获取阅卷状态 |
| GET | `/api/teacher/grading/tasks/{assessmentId}/questions/{questionId}/answers` | 获取题目答案列表 |
| POST | `/api/teacher/grading/answers/grade` | 提交评分 |
| POST | `/api/teacher/grading/tasks/{assessmentId}/finalize` | 完成阅卷 |

### 待办与聊天

| 前缀 | 说明 |
| --- | --- |
| `/api/teacher/todos` | 待办列表、新增、更新、删除 |
| `/api/teacher/chat` | 教师私聊与历史消息 |
| `/api/teacher/group-chat` | 课程群聊、禁言、踢人、群公告 |

教师群聊关键路径如下：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/teacher/group-chat/courses` | 获取课程群列表 |
| GET | `/api/teacher/group-chat/course/{courseId}` | 获取课程群详情 |
| GET | `/api/teacher/group-chat/course/{courseId}/messages` | 获取历史消息 |
| GET | `/api/teacher/group-chat/course/{courseId}/members` | 获取成员列表 |
| POST | `/api/teacher/group-chat/course/{courseId}/read` | 标记已读 |
| GET | `/api/teacher/group-chat/course/{courseId}/mute-members` | 获取禁言名单 |
| GET | `/api/teacher/group-chat/course/{courseId}/mute-all/status` | 获取全员禁言状态 |
| POST | `/api/teacher/group-chat/course/{courseId}/mute` | 禁言单个成员 |
| POST | `/api/teacher/group-chat/course/{courseId}/mute-all` | 全员禁言 |
| DELETE | `/api/teacher/group-chat/course/{courseId}/mute/{targetUserId}` | 解除单个成员禁言 |
| DELETE | `/api/teacher/group-chat/course/{courseId}/mute-all` | 解除全员禁言 |
| POST | `/api/teacher/group-chat/course/{courseId}/members/{targetUserId}/kick` | 移出成员 |
| POST | `/api/teacher/group-chat/course/{courseId}/announcement` | 发布群公告 |

## 管理端接口

### 用户与审计

管理接口前缀为 `/api/admin`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/users` | 获取用户列表 |
| GET | `/user/detail` | 获取用户详情 |
| POST | `/user/status` | 禁用或逻辑删除用户 |
| POST | `/user/restore` | 恢复用户 |
| POST | `/user/create` | 管理员创建用户 |
| POST | `/user/update` | 更新用户基础信息 |
| GET | `/audit-logs` | 查询审计日志 |
| POST | `/user/password` | 重置用户密码 |

### 仪表盘、统计与同步

| 前缀 | 说明 |
| --- | --- |
| `/api/admin/dashboard` | 管理后台概览 |
| `/api/admin/statistics` | 用户与课程统计 |
| `/api/admin/sync` | 手动触发同步任务 |

统计接口的关键路径包括：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/dashboard/overview` | 获取后台概览 |
| GET | `/api/admin/statistics/users` | 全部用户数 |
| GET | `/api/admin/statistics/users/valid` | 有效用户数 |
| GET | `/api/admin/statistics/users/admin` | 管理员数量 |
| GET | `/api/admin/statistics/users/admin/valid` | 有效管理员数量 |
| GET | `/api/admin/statistics/users/teacher` | 教师数量 |
| GET | `/api/admin/statistics/users/teacher/valid` | 有效教师数量 |
| GET | `/api/admin/statistics/users/student` | 学生数量 |
| GET | `/api/admin/statistics/users/student/valid` | 有效学生数量 |
| GET | `/api/admin/statistics/courses` | 课程总数 |
| GET | `/api/admin/statistics/courses/detailed` | 课程详细统计 |
| POST | `/api/admin/sync/start-all` | 启动整体同步任务 |

### 课程审核与分类治理

管理课程接口前缀为 `/api/admin/course`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/list` | 获取全部课程 |
| GET | `/review/pending` | 获取待审核课程 |
| POST | `/review/approve` | 审核通过课程 |
| POST | `/review/reject` | 驳回课程审核 |
| POST | `/take-down` | 下架课程 |
| POST | `/republish` | 重新上架课程 |
| POST | `/archive` | 归档课程 |
| POST | `/delete` | 删除课程 |
| GET | `/categories` | 按 ID 查询分类 |
| GET | `/categories/tree` | 获取分类树 |
| POST | `/categories/create` | 创建分类 |
| POST | `/categories/update` | 更新分类 |
| POST | `/categories/delete` | 删除分类 |
| GET | `/categories/delete/preview` | 预览删除影响 |
| POST | `/categories/delete/emergency` | 紧急删除分类 |
| POST | `/categories/delete/regular` | 提交常规删除 |
| GET | `/categories/migration/preview` | 预览分类迁移影响 |
| POST | `/categories/migration/execute` | 执行分类迁移 |

### 系统公告管理

系统公告接口前缀为 `/api/admin/sys`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/notice/list` | 获取公告列表 |
| POST | `/notice/publish` | 发布公告 |
| POST | `/update` | 更新公告 |
| POST | `/deleteSystemAnnouncement` | 删除或过期公告 |

## 统一通知中心

通知中心前缀为 `/api/notifications`，不区分学生和教师，统一依赖当前登录用户身份隔离数据。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `` | 分页查询通知列表 |
| GET | `/unread-count` | 获取未读通知数 |
| POST | `/{notificationId}/read` | 将单条通知标记已读 |
| POST | `/read-all` | 将全部通知标记已读 |

## 调用示例

### 查询公开课程

```bash
curl -X GET \
  'http://localhost:8080/api/public/courses?pageSize=10&offset=0'
```

### 获取学生测评列表

```bash
curl -X GET \
  -H 'Authorization: Bearer <token>' \
  'http://localhost:8080/api/student/assessments/list?status=ONGOING'
```

### 教师提交课程审核

```bash
curl -X POST \
  -H 'Authorization: Bearer <token>' \
  'http://localhost:8080/api/teacher/courses/course/review/submit?courseId=course-001'
```

## 与 AI 服务的关系

- 主业务后端负责用户、课程、学习、测评、聊天、通知、管理后台等核心业务。
- AI 对话、历史会话、标签解析和治理策略位于独立模块 `online-ai`。
- 前端会同时访问主业务后端与 AI 服务，因此建议结合 AI 服务文档一起阅读。

## 相关公开资料

- [公开部署说明](./SETUP.md)
- [AI 服务与治理 API 文档](./AI%20服务与治理%20API%20文档.md)
- [主库结构 SQL](../../sql/MySQL/online_educate.sql)
