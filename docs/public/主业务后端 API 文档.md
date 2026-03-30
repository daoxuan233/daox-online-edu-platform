# 在线教育平台 - 主业务后端 API 文档

| Version | Update Time | Status | Author | Description |
|---------|-------------|--------|--------|-------------|
|v2026-03-30 18:04:46|2026-03-30 18:04:46|auto|@daoxuan233|Created by smart-doc|


> 说明
>
> - 本文档对应模块 `OnlineMSBackEnd`
> - 默认示例地址使用 `http://localhost:8080`
> - 当前文件保留 smart-doc 导出的主体结构，主要覆盖管理员、教师、学生、公共接口与通知等主业务能力



## 管理员控制层
### 获取用户列表
**URL:** http://localhost:8080/api/admin/users

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/users
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 758,
  "code": 519,
  "data": {},
  "message": "success"
}
```

### 获取用户详情 - 通过ID获取
**URL:** http://localhost:8080/api/admin/user/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户详情 - 通过ID获取

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|用户ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/user/detail?userId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 412,
  "code": 277,
  "data": {},
  "message": "success"
}
```

### 删除用户/逻辑更改状态
**URL:** http://localhost:8080/api/admin/user/status

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除用户/逻辑更改状态

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|用户ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/user/status --data 'userId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 110,
  "code": 882,
  "data": {},
  "message": "success"
}
```

### 恢复被禁用的用户。
**URL:** http://localhost:8080/api/admin/user/restore

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 恢复被禁用的用户。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|true|用户ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/user/restore --data 'userId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 112,
  "code": 435,
  "data": {},
  "message": "success"
}
```

### 管理员创建用户。
**URL:** http://localhost:8080/api/admin/user/create

**Type:** POST


**Content-Type:** application/json

**Description:** 管理员创建用户。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|identifier|string|false|学号/工号。|-|
|nickname|string|false|昵称/用户名。|-|
|email|string|false|邮箱。|-|
|phone|string|false|手机号。|-|
|role|string|false|用户角色：admin / teacher / student。|-|
|password|string|false|初始密码，仅创建时必填。|-|
|enabled|boolean|false|是否启用，true 为正常，false 为禁用。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/user/create --data '{
  "identifier": "6wkyw8",
  "nickname": "alessandra.parisian",
  "email": "juliette.lakin@gmail.com",
  "phone": "1-515-775-3069",
  "role": "pin5ny",
  "password": "h1j9qz",
  "enabled": true
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 38,
  "code": 49,
  "data": {},
  "message": "success"
}
```

### 管理员更新用户基础信息。
**URL:** http://localhost:8080/api/admin/user/update

**Type:** POST


**Content-Type:** application/json

**Description:** 管理员更新用户基础信息。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|true|      用户ID|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|identifier|string|false|学号/工号。|-|
|nickname|string|false|昵称/用户名。|-|
|email|string|false|邮箱。|-|
|phone|string|false|手机号。|-|
|role|string|false|用户角色：admin / teacher / student。|-|
|password|string|false|初始密码，仅创建时必填。|-|
|enabled|boolean|false|是否启用，true 为正常，false 为禁用。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/user/update?userId=41 --data '{
  "identifier": "iewb5q",
  "nickname": "alessandra.parisian",
  "email": "juliette.lakin@gmail.com",
  "phone": "1-515-775-3069",
  "role": "o3f64w",
  "password": "ptqu5x",
  "enabled": true
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 450,
  "code": 650,
  "data": {},
  "message": "success"
}
```

### 查询统一审计日志列表。
**URL:** http://localhost:8080/api/admin/audit-logs

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 查询统一审计日志列表。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|keyword|string|false|     关键字|-|
|action|string|false|      操作动作编码|-|
|status|string|false|      执行状态|-|
|operatorRole|string|false|操作者角色|-|
|startDate|string|false|   开始日期，格式 yyyy-MM-dd|-|
|endDate|string|false|     结束日期，格式 yyyy-MM-dd|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/audit-logs?keyword=m4r9uy&action=62azuc&status=ra3koc&operatorRole=6e5tav&startDate=2026-03-30&endDate=2026-03-30 --data '&m4r9uy&62azuc&ra3koc&6e5tav&2026-03-30&2026-03-30'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 207,
  "code": 656,
  "data": {},
  "message": "success"
}
```

### 重置用户密码
**URL:** http://localhost:8080/api/admin/user/password

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 重置用户密码

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|用户ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/user/password --data 'userId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 334,
  "code": 440,
  "data": {},
  "message": "success"
}
```

## 管理员首页概览接口。
### 获取管理员首页概览数据。
**URL:** http://localhost:8080/api/admin/dashboard/overview

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取管理员首页概览数据。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|days|int32|true|趋势统计天数，默认 30 天|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/dashboard/overview?days=30 --data '&30'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 294,
  "code": 600,
  "data": {},
  "message": "success"
}
```

## 管理员 - 课程相关接口
### 获取所有课程
**URL:** http://localhost:8080/api/admin/course/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取所有课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/course/list
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 505,
  "code": 371,
  "data": {},
  "message": "success"
}
```

### 删除课程
**URL:** http://localhost:8080/api/admin/course/delete

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/course/delete --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 899,
  "code": 547,
  "data": {},
  "message": "success"
}
```

### 获取课程分类信息 - 通过id查询
**URL:** http://localhost:8080/api/admin/course/categories

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程分类信息 - 通过id查询

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|分类id|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/course/categories?id=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 85,
  "code": 467,
  "data": {},
  "message": "success"
}
```

### 获取课程分类信息 - 树形结构
**URL:** http://localhost:8080/api/admin/course/categories/tree

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程分类信息 - 树形结构

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/tree
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 186,
  "code": 714,
  "data": {},
  "message": "success"
}
```

### 创建分类
**URL:** http://localhost:8080/api/admin/course/categories/create

**Type:** POST


**Content-Type:** application/json

**Description:** 创建分类

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|name|string|false|分类名称。|-|
|parentId|string|false|父分类ID，空值表示顶级分类。|-|
|orderIndex|int32|false|排序权重。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/create --data '{
  "name": "gilberto.rippin",
  "parentId": "41",
  "orderIndex": 1
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 267,
  "code": 254,
  "data": {},
  "message": "success"
}
```

### 更新分类
**URL:** http://localhost:8080/api/admin/course/categories/update

**Type:** POST


**Content-Type:** application/json

**Description:** 更新分类

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|      分类id|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|name|string|false|分类名称。|-|
|parentId|string|false|父分类ID，空值表示顶级分类。|-|
|orderIndex|int32|false|排序权重。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/update?id=41 --data '{
  "name": "gilberto.rippin",
  "parentId": "41",
  "orderIndex": 1
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 555,
  "code": 800,
  "data": {},
  "message": "success"
}
```

### 删除分类。
**URL:** http://localhost:8080/api/admin/course/categories/delete

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除分类。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|分类ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/delete --data 'id=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 290,
  "code": 623,
  "data": {},
  "message": "success"
}
```

### 预览分类删除影响范围。
**URL:** http://localhost:8080/api/admin/course/categories/delete/preview

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 预览分类删除影响范围。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|分类ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/delete/preview?id=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 16,
  "code": 700,
  "data": {},
  "message": "success"
}
```

### 紧急删除分类。
**URL:** http://localhost:8080/api/admin/course/categories/delete/emergency

**Type:** POST


**Content-Type:** application/json

**Description:** 紧急删除分类。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|categoryId|string|true|待删除的分类ID|-|
|reason|string|false|删除原因|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/delete/emergency --data '{
  "categoryId": "41",
  "reason": "rnh906"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 571,
  "code": 716,
  "data": {},
  "message": "success"
}
```

### 提交常规删除申请。
**URL:** http://localhost:8080/api/admin/course/categories/delete/regular

**Type:** POST


**Content-Type:** application/json

**Description:** 提交常规删除申请。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|categoryId|string|true|待删除的分类ID|-|
|reason|string|false|删除原因|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/delete/regular --data '{
  "categoryId": "41",
  "reason": "j782gv"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 120,
  "code": 627,
  "data": {},
  "message": "success"
}
```

### 预览分类迁移影响范围。
**URL:** http://localhost:8080/api/admin/course/categories/migration/preview

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 预览分类迁移影响范围。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|分类ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/migration/preview?id=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 549,
  "code": 268,
  "data": {},
  "message": "success"
}
```

### 执行分类迁移。
**URL:** http://localhost:8080/api/admin/course/categories/migration/execute

**Type:** POST


**Content-Type:** application/json

**Description:** 执行分类迁移。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|sourceCategoryId|string|true|源分类ID|-|
|targetCategoryId|string|true|目标分类ID|-|
|reason|string|false|迁移原因|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/course/categories/migration/execute --data '{
  "sourceCategoryId": "41",
  "targetCategoryId": "41",
  "reason": "6y1i7t"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 76,
  "code": 315,
  "data": {},
  "message": "success"
}
```

## 数据统计分析
### 获取用户统计 - 现存数据库中所有的用户数量
**URL:** http://localhost:8080/api/admin/statistics/users

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户统计 - 现存数据库中所有的用户数量

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 367,
  "code": 342,
  "data": {},
  "message": "success"
}
```

### 获取用户统计 - 有效数据
**URL:** http://localhost:8080/api/admin/statistics/users/valid

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户统计 - 有效数据

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/valid
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 764,
  "code": 279,
  "data": {},
  "message": "success"
}
```

### 获取管理员统计数量
**URL:** http://localhost:8080/api/admin/statistics/users/admin

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取管理员统计数量

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/admin
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 676,
  "code": 930,
  "data": {},
  "message": "success"
}
```

