# 人力资源管理系统 (HR Management System)

一个基于 Spring Boot 3 + Vue 3 + Element Plus 构建的人力资源管理系统，提供员工管理、考勤管理、薪酬管理、绩效管理、招聘管理等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.5.7
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.5
- **连接池**: Druid 1.2.20
- **安全**: Spring Security + JWT
- **文档**: Knife4j (Swagger) 4.4.0
- **其他**: LangChain4j 0.35.0 (AI 集成)、Hutool 5.8.24、Apache POI 5.2.5

### 前端
- **框架**: Vue 3.5.25
- **UI 组件**: Element Plus 2.12.0
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 4.6.3
- **HTTP**: Axios 1.13.2
- **图表**: ECharts 6.0.0
- **地图**: OpenLayers 10.7.0
- **构建工具**: Vite 7.2.4

## 项目结构

```
manpower_resource_cms/
├── manpower_resource/          # 后端项目
│   ├── src/main/java/...      # Java 源码
│   └── src/main/resources/   # 资源文件
└── manpower_resource_cms/    # 前端项目
    ├── src/
    │   ├── api/              # API 接口
    │   ├── components/        # 公共组件
    │   ├── stores/           # 状态管理
    │   ├── utils/            # 工具函数
    │   └── views/           # 页面视图
    └── package.json
```

## 核心功能模块

### 1. 系统管理
- 用户管理：用户增删改查、角色分配、密码重置、状态管理
- 角色管理：角色配置、权限分配
- 菜单管理：菜单配置、权限关联
- 字典管理：系统字典配置
- 操作日志：系统操作审计
- 审批日志：审批流程审计
- 配置管理：系统参数配置
- 节假日管理：节假日设置

### 2. 组织管理
- 部门管理：组织架构维护
- 岗位管理：岗位信息维护
- 员工管理：员工档案维护
- 员工调岗：调岗记录管理

### 3. 考勤管理
- 打卡记录：员工打卡数据管理
- 打卡位置：考勤地点设置
- 考勤规则：考勤规则配置
- 考勤统计：日统计、月统计
- 考勤申诉：打卡异常申诉与审批

### 4. 薪酬管理
- 薪酬标准：薪酬体系配置
- 薪酬项目：薪酬项目定义
- 薪酬明细：薪酬发放明细
- 我的薪酬：个人薪酬查询

### 5. 绩效管理
- 绩效计划：绩效评估计划
- 绩效指标：评估指标配置
- 绩效任务：评估任务管理
- 绩效提交：员工自评提交
- 绩效评分：主管评分管理
- 我的绩效：个人绩效查询

### 6. 招聘管理
- 职位发布：招聘岗位管理
- 简历管理：应聘简历管理
- 面试管理：面试安排与记录

### 7. 请假管理
- 请假类型：请假类型配置
- 请假申请：请假申请与审批
- 请假余额：请假额度管理

### 8. 辞职管理
- 辞职申请：员工辞职申请
- 辞职预测：AI 驱动的离职风险分析

### 9. 审批管理
- 待办审批：待审批事项处理
- 已办审批：历史审批记录
- 流程管理：审批流程配置

### 10. 培训管理
- 培训计划：培训活动管理
- 培训参与：培训报名管理

### 11. 数据报表
- 综合报表：人力资源数据统计与可视化

### 12. AI 功能
- 辞职预测：基于员工数据的离职风险预测
- 智能分析：员工数据智能分析

## 快速开始

### 环境要求
- **JDK**: 17 或 21
- **Node.js**: 20.19.0 或 >= 22.12.0
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 后端启动

```bash
# 进入后端目录
cd manpower_resource

# 使用 JDK 17 启动（推荐）
JAVA_HOME=/path/to/jdk17 mvn spring-boot:run

# 或使用 JDK 21 启动（需更新 pom.xml 中的 java.version）
mvn spring-boot:run
```

后端默认运行在：http://localhost:8080/api

API 文档：http://localhost:8080/api/doc.html

### 前端启动

```bash
# 进入前端目录
cd manpower_resource_cms

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认运行在：http://localhost:5173

### 数据库配置

修改 `manpower_resource/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-host:3306/manpower_resource?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your-username
    password: your-password
```

## 开发规范

### 后端
- 统一使用 RESTful API 风格
- 使用 Result 统一返回格式
- 使用 Spring Security + JWT 进行权限认证
- 使用 Lombok 简化实体类

### 前端
- 使用 Composition API
- 使用 Pinia 进行状态管理
- 使用 Element Plus 组件库
- 使用 Axios 进行 HTTP 请求

## 许可证

本项目仅供学习交流使用。

## 作者

成都信息工程大学
