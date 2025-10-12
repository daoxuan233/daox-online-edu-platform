# 课程讨论功能 API 文档

## 概述

本文档描述了课程讨论功能的 REST API 接口，提供了获取课程讨论、置顶讨论、提问帖和未解决提问帖的功能。

## 基础信息

- **基础路径**: `/api/student/discussions`
- **认证方式**: JWT Token（通过请求头或请求属性传递）
- **响应格式**: JSON

## API 接口

### 1. 获取课程讨论列表

**接口描述**: 获取指定课程的所有讨论帖，按创建时间倒序排列。

**请求信息**:
- **方法**: `GET`
- **路径**: `/course/{courseId}`
- **参数**:
  - `courseId` (路径参数): 课程ID，必填

**请求示例**:
```http
GET /api/student/discussions/course/12345
Authorization: Bearer <JWT_TOKEN>
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "discussion_001",
      "courseId": "12345",
      "title": "关于第一章的疑问",
      "content": "请问第一章的重点是什么？",
      "author": {
        "userId": "user_001",
        "username": "张三",
        "avatar": "avatar_url"
      },
      "isPinned": false,
      "isQuestion": true,
      "isResolved": false,
      "votes": 5,
      "replies": [],
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "message": "success"
}
```

### 2. 获取课程置顶讨论

**接口描述**: 获取指定课程的置顶讨论帖。

**请求信息**:
- **方法**: `GET`
- **路径**: `/course/{courseId}/pinned`
- **参数**:
  - `courseId` (路径参数): 课程ID，必填

**请求示例**:
```http
GET /api/student/discussions/course/12345/pinned
Authorization: Bearer <JWT_TOKEN>
```

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "discussion_002",
      "courseId": "12345",
      "title": "课程重要通知",
      "content": "请大家注意课程安排变更...",
      "author": {
        "userId": "teacher_001",
        "username": "李老师",
        "avatar": "teacher_avatar_url"
      },
      "isPinned": true,
      "isQuestion": false,
      "isResolved": null,
      "votes": 15,
      "replies": [],
      "createdAt": "2024-01-10T09:00:00Z"
    }
  ],
  "message": "success"
}
```

### 3. 获取课程提问帖

**接口描述**: 获取指定课程的所有提问帖。

**请求信息**:
- **方法**: `GET`
- **路径**: `/course/{courseId}/questions`
- **参数**:
  - `courseId` (路径参数): 课程ID，必填

**请求示例**:
```http
GET /api/student/discussions/course/12345/questions
Authorization: Bearer <JWT_TOKEN>
```

### 4. 获取未解决的提问帖

**接口描述**: 获取指定课程中尚未解决的提问帖。

**请求信息**:
- **方法**: `GET`
- **路径**: `/course/{courseId}/questions/unresolved`
- **参数**:
  - `courseId` (路径参数): 课程ID，必填

**请求示例**:
```http
GET /api/student/discussions/course/12345/questions/unresolved
Authorization: Bearer <JWT_TOKEN>
```

## 响应状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 401 | 用户未认证 |
| 500 | 服务器内部错误 |

## 数据模型

### Discussion 讨论实体

```json
{
  "id": "string",              // 讨论ID
  "courseId": "string",        // 课程ID
  "title": "string",           // 讨论标题
  "content": "string",         // 讨论内容
  "author": {                   // 作者信息
    "userId": "string",        // 用户ID
    "username": "string",      // 用户名
    "avatar": "string"         // 头像URL
  },
  "isPinned": "boolean",       // 是否置顶
  "isQuestion": "boolean",     // 是否为提问
  "isResolved": "boolean",     // 是否已解决（仅提问帖有效）
  "votes": "number",           // 点赞数
  "replies": [                 // 回复列表
    {
      "id": "string",          // 回复ID
      "content": "string",     // 回复内容
      "author": {               // 回复作者
        "userId": "string",
        "username": "string",
        "avatar": "string"
      },
      "createdAt": "string"    // 回复时间
    }
  ],
  "createdAt": "string"        // 创建时间
}
```

## 错误处理

所有接口在发生错误时都会返回统一的错误响应格式：

```json
{
  "code": 401,
  "data": null,
  "message": "用户未认证"
}
```

## 使用说明

1. **认证要求**: 所有接口都需要用户认证，请确保在请求头中包含有效的 JWT Token。

2. **参数验证**: 课程ID不能为空，否则会返回空列表。

3. **排序规则**: 所有讨论列表都按创建时间倒序排列（最新的在前）。

4. **权限控制**: 当前版本所有已认证用户都可以查看课程讨论，未来可能会增加课程权限验证。

5. **性能考虑**: 当前版本返回完整的讨论列表，未来可能会增加分页功能以提高性能。

## 技术实现

### 架构设计
- **Controller层**：处理HTTP请求，参数验证，异常处理
- **Service层**：业务逻辑处理，权限验证，数据访问
- **Repository层**：MongoDB数据访问

### 权限验证机制
- **用户认证**：通过JWT认证验证用户身份
- **课程权限验证**：使用`coursesMapper.isUserEnrolledInCourse(userId, courseId)`验证用户是否加入课程
- **访问控制**：只有加入课程的用户才能访问该课程的讨论内容
- **用户ID获取**：通过`UserUtils.getCurrentUserId(request)`从请求中获取当前用户ID

### 数据访问
- 使用Spring Data MongoDB进行数据访问
- 支持复杂查询条件（课程ID、置顶状态、提问状态、解决状态）
- 按创建时间倒序排列

### 安全性
- 双重验证：JWT认证 + 课程权限验证
- 防止用户访问未授权的课程讨论
- 记录详细的操作日志，包含用户ID和课程ID

### 技术栈
- **框架**: Spring Boot + Spring Web
- **数据库**: MongoDB
- **认证**: JWT + Spring Security
- **日志**: SLF4J + Logback
- **文档**: 符合项目全局一致性规范

## 后续扩展

计划中的功能扩展：
1. 分页查询支持
2. 讨论内容搜索
3. 按标签筛选
4. 讨论点赞/取消点赞
5. 回复功能
6. 讨论创建和编辑
7. 权限控制优化