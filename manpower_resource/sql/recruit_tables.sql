-- 招聘岗位表
CREATE TABLE IF NOT EXISTS `recruit_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `job_code` VARCHAR(50) COMMENT '岗位编号',
  `job_name` VARCHAR(200) NOT NULL COMMENT '岗位名称',
  `dept_id` BIGINT COMMENT '招聘部门ID',
  `position_id` BIGINT COMMENT '对应岗位ID',
  `headcount` INT COMMENT '招聘人数',
  `hired_count` INT DEFAULT 0 COMMENT '已录用人数',
  `salary_min` DECIMAL(10,2) COMMENT '薪资下限',
  `salary_max` DECIMAL(10,2) COMMENT '薪资上限',
  `education_req` INT DEFAULT 1 COMMENT '学历要求：1-不限 2-大专 3-本科 4-硕士 5-博士',
  `experience_req` VARCHAR(200) COMMENT '经验要求',
  `job_description` TEXT COMMENT '岗位描述',
  `requirements` TEXT COMMENT '任职要求',
  `benefits` TEXT COMMENT '福利待遇',
  `location` VARCHAR(200) COMMENT '工作地点',
  `publisher_id` BIGINT COMMENT '发布人ID',
  `publish_time` DATETIME COMMENT '发布时间',
  `deadline` DATE COMMENT '截止日期',
  `status` INT DEFAULT 1 COMMENT '状态：0-草稿 1-招聘中 2-已暂停 3-已结束',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_dept` (`dept_id`),
  KEY `idx_position` (`position_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘岗位表';

-- 简历投递表
CREATE TABLE IF NOT EXISTS `recruit_resume` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `job_id` BIGINT NOT NULL COMMENT '招聘岗位ID',
  `job_name` VARCHAR(200) COMMENT '岗位名称',
  `candidate_id` BIGINT COMMENT '候选人ID',
  `name` VARCHAR(100) NOT NULL COMMENT '真实姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号码',
  `email` VARCHAR(100) COMMENT '邮箱',
  `gender` INT COMMENT '性别：0-女 1-男',
  `education` INT COMMENT '学历：1-高中 2-大专 3-本科 4-硕士 5-博士',
  `work_years` INT COMMENT '工作年限',
  `graduate_school` VARCHAR(200) COMMENT '毕业院校',
  `major` VARCHAR(200) COMMENT '专业',
  `self_intro` TEXT COMMENT '自我介绍',
  `status` INT DEFAULT 0 COMMENT '状态：0-待筛选 1-已通过筛选 2-已拒绝 3-面试中 4-已录用 5-已放弃',
  `screen_time` DATETIME COMMENT '筛选时间',
  `screen_remark` TEXT COMMENT '筛选备注',
  `submit_time` DATETIME NOT NULL COMMENT '投递时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_job` (`job_id`),
  KEY `idx_candidate` (`candidate_id`),
  KEY `idx_status` (`status`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简历投递表';

-- 面试记录表
CREATE TABLE IF NOT EXISTS `recruit_interview` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '面试ID',
  `resume_id` BIGINT NOT NULL COMMENT '简历ID',
  `job_id` BIGINT NOT NULL COMMENT '招聘岗位ID',
  `interviewer_id` BIGINT COMMENT '面试官ID',
  `interview_time` DATETIME COMMENT '面试时间',
  `interview_type` INT DEFAULT 1 COMMENT '面试类型：1-初试 2-复试 3-终试',
  `interview_location` VARCHAR(200) COMMENT '面试地点',
  `status` INT DEFAULT 0 COMMENT '状态：0-待面试 1-已面试 2-已取消',
  `score` DECIMAL(5,2) COMMENT '评分',
  `evaluation` TEXT COMMENT '面试评价',
  `result` INT COMMENT '面试结果：1-通过 2-不通过',
  `complete_time` DATETIME COMMENT '完成时间',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_resume` (`resume_id`),
  KEY `idx_job` (`job_id`),
  KEY `idx_interviewer` (`interviewer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面试记录表';

-- 插入招聘管理菜单
INSERT IGNORE INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`)
VALUES
-- 招聘管理父菜单（如果不存在则创建）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' LIMIT 1) AS t), '招聘岗位', 2, '/recruit/jobs', 'recruit/RecruitJobView', 'briefcase', 1, 1, 1, NOW(), NOW()),
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' LIMIT 1) AS t), '简历管理', 2, '/recruit/resumes', 'recruit/RecruitResumeView', 'file', 2, 1, 1, NOW(), NOW()),
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' LIMIT 1) AS t), '面试管理', 2, '/recruit/interviews', 'recruit/RecruitInterviewView', 'chat', 3, 1, 1, NOW(), NOW()),
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' LIMIT 1) AS t), '招聘统计', 2, '/recruit/statistics', 'recruit/RecruitStatisticsView', 'data-analysis', 4, 1, 1, NOW(), NOW());

-- 确保招聘管理父菜单存在
INSERT IGNORE INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`)
VALUES
(0, '招聘管理', 1, '/recruit', NULL, 'briefcase', 15, 1, 1, NOW(), NOW());

-- 为管理员和HR角色分配招聘管理菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `sys_role` r, `sys_menu` m
WHERE r.`role_code` IN ('ADMIN', 'HR')
  AND m.`path` LIKE '/recruit%';
