# 在线教育平台 - AI 服务与治理 API 文档

| Version | Update Time | Status | Author | Description |
| --- | --- | --- | --- | --- |
| v2026-03-30 | 2026-03-30 | curated | @daoxuan233 | 面向公开仓库整理的 AI 服务接口说明 |

> 说明
>
> - 本文档对应模块 `online-ai`
> - 默认示例地址使用 `http://localhost:8088`
> - 内容基于当前仓库中已验证的控制器路由与已有 smart-doc 结果整理
> - 覆盖管理员 AI 治理、学生 AI、教师 AI 助手与教师优化助手接口

## 服务信息

| 项目 | 说明 |
| --- | --- |
| 服务模块 | `online-ai` |
| 默认端口 | `8088` |
| 鉴权方式 | `Authorization: Bearer <token>` |
| 响应形式 | JSON、SSE 流式响应 |
| 主要路由前缀 | `/api/admin/ai/governance`、`/api/students/ai`、`/api/students/ai/tool`、`/api/teacher/ai` |

## 管理端 AI 治理接口

### 接口范围

管理端治理接口用于配置 AI 配额、运行时策略以及人工审核队列，前缀统一为 `/api/admin/ai/governance`。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/overview` | 获取治理总览，包括当日调用统计、待审数量和按平台聚合后的指标 |
| GET | `/records` | 分页查询 AI 调用记录，支持按用户、平台、调用状态、审查状态筛选 |
| GET | `/quota-policies` | 获取当前可见的配额策略列表 |
| POST | `/quota-policies` | 新增或更新单条配额策略 |
| GET | `/runtime-strategy` | 获取当前生效的运行时治理策略 |
| POST | `/runtime-strategy` | 保存内容审查、阻断词、复核词和重试降级顺序 |
| POST | `/reviews/{recordId}` | 对待审 AI 调用记录提交人工审查结论 |

### 常见请求参数

| 接口 | 关键参数 |
| --- | --- |
| `GET /records` | `userId`、`platform`、`page`、`size`，以及调用状态、审查状态等筛选参数 |
| `POST /quota-policies` | `scopeType`、`scopeValue`、`dailyLimit`、`hourlyLimit`、`enabled`、`remark` |
| `POST /runtime-strategy` | `reviewEnabled`、`blockedKeywords`、`reviewKeywords`、`maxAttempts`、`fallbackPlatforms` |
| `POST /reviews/{recordId}` | `decision`、`note` |

### 示例

```bash
curl -X GET -H 'Authorization: Bearer <token>' \
  http://localhost:8088/api/admin/ai/governance/overview
```

## 学生端 AI 接口

### 接口范围

学生端 AI 接口前缀统一为 `/api/students/ai`，主要用于智能问答、会话历史管理和笔记标签解析。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/chat-stream` | 学生 AI 文本流式问答 |
| GET | `/chat-stream/res` | 学生 AI 结构化流式响应，返回 `recordId` 与 `content` |
| GET | `/chat/history/summaries` | 获取当前用户的 AI 会话摘要列表 |
| GET | `/chat/history/messages` | 分页获取指定会话的消息记录 |
| GET | `/chat/history/{conversationId}` | 删除指定 AI 会话及其消息 |
| POST | `/tool/parse-tag` | 解析笔记标签 |

### 调用说明

- `question` 为对话类接口的核心参数
- `conversationId` 可选，用于续接已有会话
- `chat-stream` 与 `chat-stream/res` 为 SSE 流式接口
- `chat/history/messages` 常用分页参数为 `page` 与 `size`

### 示例

```bash
curl -X GET -H 'Authorization: Bearer <token>' \
  'http://localhost:8088/api/students/ai/chat-stream?question=请帮我总结本节重点'
```

## 教师端 AI 接口

### 接口范围

教师端 AI 接口前缀统一为 `/api/teacher/ai`，同时覆盖教师通用对话、历史会话，以及围绕课程设计的优化助手能力。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/chat-stream` | 教师 AI 文本流式问答 |
| GET | `/chat/history/summaries` | 获取教师 AI 会话摘要列表 |
| GET | `/chat/history/messages` | 分页获取指定会话的消息记录 |
| GET | `/chat/history/{conversationId}` | 删除指定教师 AI 会话及其消息 |
| GET | `/optimizationAssistant` | 优化课程简介 |
| GET | `/optimization/assistant/crowd` | 生成适合人群建议 |
| GET | `/optimization/requirements` | 生成技术要求建议 |
| POST | `/optimization/assistant/questions` | 智能出题助手 |

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

## 返回与鉴权说明

- 大部分治理接口与工具接口返回统一 JSON 包装对象
- SSE 接口按流式数据块持续返回内容
- 历史会话相关接口用于会话摘要、消息分页和会话删除
- 调用接口前通常需要在请求头中传递 JWT Token

## 与主业务后端的关系

AI 服务为独立 Spring Boot 应用，但与主业务后端共同组成完整平台：

- 前端直接调用 AI 服务的学生端与教师端接口
- 管理端可以通过 AI 治理接口维护配额、审查和运行时策略
- AI 服务依赖 Redis、MongoDB、RabbitMQ，并结合多模型平台能力提供对话与治理能力

## 相关公开资料

- [公开部署说明](./SETUP.md)
- [主业务后端 API 文档](./主业务后端%20API%20文档.md)
- [主库结构 SQL](../../sql/MySQL/online_educate.sql)