### 获取管理员统计数量 - 获取有效数据
**URL:** http://localhost:8080/api/admin/statistics/users/admin/valid

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取管理员统计数量 - 获取有效数据

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/admin/valid
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 874,
  "code": 732,
  "data": {},
  "message": "success"
}
```

### 获取教师统计数量
**URL:** http://localhost:8080/api/admin/statistics/users/teacher

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取教师统计数量

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/teacher
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 678,
  "code": 281,
  "data": {},
  "message": "success"
}
```

### 获取教师统计数量 - 获取有效数据
**URL:** http://localhost:8080/api/admin/statistics/users/teacher/valid

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取教师统计数量 - 获取有效数据

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/teacher/valid
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 611,
  "code": 937,
  "data": {},
  "message": "success"
}
```

### 获取学生统计数量
**URL:** http://localhost:8080/api/admin/statistics/users/student

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学生统计数量

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/student
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 323,
  "code": 125,
  "data": {},
  "message": "success"
}
```

### 获取学生统计数量 - 获取有效数据
**URL:** http://localhost:8080/api/admin/statistics/users/student/valid

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学生统计数量 - 获取有效数据

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/users/student/valid
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 930,
  "code": 332,
  "data": {},
  "message": "success"
}
```

### 获取课程统计 - 现存课程
**URL:** http://localhost:8080/api/admin/statistics/courses

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程统计 - 现存课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/courses
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 607,
  "code": 773,
  "data": {},
  "message": "success"
}
```

### 获取详细课程统计
**URL:** http://localhost:8080/api/admin/statistics/courses/detailed

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取详细课程统计

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/statistics/courses/detailed
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 615,
  "code": 872,
  "data": {},
  "message": "success"
}
```

## 全量同步控制器
### 触发全量数据同步到 AI 向量库<br>建议加权限控制，防止误触
**URL:** http://localhost:8080/api/admin/sync/start-all

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 触发全量数据同步到 AI 向量库
建议加权限控制，防止误触

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/sync/start-all
```

**Response-example:**
```
string
```

## 系统公告管理
### 获取公告列表
**URL:** http://localhost:8080/api/admin/sys/notice/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取公告列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|pageNum|int32|false| 页码|-|
|pageSize|int32|false|每页数量|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/admin/sys/notice/list?pageNum=724&pageSize=10
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 960,
  "code": 352,
  "data": {},
  "message": "success"
}
```

### 发布系统公告
**URL:** http://localhost:8080/api/admin/sys/notice/publish

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 发布系统公告

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|title|string|false|    标题|-|
|content|string|false|  内容|-|
|isActive|string|false| 是否激活 [0-激活 , 1-未激活]|-|
|cratedAt|string|false| 创建时间|-|
|expiredAt|string|false|失效时间|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/sys/notice/publish --data 'title=xuxu89&content=zx7j7y&isActive=j0xm2p&cratedAt=2026-03-30 18:04:47&expiredAt=2026-03-30 18:04:47'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 813,
  "code": 220,
  "data": {},
  "message": "success"
}
```

### 更新公告
**URL:** http://localhost:8080/api/admin/sys/update

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 更新公告

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|       公告id|-|
|title|string|false|    标题|-|
|content|string|false|  内容|-|
|isActive|int32|false| 状态|-|
|expiredAt|string|false|过期时间|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/admin/sys/update --data 'id=41&title=wvrx7x&content=89957e&isActive=709&expiredAt=2026-03-30 18:04:47'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 138,
  "code": 401,
  "data": {},
  "message": "success"
}
```

### 删除公告 - 本质是过期公告
**URL:** http://localhost:8080/api/admin/sys/deleteSystemAnnouncement

**Type:** POST


**Content-Type:** application/json

**Description:** 删除公告 - 本质是过期公告

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|false|公告id|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/admin/sys/deleteSystemAnnouncement --data '41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 663,
  "code": 561,
  "data": {},
  "message": "success"
}
```

## 专用用于处理错误页面的Controller
### 所有错误在这里统一处理，自动解析状态码和原因
**URL:** http://localhost:8080//error

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 所有错误在这里统一处理，自动解析状态码和原因

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/error
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 830,
  "code": 814,
  "data": {},
  "message": "success"
}
```

### 处理业务异常
**URL:** http://localhost:8080//errorbusiness

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 处理业务异常

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/errorbusiness
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 851,
  "code": 301,
  "data": {},
  "message": "success"
}
```

## 统一消息通知中心控制器。
&lt;p&gt;
对外提供当前登录用户的通知列表查询、未读数统计、单条已读和全部已读能力。
控制器本身不区分学生端或教师端，统一通过当前请求中的用户身份完成数据隔离。
&lt;/p&gt;
### 分页查询当前用户的通知列表。
**URL:** http://localhost:8080/api/notifications

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 分页查询当前用户的通知列表。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|pageNum|int32|false|         页码，从 1 开始|-|
|pageSize|int32|false|        每页条数|-|
|unreadOnly|boolean|false|      是否只看未读通知|-|
|notificationType|string|false|通知类型过滤条件|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/notifications/?pageNum=224&pageSize=10&unreadOnly=true&notificationType=dtmeh0 --data '&224&10&true&dtmeh0'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 328,
  "code": 591,
  "data": {},
  "message": "success"
}
```

### 获取当前用户的未读通知总数。
**URL:** http://localhost:8080/api/notifications/unread-count

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前用户的未读通知总数。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/notifications/unread-count
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 1,
  "code": 387,
  "data": {},
  "message": "success"
}
```

### 将指定通知标记为已读。
**URL:** http://localhost:8080/api/notifications/{notificationId}/read

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 将指定通知标记为已读。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|notificationId|string|true|通知 ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/notifications/41/read
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 973,
  "code": 623,
  "data": {},
  "message": "success"
}
```

### 将当前用户的全部未读通知标记为已读。
**URL:** http://localhost:8080/api/notifications/read-all

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 将当前用户的全部未读通知标记为已读。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/notifications/read-all
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 392,
  "code": 561,
  "data": {},
  "message": "success"
}
```

## 公共API控制器
提供文件上传和下载等公共接口
### 文件上传接口<br>支持多种文件类型，自动根据文件类型创建对应文件夹<br>文件命名格式：原文件名_上传者ID_唯一标识.扩展名
**URL:** http://localhost:8080/api/public/upload

**Type:** POST


**Content-Type:** multipart/form-data

**Description:** 文件上传接口
支持多种文件类型，自动根据文件类型创建对应文件夹
文件命名格式：原文件名_上传者ID_唯一标识.扩展名

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|file|file|true|   上传的文件|-|
|userId|string|true| 上传者ID|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization' -F 'file=' -i http://localhost:8080/api/public/upload --data 'userId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 954,
  "code": 20,
  "data": {},
  "message": "success"
}
```

### 文件下载接口<br>生成临时下载链接
**URL:** http://localhost:8080/api/public/download

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 文件下载接口
生成临时下载链接

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|objectName|string|true|文件在存储系统中的对象名称|-|
|duration|int32|true|  链接有效期（秒），默认1小时|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/download?objectName=gilberto.rippin&duration=3600 --data '&gilberto.rippin&3600'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 118,
  "code": 74,
  "data": {},
  "message": "success"
}
```

