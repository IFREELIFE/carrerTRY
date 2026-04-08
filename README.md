# carrerTRY

基于 Spring Boot 的后端脚手架，按「企业端 -> 管理端 -> 学生端 -> 学校端」顺序实现核心能力。

## 技术栈
- Spring Boot 3
- MySQL（结构化业务数据）
- Neo4j（技能图谱节点）
- RESTful API
- RBAC（若依风格角色分端）

## API 路径约定
- `/enterprise/...`
- `/admin/...`
- `/student/...`
- `/school/...`

## 当前已实现能力
1. 企业端：岗位创建、岗位批量导入、岗位详情
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
- `admin/123456`
- `enterprise/123456`
- `student/123456`
- `school/123456`

## 运行
```bash
mvn spring-boot:run
```

## 配置
可通过环境变量覆盖：
- `MYSQL_HOST` `MYSQL_PORT` `MYSQL_DB` `MYSQL_USER` `MYSQL_PASSWORD`
- `NEO4J_URI` `NEO4J_USER` `NEO4J_PASSWORD`
