# 在线教育平台 - AI 服务与治理 API 文档

| Version | Update Time | Status | Author | Description |
| --- | --- | --- | --- | --- |
| v2026-04-02 | 2026-04-02 | curated | @daoxuan233 | 面向公开仓库整理的 AI 服务接口说明 |

> 说明
>
> - 本文档对应模块 `online-ai`
> - 默认示例地址使用 `http://localhost:8088`
> - 内容基于当前仓库中已验证的控制器路由整理
> - 覆盖管理员 AI 治理、学生 AI、标签工具、教师 AI 助手与优化助手接口

## 服务信息

| 项目 | 说明 |
| --- | --- |
| 服务模块 | `online-ai` |
| 默认端口 | `8088` |
| 鉴权方式 | `Authorization: Bearer <token>` |
| 响应形式 | JSON、SSE 流式响应 |
| 主要路由前缀 | `/api/admin/ai/governance`、`/api/students/ai`、`/api/students/ai/tool`、`/api/teacher/ai` |

## 返回与鉴权说明

- 治理类接口通常返回统一 JSON 包装对象。
- 学生和教师对话接口大量使用 `text/event-stream`，适合前端逐段消费生成内容。
- 会话历史接口用于摘要查询、消息分页和会话删除。
- 调用接口前通常需要在请求头中传递 JWT Token；部分会话接口在用户未认证时会直接返回空列表或未授权结果。

## 管理端 AI 治理接口

治理接口前缀统一为 `/api/admin/ai/governance`，用于配额控制、运行时策略和人工审核。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/overview` | 获取治理总览，包括调用量、待审核记录和平台分布 |
| GET | `/records` | 分页查询 AI 调用记录，支持多条件筛选 |
| GET | `/quota-policies` | 获取配额策略列表 |
| POST | `/quota-policies` | 新增或更新配额策略 |
| GET | `/runtime-strategy` | 获取当前运行时治理策略 |
| POST | `/runtime-strategy` | 保存内容审核、重试和回退配置 |
| POST | `/reviews/{recordId}` | 对指定调用记录提交人工审核结论 |

### 常见请求参数

| 接口 | 关键参数 |
| --- | --- |
| `GET /records` | `userId`、`platform`、`page`、`size`、调用状态、审核状态 |
| `POST /quota-policies` | `scopeType`、`scopeValue`、`dailyLimit`、`hourlyLimit`、`enabled`、`remark` |
| `POST /runtime-strategy` | `reviewEnabled`、`blockedKeywords`、`reviewKeywords`、`maxAttempts`、`fallbackPlatforms` |
| `POST /reviews/{recordId}` | `decision`、`note` |

### 示例

```bash
curl -X GET \
  -H 'Authorization: Bearer <token>' \
  http://localhost:8088/api/admin/ai/governance/overview
```

## 学生端 AI 接口

学生 AI 接口前缀统一为 `/api/students/ai`，覆盖智能问答、结构化流式响应和历史会话管理。

| 方法 | 路径 | 响应类型 | 说明 |
| --- | --- | --- | --- |
| GET | `/chat-stream` | SSE 文本流 | 学生 AI 文本流式问答 |
| GET | `/chat-stream/res` | SSE 结构化流 | 返回 `content` 以及记录信息的结构化流式响应 |
| GET | `/chat/history/summaries` | JSON | 获取当前用户的 AI 会话摘要列表 |
| GET | `/chat/history/messages` | JSON | 分页获取指定会话的消息记录 |
| GET | `/chat/history/{conversationId}` | JSON | 删除指定 AI 会话及其消息 |

### 调用说明

- `question` 是对话接口的核心参数。
- `conversationId` 为可选参数，用于续接已有会话。
- `chat-stream/res` 额外支持 `options` 参数，用于指定模型平台选项。
- `chat/history/messages` 常用分页参数为 `page` 和 `size`。
- 当前删除会话接口使用 `GET /chat/history/{conversationId}` 这一既有路由，前端接入时应按现有实现调用。

### 标签工具接口

学生工具接口前缀为 `/api/students/ai/tool`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/parse-tag` | 解析学习笔记或文本中的标签 |

### 示例

```bash
curl -X GET \
  -H 'Authorization: Bearer <token>' \
  'http://localhost:8088/api/students/ai/chat-stream?question=请帮我总结本节重点&conversationId=conv-001'
```

## 教师端 AI 接口

教师端 AI 接口前缀统一为 `/api/teacher/ai`，覆盖通用问答、历史会话以及课程优化辅助。

| 方法 | 路径 | 响应类型 | 说明 |
| --- | --- | --- | --- |
| GET | `/chat-stream` | SSE 文本流 | 教师 AI 文本流式问答 |
| GET | `/chat/history/summaries` | JSON | 获取教师 AI 会话摘要列表 |
| GET | `/chat/history/messages` | JSON | 分页获取指定会话的消息记录 |
| GET | `/chat/history/{conversationId}` | JSON | 删除指定教师 AI 会话及其消息 |
| GET | `/optimizationAssistant` | SSE 结构化流 | 优化课程简介 |
| GET | `/optimization/assistant/crowd` | SSE 结构化流 | 生成适合人群建议 |
| GET | `/optimization/requirements` | SSE 结构化流 | 生成技术要求建议 |
| POST | `/optimization/assistant/questions` | JSON | 智能出题助手 |

### 智能出题助手输入摘要

`POST /optimization/assistant/questions` 使用 JSON 请求体，主要包含以下信息：

| 字段 | 说明 |
| --- | --- |
| `course.title` | 课程标题 |
| `course.category` | 课程分类 |
| `course.outline` | 课程大纲，包含章节与小节 |
| `creatorId` | 教师 ID |
| `type` | 题型，例如单选题 |
| `difficulty` | 难度等级 |

### 示例

```bash
curl -X POST \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token>' \
  http://localhost:8088/api/teacher/ai/optimization/assistant/questions \
  --data '{
    "course": {
      "title": "Java Web 开发",
      "category": "后端开发"
    },
    "creatorId": "teacher-001",
    "type": "SINGLE_CHOICE",
    "difficulty": "EASY"
  }'
```

## 与主业务后端的关系

AI 服务为独立 Spring Boot 应用，但与主业务后端共同组成完整平台：

- 前端直接调用 AI 服务的学生端与教师端接口。
- 管理端通过 AI 治理接口维护配额、审核和运行时策略。
- AI 服务依赖 Redis、MongoDB、RabbitMQ，并结合多模型平台能力提供对话与治理能力。
- 主业务后端承载课程、学习、测评、聊天、通知等主体业务，两份接口文档建议配套阅读。

## 相关公开资料

- [公开部署说明](./SETUP.md)
- [主业务后端 API 文档](./主业务后端%20API%20文档.md)
- [主库结构 SQL](../../sql/MySQL/online_educate.sql)