### 永久文件访问接口<br>通过应用服务器代理访问MinIO中的文件
**URL:** http://localhost:8080/api/public/file/**

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 永久文件访问接口
通过应用服务器代理访问MinIO中的文件

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/file/**
```

**Response-example:**
```
Return void.
```

### 分页查询公开课程列表
**URL:** http://localhost:8080/api/public/courses

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 分页查询公开课程列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|pageSize|int32|true|页大小，默认为10|-|
|offset|int32|true|  偏移量，默认为0|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/courses?pageSize=10&offset=0 --data '&10&0'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 18,
  "code": 320,
  "data": {},
  "message": "success"
}
```

### 获取课程基本信息
**URL:** http://localhost:8080/api/public/courses/{courseId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程基本信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/courses/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 897,
  "code": 318,
  "data": {},
  "message": "success"
}
```

### 获取课程分类信息 - 树形结构
**URL:** http://localhost:8080/api/public/categories

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程分类信息 - 树形结构

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/categories
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 355,
  "code": 732,
  "data": {},
  "message": "success"
}
```

### 获取课程分类信息 - 树形结构 - 用于前端组件
**URL:** http://localhost:8080/api/public/categories/tree

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程分类信息 - 树形结构 - 用于前端组件

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/categories/tree
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 532,
  "code": 27,
  "data": {},
  "message": "success"
}
```

### 获取系统公告
**URL:** http://localhost:8080/api/public/announcements

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取系统公告

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|pageNum|int32|true| 页码 默认1|-|
|pageSize|int32|true|每页数量 默认10|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/announcements?pageNum=1&pageSize=10 --data '&1&10'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 425,
  "code": 564,
  "data": {},
  "message": "success"
}
```

### 上传头像
**URL:** http://localhost:8080/api/public/upload/avatar

**Type:** POST


**Content-Type:** multipart/form-data

**Description:** 上传头像

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|file|file|true|   头像文件|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization' -F 'file=' -i http://localhost:8080/api/public/upload/avatar
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 532,
  "code": 182,
  "data": {},
  "message": "success"
}
```

### 请求邮箱验证码
**URL:** http://localhost:8080/api/public/auth/ask-code

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 请求邮箱验证码

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|email|string|true|  请求邮箱|-|
|type|string|true|   验证码类型 (使用 @Pattern 限制为 "register" 、 "reset"、""activate)。<br/>Validate[regexp: (register|reset|activate); ]|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/auth/ask-code?email=juliette.lakin@gmail.com&type=98vhl7 --data '&juliette.lakin@gmail.com&98vhl7'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 317,
  "code": 935,
  "data": {},
  "message": "success"
}
```

### 用户注册操作，需要先请求邮件验证码
**URL:** http://localhost:8080/api/public/auth/register

**Type:** POST


**Content-Type:** application/json

**Description:** 用户注册操作，需要先请求邮件验证码

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|email|string|false|邮箱|-|
|identifier|string|false|学号/工号|-|
|code|string|false|验证码 (6位数字)<br/>Validate[max: 6; ]|-|
|username|string|false|用户名 (1-10个字符)<br/>Validate[regexp: ^[a-zA-Z0-9\\u4e00-\\u9fa5]+$; max: 10; ]|-|
|password|string|false|密码 (6-20个字符)<br/>Validate[max: 25; ]|-|
|avatarUrl|string|false|头像链接<br/>可选字段，可为null|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/public/auth/register --data '{
  "email": "juliette.lakin@gmail.com",
  "identifier": "0q1lto",
  "code": "76108",
  "username": "gilberto.rippin",
  "password": "nwcp53",
  "avatarUrl": "www.dustin-schulist.biz"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 689,
  "code": 570,
  "data": {},
  "message": "success"
}
```

### 执行密码重置确认，检查验证码是否正确
**URL:** http://localhost:8080/api/public/reset-confirm

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 执行密码重置确认，检查验证码是否正确

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|email|string|false|No comments found.|-|
|code|string|false|No comments found.<br/>Validate[max: 6; ]|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/public/reset-confirm --data 'email=juliette.lakin@gmail.com&code=76108&vo='
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 148,
  "code": 124,
  "data": {},
  "message": "success"
}
```

### 重置密码，需要先执行密码重置确认
**URL:** http://localhost:8080/api/public/reset-password

**Type:** POST


**Content-Type:** application/json

**Description:** 重置密码，需要先执行密码重置确认

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|email|string|false|No comments found.|-|
|code|string|false|No comments found.<br/>Validate[max: 6; ]|-|
|password|string|false|No comments found.<br/>Validate[max: 25; ]|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/public/reset-password --data '{
  "email": "juliette.lakin@gmail.com",
  "code": "76108",
  "password": "arr5gm"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 84,
  "code": 537,
  "data": {},
  "message": "success"
}
```

### 修改密码
**URL:** http://localhost:8080/api/public/update-password

**Type:** POST


**Content-Type:** application/json

**Description:** 修改密码

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|oldPassword|string|false|原密码<br/>Validate[max: 25; ]|-|
|newPassword|string|false|新密码<br/>Validate[max: 25; ]|-|
|confirmPassword|string|false|确认新密码<br/>Validate[max: 25; ]|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/public/update-password --data '{
  "oldPassword": "1cabnb",
  "newPassword": "54ypsi",
  "confirmPassword": "jxgeac"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 496,
  "code": 586,
  "data": {},
  "message": "success"
}
```

### 获取课程评价统计信息
**URL:** http://localhost:8080/api/public/courses/ratings/statistics

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程评价统计信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/courses/ratings/statistics?courseId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 29,
  "code": 273,
  "data": {},
  "message": "success"
}
```

### 获取讲师评分统计
**URL:** http://localhost:8080/api/public/teachers/ratings/statistics

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取讲师评分统计

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|teacherId|string|true|讲师ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/teachers/ratings/statistics?teacherId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 907,
  "code": 318,
  "data": {},
  "message": "success"
}
```

### 获取热门课程排行
**URL:** http://localhost:8080/api/public/courses/top-rated

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取热门课程排行

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|limit|int32|true|     返回数量限制，默认20，最大100|-|
|minRatings|int32|true|最少评分数量要求，默认10|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/courses/top-rated?limit=20&minRatings=10 --data '&20&10'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 727,
  "code": 445,
  "data": {},
  "message": "success"
}
```

### 检查用户评分权限
**URL:** http://localhost:8080/api/public/courses/ratings/permission

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 检查用户评分权限

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/public/courses/ratings/permission?courseId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 143,
  "code": 767,
  "data": {},
  "message": "success"
}
```

## 学生端 - 考核信息接口
### 获取我的测评列表
**URL:** http://localhost:8080/api/student/assessments/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取我的测评列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|status|string|false| 测评状态过滤条件（可选）|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/assessments/list?status=61bwa9 --data '&61bwa9'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 987,
  "code": 311,
  "data": {},
  "message": "success"
}
```

### 获取单个测评详情
**URL:** http://localhost:8080/api/student/assessments/{assessmentId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取单个测评详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/assessments/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 684,
  "code": 935,
  "data": {},
  "message": "success"
}
```

### 检查是否可以开始测评
**URL:** http://localhost:8080/api/student/assessments/can-start/{assessmentId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 检查是否可以开始测评

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/assessments/can-start/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 622,
  "code": 504,
  "data": {},
  "message": "success"
}
```

### 开始考试，创建测评会话
**URL:** http://localhost:8080/api/student/assessments/start

**Type:** POST


**Content-Type:** application/json

**Description:** 开始考试，创建测评会话

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/assessments/start --data '{
  "assessmentId": "41"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 659,
  "code": 12,
  "data": {},
  "message": "success"
}
```

### 获取试卷内容接口
**URL:** http://localhost:8080/api/student/assessments/{assessmentId}/paper

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取试卷内容接口

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评 ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/assessments/41/paper
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 114,
  "code": 938,
  "data": {},
  "message": "success"
}
```

### 实时保存学生答案
**URL:** http://localhost:8080/api/student/assessments/{assessmentId}/answer

**Type:** POST


**Content-Type:** application/json

**Description:** 实时保存学生答案

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID (从路径获取)|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|questionId|string|false|No comments found.|-|
|response|object|false|No comments found.|-|
|└─any object|object|false|any object.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/assessments/41/answer --data '{
  "questionId": "41",
  "response": {}
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 342,
  "code": 476,
  "data": {},
  "message": "success"
}
```

### 提交试卷
**URL:** http://localhost:8080/api/student/assessments/{assessmentId}/submit

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 提交试卷

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/assessments/41/submit
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 405,
  "code": 386,
  "data": {},
  "message": "success"
}
```

## 学生端 - 讨论区控制器
### 获取课程讨论列表
**URL:** http://localhost:8080/api/student/discussions/course/{courseId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程讨论列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/course/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 988,
  "code": 632,
  "data": {},
  "message": "success"
}
```

### 获取课程置顶讨论
**URL:** http://localhost:8080/api/student/discussions/course/{courseId}/pinned

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程置顶讨论

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/course/41/pinned
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 240,
  "code": 3,
  "data": {},
  "message": "success"
}
```

### 获取课程提问帖
**URL:** http://localhost:8080/api/student/discussions/course/{courseId}/questions

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程提问帖

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/course/41/questions
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 167,
  "code": 364,
  "data": {},
  "message": "success"
}
```

### 获取未解决的提问帖
**URL:** http://localhost:8080/api/student/discussions/course/{courseId}/questions/unresolved

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取未解决的提问帖

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/course/41/questions/unresolved
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 837,
  "code": 486,
  "data": {},
  "message": "success"
}
```

### 发布讨论
**URL:** http://localhost:8080/api/student/discussions

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 发布讨论

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|  课程ID|-|
|title|string|true|     讨论标题|-|
|content|string|true|   讨论内容|-|
|isQuestion|boolean|true|是否为提问帖|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/discussions/ --data 'courseId=41&title=ooinr2&content=3zmv6o&isQuestion=false'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 9,
  "code": 246,
  "data": {},
  "message": "success"
}
```

### 获取讨论详情
**URL:** http://localhost:8080/api/student/discussions/{id}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取讨论详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|     讨论ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 459,
  "code": 249,
  "data": {},
  "message": "success"
}
```

### 获取讨论回复
**URL:** http://localhost:8080/api/student/discussions/{id}/replies

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取讨论回复

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|     讨论ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/discussions/41/replies
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 642,
  "code": 427,
  "data": {},
  "message": "success"
}
```

### 发布回复
**URL:** http://localhost:8080/api/student/discussions/{id}/replies

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 发布回复

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|     讨论ID|-|

**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|content|string|true|回复内容|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/discussions/41/replies --data 'content=vto6pg'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 660,
  "code": 952,
  "data": {},
  "message": "success"
}
```

### 点赞回复
**URL:** http://localhost:8080/api/student/discussions/replies/{id}/like

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 点赞回复

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|     回复ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/discussions/replies/41/like
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 40,
  "code": 294,
  "data": {},
  "message": "success"
}
```

## 学生端 -  学习记录相关接口
### 获取视频播放地址
**URL:** http://localhost:8080/api/student/learning/video

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取视频播放地址

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|sectionId|string|true|小节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/video?sectionId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 183,
  "code": 715,
  "data": {},
  "message": "success"
}
```

### 学生端更新视频学习进度接口<br>前端应定期或在关键事件（暂停、关闭）时调用此接口。
**URL:** http://localhost:8080/api/student/learning/update

**Type:** POST


**Content-Type:** application/json

**Description:** 学生端更新视频学习进度接口
前端应定期或在关键事件（暂停、关闭）时调用此接口。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|sectionId|string|true|正在观看的小节的ID。<br/>这是一个必填项，用于定位要更新哪一条学习记录。<br/>- @NotBlank: 确保小节ID不为null，且不为空字符串或纯空格。|-|
|progressSeconds|int32|true|当前的观看进度，单位为秒。<br/>这是一个必填项。<br/>- @NotNull: 确保前端必须传递此参数。<br/>- @PositiveOrZero: 确保观看时长不能为负数，可以是0。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/learning/update --data '{
  "sectionId": "41",
  "progressSeconds": 297
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 327,
  "code": 998,
  "data": {},
  "message": "success"
}
```

### 学生端统一文件访问接口（触发学习进度记录）。<br>支持从URL参数中获取token进行认证。
**URL:** http://localhost:8080/api/student/learning/files/{fileId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 学生端统一文件访问接口（触发学习进度记录）。
支持从URL参数中获取token进行认证。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|fileId|string|true|No comments found.|-|

**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|token|string|false|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/files/41?token=5bm8pu --data '&5bm8pu'
```

**Response-example:**
```
Return void.
```

### 学生端获取课程内容大纲接口<br>一次性返回指定课程的所有章节、小节、关联资料，以及当前学生的个性化学习进度。
**URL:** http://localhost:8080/api/student/learning/{courseId}/content

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 学生端获取课程内容大纲接口
一次性返回指定课程的所有章节、小节、关联资料，以及当前学生的个性化学习进度。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/41/content
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 281,
  "code": 940,
  "data": {},
  "message": "success"
}
```

### 获取课程资料 - 所有课程资料
**URL:** http://localhost:8080/api/student/learning/materials/all

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程资料 - 所有课程资料

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|  用户ID|-|
|courseId|string|false|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/materials/all?userId=41&courseId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 301,
  "code": 548,
  "data": {},
  "message": "success"
}
```

### 获取课程资料 - 章节
**URL:** http://localhost:8080/api/student/learning/materials/chapters

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程资料 - 章节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/materials/chapters?userId=41&courseId=41&chapterId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 466,
  "code": 275,
  "data": {},
  "message": "success"
}
```

### 获取课程资料 - 小节资料
**URL:** http://localhost:8080/api/student/learning/materials/sections

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程资料 - 小节资料

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID|-|
|sectionId|string|false|小节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/materials/sections?userId=41&courseId=41&chapterId=41&sectionId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 557,
  "code": 542,
  "data": {},
  "message": "success"
}
```

### 获取学习笔记 - 所有
**URL:** http://localhost:8080/api/student/learning/notes

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学习笔记 - 所有

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 4,
  "code": 578,
  "data": {},
  "message": "success"
}
```

### 获取一个学生的所有课程笔记
**URL:** http://localhost:8080/api/student/learning/notes/course

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取一个学生的所有课程笔记

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|page|int32|false|No comments found.|-|
|size|int32|false|No comments found.|-|
|sort|string|false|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/course?page=1&size=10&sort=kge4t7
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 462,
  "code": 949,
  "data": {},
  "message": "success"
}
```

### 获取学习笔记 - 收件箱
**URL:** http://localhost:8080/api/student/learning/notes/inbox

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学习笔记 - 收件箱

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|page|int32|false|No comments found.|-|
|size|int32|false|No comments found.|-|
|sort|string|false|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/inbox?page=1&size=10&sort=npb1x1
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 223,
  "code": 546,
  "data": {},
  "message": "success"
}
```

### 归档收件箱学习笔记
**URL:** http://localhost:8080/api/student/learning/notes/{id}/archive

**Type:** PUT


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 归档收件箱学习笔记

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|   笔记ID|-|

**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false| 课程ID [非必须]|-|
|chapterId|string|false|章节ID [非必须]|-|
|sectionId|string|false|小节ID [非必须]|-|

**Request-example:**
```
curl -X PUT -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/41/archive --data 'courseId=41&chapterId=41&sectionId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 869,
  "code": 651,
  "data": {},
  "message": "success"
}
```

### 获取学习笔记 - 指定课程
**URL:** http://localhost:8080/api/student/learning/notes/course/specify

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学习笔记 - 指定课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|  用户ID|-|
|courseId|string|false|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/course/specify?userId=41&courseId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 506,
  "code": 800,
  "data": {},
  "message": "success"
}
```

### 获取学习笔记 - 章节
**URL:** http://localhost:8080/api/student/learning/notes/course/chapter

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学习笔记 - 章节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/course/chapter?userId=41&courseId=41&chapterId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 398,
  "code": 306,
  "data": {},
  "message": "success"
}
```

### 获取学习笔记 - 小节
**URL:** http://localhost:8080/api/student/learning/notes/course/sections

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取学习笔记 - 小节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID|-|
|sectionId|string|false|小节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/notes/course/sections?userId=41&courseId=41&chapterId=41&sectionId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 789,
  "code": 284,
  "data": {},
  "message": "success"
}
```

### 创建学习笔记 - 课程
**URL:** http://localhost:8080/api/student/learning/create/note

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 创建学习笔记 - 课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID [非必选]|-|
|sectionId|string|false|小节ID [非必选]|-|
|title|string|false|    标题|-|
|content|string|false|  内容|-|
|tags|array|false|     标签,[array of string]|-|
|videoTime|int32|false|视频播放时间 [非必选]|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/learning/create/note --data 'userId=41&courseId=41&chapterId=41&sectionId=41&title=vur8gn&content=78gx37&tags=s6h1h3&tags=s6h1h3&videoTime=350'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 859,
  "code": 325,
  "data": {},
  "message": "success"
}
```

### 创建学习笔记 - 自由
**URL:** http://localhost:8080/api/student/learning/note/free

**Type:** POST


**Content-Type:** application/json

**Description:** 创建学习笔记 - 自由

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false|No comments found.|-|
|chapterId|string|false|No comments found.|-|
|sectionId|string|false|No comments found.|-|
|title|string|false|标题|-|
|content|string|false|内容|-|
|sourceType|string|false|笔记来源类型，"AI_CHAT", "EMAIL", "MANUAL" = 除ai产生外的内部数据|-|
|sourceRefId|string|false|来源引用ID，根据source_type不同，存储不同的引用ID<br/>例如：AI_CHAT 可能是对话ID，EMAIL 可能是邮件ID，MANUAL = 除ai外的内部数据为空(NULL)|-|
|tags|array|false|No comments found.|-|
|isPrivate|boolean|false|No comments found.|-|
|videoTime|int32|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/learning/note/free --data '{
  "courseId": "41",
  "chapterId": "41",
  "sectionId": "41",
  "title": "288vmj",
  "content": "ech81g",
  "sourceType": "2ydca8",
  "sourceRefId": "41",
  "tags": [
    "heso4v"
  ],
  "isPrivate": true,
  "videoTime": 249
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 222,
  "code": 738,
  "data": {},
  "message": "success"
}
```

### 创建学习笔记 - 收件箱
**URL:** http://localhost:8080/api/student/learning/note/inbox

**Type:** POST


**Content-Type:** application/json

**Description:** 创建学习笔记 - 收件箱

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false|No comments found.|-|
|chapterId|string|false|No comments found.|-|
|sectionId|string|false|No comments found.|-|
|title|string|false|标题|-|
|content|string|false|内容|-|
|sourceType|string|false|笔记来源类型，"AI_CHAT", "EMAIL", "MANUAL" = 除ai产生外的内部数据|-|
|sourceRefId|string|false|来源引用ID，根据source_type不同，存储不同的引用ID<br/>例如：AI_CHAT 可能是对话ID，EMAIL 可能是邮件ID，MANUAL = 除ai外的内部数据为空(NULL)|-|
|tags|array|false|No comments found.|-|
|isPrivate|boolean|false|No comments found.|-|
|videoTime|int32|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/learning/note/inbox --data '{
  "courseId": "41",
  "chapterId": "41",
  "sectionId": "41",
  "title": "d142ad",
  "content": "06xb8q",
  "sourceType": "m5s1b6",
  "sourceRefId": "41",
  "tags": [
    "5bu64b"
  ],
  "isPrivate": true,
  "videoTime": 718
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 898,
  "code": 318,
  "data": {},
  "message": "success"
}
```

### 更新学习笔记 - 覆盖式
**URL:** http://localhost:8080/api/student/learning/note/{id}

**Type:** POST


**Content-Type:** application/json

**Description:** 更新学习笔记 - 覆盖式

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|true|No comments found.|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|title|string|false|No comments found.|-|
|content|string|false|No comments found.|-|
|tags|array|false|No comments found.|-|
|isPrivate|boolean|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/learning/note/41 --data '{
  "title": "4o8blc",
  "content": "sddfri",
  "tags": [
    "epo0sa"
  ],
  "isPrivate": true
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 166,
  "code": 457,
  "data": {},
  "message": "success"
}
```

### 更新学习笔记
**URL:** http://localhost:8080/api/student/learning/update/note

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 更新学习笔记

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|userId|string|false|   用户ID|-|
|courseId|string|false| 课程ID|-|
|chapterId|string|false|章节ID|-|
|sectionId|string|false|小节ID|-|
|title|string|false|    标题|-|
|content|string|false|  内容|-|
|tags|array|false|     标签,[array of string]|-|
|videoTime|int32|false|视频播放时间|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/learning/update/note --data 'userId=41&courseId=41&chapterId=41&sectionId=41&title=bg1w3w&content=3255h2&tags=b6skej&tags=b6skej&videoTime=138'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 268,
  "code": 618,
  "data": {},
  "message": "success"
}
```

### 获取指定用户的总学习时长
**URL:** http://localhost:8080/api/student/learning/total/time

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取指定用户的总学习时长

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/total/time
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 460,
  "code": 710,
  "data": {},
  "message": "success"
}
```

### 获取用户总体学习进度
**URL:** http://localhost:8080/api/student/learning/overall/progress

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户总体学习进度

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/learning/overall/progress
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 596,
  "code": 362,
  "data": {},
  "message": "success"
}
```

## 学生端 - 课程相关接口
### 获取我的课程列表
**URL:** http://localhost:8080/api/student/course/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取我的课程列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/list
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 156,
  "code": 349,
  "data": {},
  "message": "success"
}
```

### 课程搜索 - 关键词
**URL:** http://localhost:8080/api/student/course/search/keyword

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 课程搜索 - 关键词

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|keyword|string|true|关键词|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/search/keyword?keyword=iz1qnr --data '&iz1qnr'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 540,
  "code": 726,
  "data": {},
  "message": "success"
}
```

### 课程搜索 - 分类
**URL:** http://localhost:8080/api/student/course/search/category

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 课程搜索 - 分类

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|categoryId|string|true|分类ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/search/category?categoryId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 863,
  "code": 158,
  "data": {},
  "message": "success"
}
```

### 搜索课程 - 等级
**URL:** http://localhost:8080/api/student/course/search/level

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 搜索课程 - 等级

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|level|string|true|等级|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/search/level?level=rgosjc --data '&rgosjc'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 743,
  "code": 573,
  "data": {},
  "message": "success"
}
```

### 获取课程详细信息
**URL:** http://localhost:8080/api/student/course/courses/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程详细信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/courses/detail?courseId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 465,
  "code": 344,
  "data": {},
  "message": "success"
}
```

### 加入课程
**URL:** http://localhost:8080/api/student/course/enroll

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 加入课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/course/enroll --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 68,
  "code": 470,
  "data": {},
  "message": "success"
}
```

### 退出课程
**URL:** http://localhost:8080/api/student/course/unenroll

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 退出课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/course/unenroll --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 862,
  "code": 71,
  "data": {},
  "message": "success"
}
```

### 获取课程章节 - 仅列表
**URL:** http://localhost:8080/api/student/course/getCourseChapters

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程章节 - 仅列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/getCourseChapters?courseId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 864,
  "code": 988,
  "data": {},
  "message": "success"
}
```

### 获取已完成课程数量
**URL:** http://localhost:8080/api/student/course/completed-courses-count

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取已完成课程数量

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/course/completed-courses-count
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 546,
  "code": 332,
  "data": {},
  "message": "success"
}
```

## 学生相关接口
### 获取个人信息
**URL:** http://localhost:8080/api/student/profile

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取个人信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|identifier|string|true|学号/工号|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/profile?identifier=u7g30d --data '&u7g30d'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 549,
  "code": 607,
  "data": {},
  "message": "success"
}
```

### 修改头像 - 调用public.api --&gt; 上传url地址
**URL:** http://localhost:8080/api/student/avatar

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 修改头像 - 调用public.api --> 上传url地址

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|avatarUrl|string|true|头像url|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/avatar --data 'avatarUrl=www.dustin-schulist.biz'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 497,
  "code": 444,
  "data": {},
  "message": "success"
}
```

### 更新个人资料
**URL:** http://localhost:8080/api/student/update

**Type:** POST


**Content-Type:** application/json

**Description:** 更新个人资料

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|false|用户ID|-|
|identifier|string|false|学号/工号|-|
|nickname|string|false|昵称/用户名|-|
|email|string|false|邮箱|-|
|role|string|false|'student' 'teacher' 'admin'|-|
|avatarUrl|string|false|头像URL|-|
|createdAt|string|false|创建时间|-|
|phone|string|false|手机号|-|
|gender|string|false|性别<br/>男 [man]、女 [female]、其他 [other]|-|
|birthday|string|false|生日|-|
|biography|string|false|简介 / 签名|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/student/update --data '{
  "id": "41",
  "identifier": "hm3mui",
  "nickname": "alessandra.parisian",
  "email": "juliette.lakin@gmail.com",
  "role": "k923yw",
  "avatarUrl": "www.dustin-schulist.biz",
  "createdAt": "2026-03-30 18:04:48",
  "phone": "1-515-775-3069",
  "gender": "x74eb9",
  "birthday": "2026-03-30 18:04:48",
  "biography": "8c0lq3"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 107,
  "code": 229,
  "data": {},
  "message": "success"
}
```

## 单聊控制器
提供单聊相关的API接口
### 查看好友列表
**URL:** http://localhost:8080/api/student/chat/friend

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 查看好友列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/friend
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 717,
  "code": 334,
  "data": {},
  "message": "success"
}
```

### 获取用户的聊天会话列表
**URL:** http://localhost:8080/api/student/chat/history

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户的聊天会话列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/history
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 238,
  "code": 580,
  "data": {},
  "message": "success"
}
```

### 获取和指定对象的聊天记录详情
**URL:** http://localhost:8080/api/student/chat/history/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取和指定对象的聊天记录详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|friendId|string|false|对象的ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/history/detail?friendId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 729,
  "code": 577,
  "data": {},
  "message": "success"
}
```

### 将与指定好友的聊天消息标记为已读
**URL:** http://localhost:8080/api/student/chat/read

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 将与指定好友的聊天消息标记为已读

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|friendId|string|true|好友ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/chat/read --data 'friendId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 640,
  "code": 219,
  "data": {},
  "message": "success"
}
```

### 获取当前用户所有未读消息的总数
**URL:** http://localhost:8080/api/student/chat/unread-count

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前用户所有未读消息的总数

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/unread-count
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 385,
  "code": 271,
  "data": {},
  "message": "success"
}
```

### 查询好友
**URL:** http://localhost:8080/api/student/chat/friend

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 查询好友

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|friend_identifier|string|true|好友标识符（学号/工号）|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/chat/friend --data 'friend_identifier=85vnb9'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 274,
  "code": 147,
  "data": {},
  "message": "success"
}
```

### 添加好友
**URL:** http://localhost:8080/api/student/chat/friend/add

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 添加好友

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|targetUserId|string|true|好友id|-|
|remark|string|false|      备注|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/chat/friend/add --data 'targetUserId=41&remark=myvdp9'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 475,
  "code": 330,
  "data": {},
  "message": "success"
}
```

### 确认好友申请
**URL:** http://localhost:8080/api/student/chat/friend/confirm

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 确认好友申请

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|targetUserId|string|true|好友id|-|
|remark|string|false|      备注|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/student/chat/friend/confirm --data 'targetUserId=41&remark=6qkl4m'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 695,
  "code": 782,
  "data": {},
  "message": "success"
}
```

### 获取待确认好友申请列表
**URL:** http://localhost:8080/api/student/chat/friend/pending

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取待确认好友申请列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/friend/pending
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 407,
  "code": 720,
  "data": {},
  "message": "success"
}
```

### 统计用户待处理好友申请数
**URL:** http://localhost:8080/api/student/chat/friend/pending/count

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 统计用户待处理好友申请数

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/student/chat/friend/pending/count
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 137,
  "code": 869,
  "data": {},
  "message": "success"
}
```

## 教师端 - 测评相关接口
### 获取所有测评
**URL:** http://localhost:8080/api/teacher/assessments

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取所有测评

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 841,
  "code": 309,
  "data": {},
  "message": "success"
}
```

### 获取测评列表
**URL:** http://localhost:8080/api/teacher/assessments/{courseID}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取测评列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseID|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 311,
  "code": 327,
  "data": {},
  "message": "success"
}
```

### 创建测评
**URL:** http://localhost:8080/api/teacher/assessments/create

**Type:** POST


**Content-Type:** application/json

**Description:** 创建测评

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false|课程ID (必填)<br/>指明此测评属于哪一门课程。|-|
|creatorId|string|false|创建者ID (必填)<br/>指明此测评创建者。|-|
|assessmentType|string|false|测评类型 (必填)<br/>ClassroomExam、ChapterExam、MidtermExam、FinalExam、homework|-|
|title|string|false|测评标题 (必填)<br/>例如：“第一章 数据库基础 课后作业”。|-|
|startTime|string|false|测评开始时间 (必填)|-|
|endTime|string|false|测评结束时间 (必填)|-|
|durationMinutes|int32|false|答题时长(分钟) (必填)|-|
|isPublished|int32|false|是否发布 (必填)<br/>0-暂存为草稿，1-正式发布。, 2删除|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/create --data '{
  "courseId": "41",
  "creatorId": "41",
  "assessmentType": "f3f8hf",
  "title": "aei0vv",
  "startTime": "2026-03-30 18:04:48",
  "endTime": "2026-03-30 18:04:48",
  "durationMinutes": 221,
  "isPublished": 572
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 561,
  "code": 900,
  "data": {},
  "message": "success"
}
```

### 发布测评
**URL:** http://localhost:8080/api/teacher/assessments/publish

**Type:** POST


**Content-Type:** application/json

**Description:** 发布测评

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|测评ID<br/>要发布的测评的唯一标识符|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/publish --data '{
  "assessmentId": "41"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 677,
  "code": 998,
  "data": {},
  "message": "success"
}
```

### 获取测评详情
**URL:** http://localhost:8080/api/teacher/assessments/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取测评详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/detail?assessmentId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 212,
  "code": 801,
  "data": {},
  "message": "success"
}
```

### 更新测评
**URL:** http://localhost:8080/api/teacher/assessments/update

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 更新测评

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|测评ID|-|
|title|string|false|       测评标题|-|
|duration|int32|false|    测评时长|-|
|startTime|string|false|   测评开始时间|-|
|endTime|string|false|     测评结束时间|-|
|isPublished|int32|false| 是否发布|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/update --data 'assessmentId=41&title=tgrsiz&duration=468&startTime=2026-03-30 18:04:47&endTime=2026-03-30 18:04:47&isPublished=917'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 7,
  "code": 140,
  "data": {},
  "message": "success"
}
```

### 删除测评 - 逻辑处理 - 将is_published = -1
**URL:** http://localhost:8080/api/teacher/assessments/delete

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除测评 - 逻辑处理 - 将is_published = -1

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|测评ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/delete --data 'assessmentId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 519,
  "code": 93,
  "data": {},
  "message": "success"
}
```

### 获取试卷结构
**URL:** http://localhost:8080/api/teacher/assessments/paper-structure

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取试卷结构

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|false|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/paper-structure?assessmentId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 405,
  "code": 210,
  "data": {},
  "message": "success"
}
```

### 获取单个测评的结果汇总<br><br>返回内容包含：实际参考人数、应参考人数、平均分、完成率。<br> 
**URL:** http://localhost:8080/api/teacher/assessments/{assessmentId}/result-summary

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取单个测评的结果汇总
<p>
返回内容包含：实际参考人数、应参考人数、平均分、完成率。
</p>

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/assessments/41/result-summary
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 226,
  "code": 593,
  "data": {},
  "message": "success"
}
```

## 教师端 - 课程相关接口
### 获取课程章节 - 仅列表
**URL:** http://localhost:8080/api/teacher/courses/chapters/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程章节 - 仅列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/courses/chapters/list?courseId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 209,
  "code": 474,
  "data": {},
  "message": "success"
}
```

### 创建章节
**URL:** http://localhost:8080/api/teacher/courses/chapters/create

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 创建章节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|  课程id|-|
|title|string|true|     章节标题|-|
|orderIndex|int32|true|排序索引|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/chapters/create --data 'courseId=41&title=1dqr4y&orderIndex=1'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 830,
  "code": 291,
  "data": {},
  "message": "success"
}
```

### 更新章节
**URL:** http://localhost:8080/api/teacher/courses/chapters/updateChapter

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 更新章节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|chapterId|string|true| 章节ID|-|
|title|string|true|     章节标题|-|
|orderIndex|int32|true|章节顺序|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/chapters/updateChapter --data 'chapterId=41&title=yzrwl9&orderIndex=1'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 293,
  "code": 332,
  "data": {},
  "message": "success"
}
```

### 获取小节列表
**URL:** http://localhost:8080/api/teacher/courses/chapters/sections

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取小节列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|chapterId|string|true|章节ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/courses/chapters/sections?chapterId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 687,
  "code": 843,
  "data": {},
  "message": "success"
}
```

### 创建小节
**URL:** http://localhost:8080/api/teacher/courses/chapters/createSection

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 创建小节

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|chapterId|string|true|      章节ID|-|
|title|string|true|          小节标题|-|
|videoUrl|string|true|       视频地址|-|
|durationSeconds|int32|true|视频时长|-|
|orderIndex|int32|true|     排序索引|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/chapters/createSection --data 'chapterId=41&title=vbysk8&videoUrl=www.dustin-schulist.biz&durationSeconds=596&orderIndex=1'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 635,
  "code": 874,
  "data": {},
  "message": "success"
}
```

## 教师文件管理控制器
实现资源文件管理的所有接口
### 教师端统一文件上传接口<br>使用 @ModelAttribute 来接收 multipart/form-data 请求。
**URL:** http://localhost:8080/api/teacher/courses/files/upload

**Type:** POST


**Content-Type:** multipart/form-data

**Description:** 教师端统一文件上传接口
使用 @ModelAttribute 来接收 multipart/form-data 请求。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|file|file|false|上传的文件本身。<br/>Spring Boot 会自动将请求中的文件部分绑定到此字段。<br/>- @NotNull: 确保请求中必须包含文件部分，不能为空。|-|
|courseId|string|true|资料关联的课程ID。<br/>这是一个必填项，因为所有资料都必须属于某一门课程。<br/>- @NotBlank: 确保课程ID不为空，且不只包含空白字符。|-|
|chapterId|string|false|资料关联的章节ID (可选)。<br/>如果资料是章节级别的，则需要此字段。<br/>如果是课程级别的资料，则此字段为空。|-|
|sectionId|string|false|资料关联的小节ID (可选)。<br/>如果资料是小节级别的，则需要此字段。<br/>如果是课程或章节级别的资料，则此字段为空。|-|
|durationSeconds|int32|false|视频时长（秒）。<br/>仅当上传的文件是视频时，此字段为必填。|-|
|title|string|true|资料的标题。<br/>这是一个必填项，用于在前端展示。<br/>- @NotBlank: 确保标题不为空，且不只包含空白字符。|-|
|allowDownload|boolean|false|是否允许学生下载该文件 (针对非视频文件)。<br/>前端可以传递此布尔值。<br/>如果前端未传递，则默认为 true，即允许下载。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization' -F 'file=' -i http://localhost:8080/api/teacher/courses/files/upload --data 'courseId=41&chapterId=41&sectionId=41&durationSeconds=767&title=0ri0zc&allowDownload=true'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 542,
  "code": 40,
  "data": {},
  "message": "success"
}
```

### 上传课程资料接口<br>使用 @ModelAttribute 来接收 multipart/form-data 请求。
**URL:** http://localhost:8080/api/teacher/materials/upload

**Type:** POST


**Content-Type:** multipart/form-data

**Description:** 上传课程资料接口
使用 @ModelAttribute 来接收 multipart/form-data 请求。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|file|file|false|上传的文件本身。<br/>Spring Boot 会自动将请求中的文件部分绑定到此字段。<br/>- @NotNull: 确保请求中必须包含文件部分，不能为空。|-|
|courseId|string|true|资料关联的课程ID。<br/>这是一个必填项，因为所有资料都必须属于某一门课程。<br/>- @NotBlank: 确保课程ID不为空，且不只包含空白字符。|-|
|chapterId|string|false|资料关联的章节ID (可选)。<br/>如果资料是章节级别的，则需要此字段。<br/>如果是课程级别的资料，则此字段为空。|-|
|sectionId|string|false|资料关联的小节ID (可选)。<br/>如果资料是小节级别的，则需要此字段。<br/>如果是课程或章节级别的资料，则此字段为空。|-|
|durationSeconds|int32|false|视频时长（秒）。<br/>仅当上传的文件是视频时，此字段为必填。|-|
|title|string|true|资料的标题。<br/>这是一个必填项，用于在前端展示。<br/>- @NotBlank: 确保标题不为空，且不只包含空白字符。|-|
|allowDownload|boolean|false|是否允许学生下载该文件 (针对非视频文件)。<br/>前端可以传递此布尔值。<br/>如果前端未传递，则默认为 true，即允许下载。|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization' -F 'file=' -i http://localhost:8080/api/teacher/materials/upload --data 'courseId=41&chapterId=41&sectionId=41&durationSeconds=536&title=x16n6l&allowDownload=true'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 662,
  "code": 590,
  "data": {},
  "message": "success"
}
```

### 统一文件访问接口。<br>处理视频流、文件下载和在线预览。<br>支持从URL参数中获取token进行认证。
**URL:** http://localhost:8080/api/teacher/files/{fileId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 统一文件访问接口。
处理视频流、文件下载和在线预览。
支持从URL参数中获取token进行认证。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|fileId|string|true|No comments found.|-|

**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|token|string|false|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/files/41?token=n646fb --data '&n646fb'
```

**Response-example:**
```
Return void.
```

## 教师端 - 试卷相关控制器
### 创建试卷
**URL:** http://localhost:8080/api/teacher/paper

**Type:** POST


**Content-Type:** application/json

**Description:** 创建试卷

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|false|试卷ID，建议与MySQL中assessments表的ID保持一致，形成一对一关联。<br/>这样既能保证唯一性，也便于直接关联查询。|-|
|courseId|string|false|冗余字段：课程ID，关联MySQL|-|
|creatorId|string|false|冗余字段：创建者ID，关联MySQL|-|
|title|string|false|试卷标题 [选填]|-|
|description|string|false|试卷描述 [选填]|-|
|assessmentId|string|false|测评id，关联MySQL [必填]|-|
|totalScore|number|false|试卷总分，由所有题目分数累加得到。<br/>可以在创建试卷时预先计算并存储，方便前端展示和后续统计。|-|
|createdAt|string|false|试卷创建时间|-|
|updatedAt|string|false|试卷最后更新时间|-|
|sections|array|false|试卷题目分组列表。<br/>允许将试卷题目按部分（如“第一部分：单选题”，“第二部分：简答题”）进行组织。<br/>如果试卷不分组，则该列表只包含一个元素。|-|
|└─title|string|false|分组标题，例如 "第一部分：选择题"|-|
|└─description|string|false|分组描述，可选，可对该部分题目做一些说明|-|
|└─questions|array|false|该分组下的题目列表|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─questionId|string|false|题目ID，关联MongoDB中questions集合的_id|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─score|number|false|该题目在本试卷中的分值|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─orderIndex|int32|false|题目在本分组内的显示顺序，从0开始|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/paper/ --data '{
  "id": "41",
  "courseId": "41",
  "creatorId": "41",
  "title": "oy78e2",
  "description": "kypx0r",
  "assessmentId": "41",
  "totalScore": 236,
  "createdAt": "2026-03-30 18:04:48",
  "updatedAt": "2026-03-30 18:04:48",
  "sections": [
    {
      "title": "43jj6m",
      "description": "7anx6u",
      "questions": [
        {
          "questionId": "41",
          "score": 619,
          "orderIndex": 1
        }
      ]
    }
  ]
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 757,
  "code": 713,
  "data": {},
  "message": "success"
}
```

### 通过assessmentId查询试卷
**URL:** http://localhost:8080/api/teacher/paper/{assessmentId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 通过assessmentId查询试卷

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/paper/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 309,
  "code": 960,
  "data": {},
  "message": "success"
}
```

## 教师端 - 题目相关接口
### 获取所有题目
**URL:** http://localhost:8080/api/teacher/questions

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取所有题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/questions/
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 187,
  "code": 748,
  "data": {},
  "message": "success"
}
```

### 获取课程所属的指定类型题目
**URL:** http://localhost:8080/api/teacher/questions/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程所属的指定类型题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|
|type|string|true|    题目类型|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/questions/list?courseId=41&type=20qy4p --data '&41&20qy4p'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 343,
  "code": 292,
  "data": {},
  "message": "success"
}
```

### 获取课程所属的题目
**URL:** http://localhost:8080/api/teacher/questions/{courseId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程所属的题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/questions/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 799,
  "code": 113,
  "data": {},
  "message": "success"
}
```

### 获取题型所属的题目
**URL:** http://localhost:8080/api/teacher/questions/list/{type}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取题型所属的题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|type|string|true|题型|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/questions/list/r76k3l
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 374,
  "code": 730,
  "data": {},
  "message": "success"
}
```

### 创建题目
**URL:** http://localhost:8080/api/teacher/questions/create

**Type:** POST


**Content-Type:** application/json

**Description:** 创建题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|false|文档唯一主键，自动生成|-|
|courseId|string|false|关联MySQL courses.id，标识题目所属课程|-|
|creatorId|string|false|关联MySQL users.id，记录题目创建者（教师）|-|
|type|enum|false|题型枚举<br/>(See: 类型枚举)|-|
|difficulty|enum|false|题目难度，枚举<br/>(See: 难度枚举)|-|
|stem|string|false|题干，即题目的主要问题描述|-|
|options|array|false|选项数组，仅用于选择题。对象包含key (如"A") 和text (选项内容)|-|
|└─key|string|false|选项键值，如"A", "B", "C", "D"|-|
|└─text|string|false|选项内容|-|
|answer|object|false|题目的正确答案。单选题为String，多选题为Array<String>，判断题为Boolean，简答题为String或Array<String>|-|
|└─any object|object|false|any object.|-|
|analysis|string|false|答案的详细解析|-|
|tags|array|false|知识点标签数组，用于题目检索和分类|-|
|isDeleted|boolean|false|逻辑删除标志，默认为false|-|
|createdAt|string|false|文档创建时间|-|
|updatedAt|string|false|文档最后更新时间|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/questions/create --data '{
  "id": "41",
  "courseId": "41",
  "creatorId": "41",
  "type": "SINGLE_CHOICE",
  "difficulty": "EASY",
  "stem": "5f822w",
  "options": [
    {
      "key": "0jfprt",
      "text": "7q8vcw"
    }
  ],
  "answer": {
    "object": "any object"
  },
  "analysis": "7hgk6j",
  "tags": [
    "rq4lfs"
  ],
  "isDeleted": true,
  "createdAt": "2026-03-30 18:04:48",
  "updatedAt": "2026-03-30 18:04:48"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 153,
  "code": 870,
  "data": {},
  "message": "success"
}
```

### 获取题目详情
**URL:** http://localhost:8080/api/teacher/questions/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取题目详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|questionId|string|true|题目ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/questions/detail?questionId=41 --data '&41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 490,
  "code": 593,
  "data": {},
  "message": "success"
}
```

### 更新题目
**URL:** http://localhost:8080/api/teacher/questions/update

**Type:** POST


**Content-Type:** application/json

**Description:** 更新题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|id|string|false|文档唯一主键，自动生成|-|
|courseId|string|false|关联MySQL courses.id，标识题目所属课程|-|
|creatorId|string|false|关联MySQL users.id，记录题目创建者（教师）|-|
|type|enum|false|题型枚举<br/>(See: 类型枚举)|-|
|difficulty|enum|false|题目难度，枚举<br/>(See: 难度枚举)|-|
|stem|string|false|题干，即题目的主要问题描述|-|
|options|array|false|选项数组，仅用于选择题。对象包含key (如"A") 和text (选项内容)|-|
|└─key|string|false|选项键值，如"A", "B", "C", "D"|-|
|└─text|string|false|选项内容|-|
|answer|object|false|题目的正确答案。单选题为String，多选题为Array<String>，判断题为Boolean，简答题为String或Array<String>|-|
|└─any object|object|false|any object.|-|
|analysis|string|false|答案的详细解析|-|
|tags|array|false|知识点标签数组，用于题目检索和分类|-|
|isDeleted|boolean|false|逻辑删除标志，默认为false|-|
|createdAt|string|false|文档创建时间|-|
|updatedAt|string|false|文档最后更新时间|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/questions/update --data '{
  "id": "41",
  "courseId": "41",
  "creatorId": "41",
  "type": "SINGLE_CHOICE",
  "difficulty": "EASY",
  "stem": "ablwzl",
  "options": [
    {
      "key": "ovom4b",
      "text": "o3nfsj"
    }
  ],
  "answer": {
    "object": "any object"
  },
  "analysis": "a8346i",
  "tags": [
    "qzoms4"
  ],
  "isDeleted": true,
  "createdAt": "2026-03-30 18:04:48",
  "updatedAt": "2026-03-30 18:04:48"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 775,
  "code": 21,
  "data": {},
  "message": "success"
}
```

### 删除题目
**URL:** http://localhost:8080/api/teacher/questions/delete

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除题目

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|questionId|string|true|题目ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/questions/delete --data 'questionId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 673,
  "code": 247,
  "data": {},
  "message": "success"
}
```

## 教师端 - 个人信息相关接口
### 获取教师信息
**URL:** http://localhost:8080/api/teacher/profile

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取教师信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|identifier|string|true|教师工号|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/profile?identifier=cz86d5 --data '&cz86d5'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 143,
  "code": 637,
  "data": {},
  "message": "success"
}
```

### 修改头像 - 调用public.api --&gt; 上传url地址
**URL:** http://localhost:8080/api/teacher/avatar

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 修改头像 - 调用public.api --> 上传url地址

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|avatarUrl|string|true|头像url|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/avatar --data 'avatarUrl=www.dustin-schulist.biz'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 490,
  "code": 966,
  "data": {},
  "message": "success"
}
```

## 教师端 - 课程相关接口
### 获取当前用户课程列表
**URL:** http://localhost:8080/api/teacher/courses/list

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前用户课程列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/courses/list
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 806,
  "code": 145,
  "data": {},
  "message": "success"
}
```

### 创建课程 (课程主体 + 课程属性)
**URL:** http://localhost:8080/api/teacher/courses

**Type:** POST


**Content-Type:** application/json

**Description:** 创建课程 (课程主体 + 课程属性)

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|title|string|false|No comments found.|-|
|description|string|false|No comments found.|-|
|coverImageUrl|string|false|No comments found.|-|
|teacherId|string|false|No comments found.|-|
|categoryId|string|false|No comments found.|-|
|status|string|false|No comments found.|-|
|privateCourse|boolean|false|No comments found.|-|
|properties|object|false|No comments found.|-|
|└─level|string|false|No comments found.|-|
|└─targetAudience|string|false|No comments found.|-|
|└─requirements|string|false|No comments found.|-|
|└─price|number|false|No comments found.|-|
|└─originalPrice|number|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/courses/ --data '{
  "title": "cj28un",
  "description": "6jsqn2",
  "coverImageUrl": "www.dustin-schulist.biz",
  "teacherId": "41",
  "categoryId": "41",
  "status": "7cqoaz",
  "privateCourse": true,
  "properties": {
    "level": "x7ss1m",
    "targetAudience": "ydrvac",
    "requirements": "ho5eos",
    "price": 278,
    "originalPrice": 173
  }
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 603,
  "code": 540,
  "data": {},
  "message": "success"
}
```

### 更新指定课程的核心信息
**URL:** http://localhost:8080/api/teacher/courses/{courseId}

**Type:** POST


**Content-Type:** application/json

**Description:** 更新指定课程的核心信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|      要更新的课程的 ID (从 URL 路径中获取)|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|title|string|false|No comments found.|-|
|description|string|false|No comments found.|-|
|coverImageUrl|string|false|No comments found.|-|
|teacherId|string|false|No comments found.|-|
|categoryId|string|false|No comments found.|-|
|status|string|false|No comments found.|-|
|privateCourse|boolean|false|No comments found.|-|
|properties|object|false|No comments found.|-|
|└─level|string|false|No comments found.|-|
|└─targetAudience|string|false|No comments found.|-|
|└─requirements|string|false|No comments found.|-|
|└─price|number|false|No comments found.|-|
|└─originalPrice|number|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/courses/41 --data '{
  "title": "e8o8xo",
  "description": "2ezgmc",
  "coverImageUrl": "www.dustin-schulist.biz",
  "teacherId": "41",
  "categoryId": "41",
  "status": "6653fb",
  "privateCourse": true,
  "properties": {
    "level": "xcqker",
    "targetAudience": "5h3gow",
    "requirements": "pumqwd",
    "price": 309,
    "originalPrice": 716
  }
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 160,
  "code": 308,
  "data": {},
  "message": "success"
}
```

### 全量更新指定课程的完整大纲 (章节 + 小节)<br>采用“先删除旧大纲，再插入新大纲”的策略
**URL:** http://localhost:8080/api/teacher/courses/{courseId}/outline

**Type:** POST


**Content-Type:** application/json

**Description:** 全量更新指定课程的完整大纲 (章节 + 小节)
采用“先删除旧大纲，再插入新大纲”的策略

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|  要更新大纲的课程的 ID|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|outline|array|false|课程大纲|-|
|└─id|string|false|No comments found.|-|
|└─title|string|false|No comments found.|-|
|└─orderIndex|int32|false|No comments found.|-|
|└─sections|array|false|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─id|string|false|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─title|string|false|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─videoUrl|string|false|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─durationSeconds|int32|false|No comments found.|-|
|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─orderIndex|int32|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/courses/41/outline --data '{
  "outline": [
    {
      "id": "41",
      "title": "cjd7et",
      "orderIndex": 1,
      "sections": [
        {
          "id": "41",
          "title": "j6yk8h",
          "videoUrl": "www.dustin-schulist.biz",
          "durationSeconds": 277,
          "orderIndex": 1
        }
      ]
    }
  ]
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 355,
  "code": 9,
  "data": {},
  "message": "success"
}
```

### 获取课程大纲
**URL:** http://localhost:8080/api/teacher/courses/{courseId}/outline

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程大纲

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/courses/41/outline
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 197,
  "code": 142,
  "data": {},
  "message": "success"
}
```

### 修改课程封面
**URL:** http://localhost:8080/api/teacher/courses/update/cover

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 修改课程封面

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|coverUrl|string|true|课程封面图片URL|-|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/update/cover --data 'coverUrl=www.dustin-schulist.biz&courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 821,
  "code": 858,
  "data": {},
  "message": "success"
}
```

### 获取课程详情
**URL:** http://localhost:8080/api/teacher/courses/detail

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取课程详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false|课程id|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/detail --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 561,
  "code": 122,
  "data": {},
  "message": "success"
}
```

### 更新课程信息
**URL:** http://localhost:8080/api/teacher/courses/course/properties

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 更新课程信息

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|false|          课程id|-|
|courseVo|string|false|          课程信息|-|
|coursePropertiesVo|string|false|课程属性信息|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/course/properties --data 'courseId=41&courseVo=2vol79&coursePropertiesVo=wh7d37'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 40,
  "code": 32,
  "data": {},
  "message": "success"
}
```

### 删除课程
**URL:** http://localhost:8080/api/teacher/courses/course/delete

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/course/delete --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 247,
  "code": 224,
  "data": {},
  "message": "success"
}
```

### 发布课程
**URL:** http://localhost:8080/api/teacher/courses/course/publish

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 发布课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/course/publish --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 995,
  "code": 122,
  "data": {},
  "message": "success"
}
```

### 归档课程
**URL:** http://localhost:8080/api/teacher/courses/course/archive

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 归档课程

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/course/archive --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 944,
  "code": 668,
  "data": {},
  "message": "success"
}
```

### 获取选课学生
**URL:** http://localhost:8080/api/teacher/courses/course/students

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取选课学生

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|courseId|string|true|课程ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/courses/course/students --data 'courseId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 355,
  "code": 829,
  "data": {},
  "message": "success"
}
```

### 上传课程封面
**URL:** http://localhost:8080/api/teacher/courses/upload/cover

**Type:** POST


**Content-Type:** multipart/form-data

**Description:** 上传课程封面

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|file|file|true|   文件|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: multipart/form-data' -H 'Authorization' -F 'file=' -i http://localhost:8080/api/teacher/courses/upload/cover
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 954,
  "code": 499,
  "data": {},
  "message": "success"
}
```

## 教师工作台控制器。
&lt;p&gt;
对外提供教师首页所需的聚合统计接口，当前主要包含“本周概览”数据查询能力。
&lt;/p&gt;
### 获取当前登录教师的本周概览数据。
**URL:** http://localhost:8080/api/teacher/dashboard/weekly-overview

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前登录教师的本周概览数据。

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/dashboard/weekly-overview
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 481,
  "code": 63,
  "data": {},
  "message": "success"
}
```

## 教师阅卷控制器
### 获取当前登录教师的所有待批阅测评任务
**URL:** http://localhost:8080/api/teacher/grading/tasks

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前登录教师的所有待批阅测评任务

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/grading/tasks
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 829,
  "code": 518,
  "data": {},
  "message": "success"
}
```

### [新增] 获取单个测评的批阅详情（主观题列表及进度）
**URL:** http://localhost:8080/api/teacher/grading/tasks/{assessmentId}

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** [新增] 获取单个测评的批阅详情（主观题列表及进度）

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/grading/tasks/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 233,
  "code": 556,
  "data": {},
  "message": "success"
}
```

### 
**URL:** http://localhost:8080/api/teacher/grading/tasks/{assessmentId}/status

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/grading/tasks/41/status
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 630,
  "code": 223,
  "data": {},
  "message": "success"
}
```

### 分页获取单个主观题的所有学生答案
**URL:** http://localhost:8080/api/teacher/grading/tasks/{assessmentId}/questions/{questionId}/answers

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 分页获取单个主观题的所有学生答案

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|
|questionId|string|true|  题目ID|-|

**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|page|int32|false|No comments found.|-|
|size|int32|false|No comments found.|-|
|sort|string|false|No comments found.|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/grading/tasks/41/questions/41/answers?page=1&size=10&sort=40yqsi
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 396,
  "code": 786,
  "data": {},
  "message": "success"
}
```

### [新增] 提交对单个答案的评分
**URL:** http://localhost:8080/api/teacher/grading/answers/grade

**Type:** POST


**Content-Type:** application/json

**Description:** [新增] 提交对单个答案的评分

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|submissionId|string|false|No comments found.|-|
|questionId|string|false|No comments found.|-|
|score|number|false|No comments found.|-|
|comment|string|false|No comments found.|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/grading/answers/grade --data '{
  "submissionId": "41",
  "questionId": "41",
  "score": 140,
  "comment": "fquk4v"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 356,
  "code": 565,
  "data": {},
  "message": "success"
}
```

### [新增] 完成并归档整个测评的批阅工作
**URL:** http://localhost:8080/api/teacher/grading/tasks/{assessmentId}/finalize

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** [新增] 完成并归档整个测评的批阅工作

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|assessmentId|string|true|测评ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/grading/tasks/41/finalize
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 687,
  "code": 802,
  "data": {},
  "message": "success"
}
```

## 教师端待办事项控制器
对外提供待办事项的查询、新增、修改、删除接口
### 获取当前登录教师的待办事项列表
**URL:** http://localhost:8080/api/teacher/todos

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前登录教师的待办事项列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/todos/
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 671,
  "code": 320,
  "data": {},
  "message": "success"
}
```

### 新增待办事项
**URL:** http://localhost:8080/api/teacher/todos

**Type:** POST


**Content-Type:** application/json

**Description:** 新增待办事项

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|content|string|true|待办事项内容<br/>不能为空，最大长度 255<br/>Validate[max: 255; ]|-|
|priority|string|false|优先级<br/>支持 high / medium / low<br/>Validate[regexp: ^(high|medium|low)?$; ]|-|
|dueDate|string|false|截止时间<br/>允许为空，若为空表示无截止时间|-|

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/todos/ --data '{
  "content": "ktvgah",
  "priority": "2czav9",
  "dueDate": "2026-03-30"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 142,
  "code": 16,
  "data": {},
  "message": "success"
}
```

### 修改待办事项
**URL:** http://localhost:8080/api/teacher/todos/{todoId}

**Type:** PUT


**Content-Type:** application/json

**Description:** 修改待办事项

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|todoId|string|true|     待办事项ID|-|

**Body-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|content|string|false|待办事项内容<br/>可选，若传入则长度不能超过 255<br/>Validate[max: 255; ]|-|
|priority|string|false|优先级<br/>可选，若传入仅支持 high / medium / low<br/>Validate[regexp: ^(high|medium|low)?$; ]|-|
|completed|boolean|false|完成状态<br/>可选，true 表示已完成，false 表示未完成|-|
|dueDate|string|false|截止时间<br/>可选，传入值将覆盖原截止时间|-|

**Request-example:**
```
curl -X PUT -H 'Content-Type: application/json' -H 'Authorization' -i http://localhost:8080/api/teacher/todos/41 --data '{
  "content": "73ovox",
  "priority": "p9sb9x",
  "completed": true,
  "dueDate": "2026-03-30"
}'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 474,
  "code": 205,
  "data": {},
  "message": "success"
}
```

### 删除待办事项（逻辑删除）
**URL:** http://localhost:8080/api/teacher/todos/{todoId}

**Type:** DELETE


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 删除待办事项（逻辑删除）

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Path-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|todoId|string|true| 待办事项ID|-|

**Request-example:**
```
curl -X DELETE -H 'Authorization' -i http://localhost:8080/api/teacher/todos/41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 708,
  "code": 938,
  "data": {},
  "message": "success"
}
```

## 
### 查看好友列表
**URL:** http://localhost:8080/api/teacher/chat/friend

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 查看好友列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/chat/friend
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 617,
  "code": 361,
  "data": {},
  "message": "success"
}
```

### 获取用户的聊天会话列表
**URL:** http://localhost:8080/api/teacher/chat/history

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取用户的聊天会话列表

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/chat/history
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 592,
  "code": 836,
  "data": {},
  "message": "success"
}
```

### 获取和指定对象的聊天记录详情
**URL:** http://localhost:8080/api/teacher/chat/history/detail

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取和指定对象的聊天记录详情

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|friendId|string|false|对象的ID|-|

**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/chat/history/detail?friendId=41
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 64,
  "code": 906,
  "data": {},
  "message": "success"
}
```

### 将与指定好友的聊天消息标记为已读
**URL:** http://localhost:8080/api/teacher/chat/read

**Type:** POST


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 将与指定好友的聊天消息标记为已读

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Query-parameters:**

| Parameter | Type | Required | Description | Since |
|-----------|------|----------|-------------|-------|
|friendId|string|true|好友ID|-|

**Request-example:**
```
curl -X POST -H 'Authorization' -i http://localhost:8080/api/teacher/chat/read --data 'friendId=41'
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 210,
  "code": 344,
  "data": {},
  "message": "success"
}
```

### 获取当前用户所有未读消息的总数
**URL:** http://localhost:8080/api/teacher/chat/unread-count

**Type:** GET


**Content-Type:** application/x-www-form-urlencoded;charset=UTF-8

**Description:** 获取当前用户所有未读消息的总数

**Request-headers:**

| Header | Type | Required | Description | Since |
|--------|------|----------|-------------|-------|
|Authorization|string|false|JWT token|-|


**Request-example:**
```
curl -X GET -H 'Authorization' -i http://localhost:8080/api/teacher/chat/unread-count
```
**Response-fields:**

| Field | Type | Description | Since |
|-------|------|-------------|-------|
|id|int64|No comments found.|-|
|code|int32|No comments found.|-|
|data|object|No comments found.|-|
|└─any object|object|any object.|-|
|message|string|No comments found.|-|

**Response-example:**
```
{
  "id": 558,
  "code": 65,
  "data": {},
  "message": "success"
}
```

