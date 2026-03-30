# Online Education 公开部署说明

## 适用范围

本文档仅描述当前公开仓库中已经能够从工程配置直接验证的启动方式、依赖组件与配置项分类，不引用内部设计文档，也不包含任何私有环境参数。

## 服务组成

| 服务 | 路径 | 默认说明 |
| --- | --- | --- |
| 主业务后端 | `OnlineMSBackEnd` | 未显式配置 `server.port`，默认按 Spring Boot 使用 `8080` |
| AI 服务 | `online-ai` | `application.yml` 中显式配置端口为 `8088` |
| 前端 | `online-ms-front-end` | Vite 未显式指定端口，开发模式默认 `5173` |

## 基础依赖

- Java 21
- Maven
- Node.js 与 npm
- MySQL 8+
- Redis
- MongoDB
- RabbitMQ
- MinIO

## 配置文件位置

- `OnlineMSBackEnd/src/main/resources/application.yml`
- `OnlineMSBackEnd/src/main/resources/application-local.yml`
- `online-ai/src/main/resources/application.yml`
- `online-ai/src/main/resources/application-local.yml`

两个 Spring Boot 服务当前都启用了 `local` profile。公开仓库中可以据此了解配置结构，但本地启动前应自行替换成自己的环境参数。

## 配置项分类

### 主业务后端

- 数据源：`SPRING_DATASOURCE_*`
- Redis：`SPRING_DATA_REDIS_*`
- MongoDB：`SPRING_DATA_MONGODB_*`
- RabbitMQ：`SPRING_RABBITMQ_*`
- 邮件：`SPRING_MAIL_*`
- JWT：`SPRING_SECURITY_JWT_KEY`
- MinIO：`APP_FILE_STORAGE_MINIO_*`

### AI 服务

- Redis：`SPRING_DATA_REDIS_*`
- MongoDB：`SPRING_DATA_MONGODB_*`
- RabbitMQ：`SPRING_RABBITMQ_*`
- JWT：`SPRING_SECURITY_JWT_KEY`
- OpenAI：`SPRING_AI_OPENAI_*`
- DashScope：`SPRING_AI_DASHSCOPE_*`
- DeepSeek：`SPRING_AI_DEEPSEEK_*`

## 启动顺序建议

1. 启动 MySQL、Redis、MongoDB、RabbitMQ、MinIO。
2. 启动主业务后端 `OnlineMSBackEnd`。
3. 启动 AI 服务 `online-ai`。
4. 启动前端 `online-ms-front-end`。

## 常用命令

### 构建全部模块

```bash
mvn clean install
```

### 启动主业务后端

```bash
mvn -pl OnlineMSBackEnd spring-boot:run
```

### 启动 AI 服务

```bash
mvn -pl online-ai spring-boot:run
```

### 启动前端

```bash
cd online-ms-front-end
npm install
npm run dev
```

## 说明

- 当前公开仓库中数据库结构文件位于 [sql/MySQL/online_educate.sql](../../sql/MySQL/online_educate.sql)
- 如果后续公开更多可复用资料，建议继续放在 `docs/public` 下，避免 README 依赖内部文档