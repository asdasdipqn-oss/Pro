-- 考核方案表
CREATE TABLE IF NOT EXISTS `assess_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `plan_name` VARCHAR(200) NOT NULL COMMENT '方案名称',
  `plan_type` INT NOT NULL COMMENT '方案类型：1-月度考核 2-季度考核 3-年度考核',
  `plan_year` INT COMMENT '年份',
  `plan_month` INT COMMENT '月份（月度考核时使用）',
  `plan_quarter` INT COMMENT '季度（季度考核时使用）',
  `start_date` DATETIME COMMENT '考核开始时间',
  `end_date` DATETIME COMMENT '考核结束时间',
  `description` TEXT COMMENT '方案说明',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已结束',
  `create_by` BIGINT COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_year_month` (`plan_year`, `plan_month`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核方案表';

-- 考核任务表
CREATE TABLE IF NOT EXISTS `assess_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `plan_id` BIGINT COMMENT '关联考核方案ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述/要求',
  `deadline` DATETIME COMMENT '截止时间',
  `allow_file_types` VARCHAR(500) COMMENT '允许的文件类型',
  `max_file_size` INT COMMENT '最大文件大小(MB)',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已截止',
  `create_by` BIGINT COMMENT '创建人用户ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan` (`plan_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核任务表';

-- 考核指标表
CREATE TABLE IF NOT EXISTS `assess_indicator` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `plan_id` BIGINT COMMENT '关联考核方案ID',
  `indicator_name` VARCHAR(200) NOT NULL COMMENT '指标名称',
  `indicator_code` VARCHAR(50) COMMENT '指标编码',
  `weight` DECIMAL(5,2) COMMENT '权重(%)',
  `max_score` DECIMAL(5,2) COMMENT '满分',
  `scoring_standard` TEXT COMMENT '评分标准',
  `sort_order` INT COMMENT '排序',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_plan` (`plan_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核指标表';

-- 考核提交表
CREATE TABLE IF NOT EXISTS `assess_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `task_id` BIGINT NOT NULL COMMENT '考核任务ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `file_name` VARCHAR(255) COMMENT '文件名',
  `file_path` VARCHAR(500) COMMENT '文件存储路径',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `revision_no` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0-待评分 1-已评分 2-已退回',
  `score` DECIMAL(5,2) COMMENT '得分',
  `grade` VARCHAR(10) COMMENT '等级：A-优秀 B-良好 C-合格 D-待改进 E-不合格',
  `scorer_id` BIGINT COMMENT '评分人ID',
  `scorer_comment` TEXT COMMENT '评语',
  `score_time` DATETIME COMMENT '评分时间',
  `submit_time` DATETIME NOT NULL COMMENT '提交时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_employee` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核提交表';

-- 考核结果表
CREATE TABLE IF NOT EXISTS `assess_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `employee_id` BIGINT NOT NULL COMMENT '员工ID',
  `plan_id` BIGINT COMMENT '考核方案ID',
  `task_id` BIGINT COMMENT '考核任务ID',
  `total_score` DECIMAL(5,2) COMMENT '总分',
  `grade` VARCHAR(10) COMMENT '等级',
  `assess_year` INT COMMENT '考核年份',
  `assess_month` INT COMMENT '考核月份',
  `assessor_id` BIGINT COMMENT '考核人ID',
  `assess_time` DATETIME COMMENT '考核时间',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_employee` (`employee_id`),
  KEY `idx_plan` (`plan_id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_year_month` (`assess_year`, `assess_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核结果表';

-- 考核评分详情表
CREATE TABLE IF NOT EXISTS `assess_score_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `submission_id` BIGINT NOT NULL COMMENT '提交记录ID',
  `indicator_id` BIGINT NOT NULL COMMENT '指标ID',
  `score` DECIMAL(5,2) COMMENT '得分',
  `remark` TEXT COMMENT '评语',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_submission` (`submission_id`),
  KEY `idx_indicator` (`indicator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核评分详情表';

-- 插入考核菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`)
VALUES
-- 考核方案管理（管理员/HR可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/assess' LIMIT 1) AS t), '考核方案', 2, '/assess/plan', 'assess/AssessPlanView', 'document', 10, 1, 1, NOW(), NOW()),
-- 考核任务管理（管理员/HR可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/assess' LIMIT 1) AS t), '考核任务', 2, '/assess/task', 'assess/AssessTaskView', 'edit', 11, 1, 1, NOW(), NOW()),
-- 考核结果（管理员/HR/经理可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/assess' LIMIT 1) AS t), '考核结果', 2, '/assess/result', 'assess/AssessResultView', 'data-analysis', 12, 1, 1, NOW(), NOW()),
-- 我的考核（所有人可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/assess' LIMIT 1) AS t), '我的考核', 2, '/assess/my', 'assess/MyAssessView', 'user', 13, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- 为管理员角色分配考核菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `sys_role` r, `sys_menu` m
WHERE r.`role_code` IN ('ADMIN', 'HR', 'MANAGER')
  AND m.`path` IN ('/assess/plan', '/assess/task', '/assess/result');

-- 为所有角色分配我的考核菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `sys_role` r, `sys_menu` m
WHERE m.`path` = '/assess/my';
