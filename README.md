# carrerTRY

基于 Spring Boot 的后端脚手架，按「企业端 -> 管理端 -> 学生端 -> 学校端」顺序实现核心能力。

## 技术栈
- Spring Boot 3
- MySQL（结构化业务数据）
- Neo4j（技能图谱节点）
- RESTful API
- RBAC（若依风格角色分端）
- React + Vite（前端同仓）

## API 路径约定
- `/enterprise/...`
- `/admin/...`
- `/student/...`
- `/school/...`

## 当前已实现能力
0. 账号基础：学生/学校/企业注册，数据库用户模型与角色鉴权
1. 企业端：岗位完整字段创建、岗位分页列表、Excel（CSV）导入去重、岗位详情
2. 管理端：岗位审核通过/驳回、岗位列表、AI任务监控列表
3. 学生端：岗位浏览、岗位详情、岗位投递、岗位匹配
4. 学校端：辅导评语录入、按学生查询评语

## RBAC（若依风格）
采用角色分端访问控制：
- `ADMIN` 访问 `/admin/**`
- `ENTERPRISE` 访问 `/enterprise/**`
- `STUDENT` 访问 `/student/**`
- `SCHOOL` 访问 `/school/**`

默认测试账号（HTTP Basic）：
- `admin/${RBAC_ADMIN_PASSWORD:Admin@1234}`
- `enterprise/${RBAC_ENTERPRISE_PASSWORD:Enterprise@1234}`
- `student/${RBAC_STUDENT_PASSWORD:Student@1234}`
- `school/${RBAC_SCHOOL_PASSWORD:School@1234}`

注册接口（免登录）：
- `POST /auth/register/student`（邮箱+学校绑定）
- `POST /auth/register/school`（仅 `.edu` / `.edu.cn` 邮箱）
- `POST /auth/register/enterprise`（企业三要素校验接口已预留）

## 运行
```bash
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

## 前端路由
- `/enterprise/...`
- `/admin/...`
- `/student/...`
- `/school/...`
- `/auth/register`

## 企业岗位接口（第2步）
- `POST /enterprise/jobs`：创建岗位（名称、部门、地点、薪资区间、经验、学历、技能、描述、状态）
- `GET /enterprise/jobs?page=0&size=10&enterpriseName=...`：企业岗位分页列表
- `POST /enterprise/jobs/import`：JSON批量导入（同企业+岗位+部门+地点去重）
- `POST /enterprise/jobs/import/excel`：Excel导入入口（当前支持 CSV 上传并按同规则去重）

## 配置
可通过环境变量覆盖：
- `MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USER` `MYSQL_PASSWORD`
- `NEO4J_URI` `NEO4J_USER` `NEO4J_PASSWORD`
