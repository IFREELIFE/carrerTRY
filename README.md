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

## 当前已实现能力（1-12步）
1. 账号与三端注册登录：学生/学校/企业注册、学校邮箱校验、企业三要素校验、RBAC权限模型
2. 企业岗位管理：完整岗位字段、分页列表、批量导入（JSON/CSV）与去重
3. 管理端审核与任务中心：岗位待审通过驳回、任务状态筛选、失败任务重试
4. 学生首次必填链路：技术栈/能力、MBTI、保证书勾选，未完成限制投递与画像匹配
5. 学生核心业务页：首页15条分页岗位、岗位详情与投递、个人中心可编辑、简历上传/建议/美化
6. 学生画像与匹配：12维画像结构、画像展示、AI总结与标签、四维匹配分
7. 学校辅导管理：老师管理、学生列表与画像查看、辅导纪要回流画像
8. 企业投递与面试流程：投递管理、面试通知、面试反馈、结合老师评语的智能筛选
9. 目标设定/个性规划/报告：目标城市职业、70%阈值判断、培养计划PDF下载、动态进度调整
10. 通告中心与成就系统：系统公告、面试通知聚合、30个成就定义与触发记录
11. AI与异步能力增强：RabbitMQ任务分发（失败重试入队）、任务排队状态、RAG记录、质量指标、错题纠偏闭环、置信度发布拦截
12. 联调验收与上线准备：验收清单接口与分步骤状态管理

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
- `POST /enterprise/jobs/import/excel`：Excel导入入口（支持 `.xlsx` / `.xls`，并兼容CSV文本上传，按同规则去重）

## 学生端接口（第4-6/9/10/11步）
- `GET /student/jobs`：浏览已审核通过岗位
- `GET /student/home?page=0`：首页岗位分页（15条）
- `GET /student/home/summary`：首页概览（签到/简历上传/MBTI/匹配岗位数/连续签到/报告列表）
- `GET /student/jobs/{id}`：查看已审核通过岗位详情
- `POST /student/applications`：投递岗位（基于当前登录学生身份写入，限制同学生重复投递同岗位）
- `GET /student/applications`：查询当前登录学生的投递记录
- `POST /student/onboarding`：首次必填链路提交
- `POST /student/activity`：记录当日活跃（30秒活跃/浏览岗位/刷新简历）
- `POST /student/check-in`：当日签到（满足活跃规则后可签到）
- `GET /student/check-in/summary`：签到与连续签到天数摘要（含“当日活跃=当日优先”规则）
- `GET /student/profile`：学生画像
- `PUT /student/center`：个人中心信息编辑
- `POST /student/resumes` / `GET /student/resumes`：简历管理
- `GET /student/reports`：已生成报告列表（发展报告/职业规划报告）
- `GET /student/mentors`：按当前学生学校查询可预约老师
- `POST /student/appointments` / `GET /student/appointments`：学生预约指导（选老师/我的预约）
- `GET /student/portrait/matches`：四维匹配分
- `POST /student/plans` / `GET /student/plans` / `GET /student/plans/pdf`：目标规划与PDF
- `GET /student/notices`：通知中心
- `GET /student/achievements`：成就记录
- `POST /student/corrections` / `GET /student/corrections`：错题纠偏闭环

## 学校端接口（第7步）
- `POST /school/feedbacks`：录入学生辅导评语（基于当前登录学校账号绑定学校）
- `GET /school/feedbacks?studentName=...`：按学生姓名查询当前学校下的评语记录（按时间倒序）
- `POST /school/mentors` / `GET /school/mentors`：老师管理
- `GET /school/students`：学生列表与画像查看

## 企业端流程扩展（第8/11步）
- `GET /enterprise/jobs/applications`：投递管理
- `GET /enterprise/jobs/applications/screening?keyword=...`：智能筛选
- `PATCH /enterprise/jobs/applications/{id}/notify`：面试通知
- `PATCH /enterprise/jobs/applications/{id}/feedback`：面试反馈
- `POST /enterprise/jobs/rag`：RAG结果与置信度发布拦截记录

## 管理端扩展（第3/10/11/12步）
- `GET /admin/ai-tasks?status=...`：任务状态列表
- `PATCH /admin/ai-tasks/{id}/retry`：失败任务重试
- `POST /admin/notices`：发布系统公告
- `POST /admin/achievements/init`：初始化30个成就定义
- `GET /admin/quality-metrics`：质量指标
- `POST /admin/acceptance` / `GET /admin/acceptance`：联调验收清单

## 配置
可通过环境变量覆盖：
- `MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USER` `MYSQL_PASSWORD`
- `NEO4J_URI` `NEO4J_USER` `NEO4J_PASSWORD`
- `APP_AI_QUEUE_NAME`（映射 `app.ai.queue-name`，默认 `careertry.ai.tasks`）
