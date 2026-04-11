-- 招聘投递申请表
CREATE TABLE IF NOT EXISTS `recruit_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '投递ID',
  `job_id` BIGINT NOT NULL COMMENT '岗位ID',
  `job_seeker_id` BIGINT NOT NULL COMMENT '求职者ID',
  `resume_id` BIGINT COMMENT '简历ID（可为空）',
  `status` INT DEFAULT 0 COMMENT '投递状态：0-待处理 1-已查看 2-面试中 3-已录用 4-未通过',
  `cover_letter` TEXT COMMENT '求职信',
  `self_intro` TEXT COMMENT '个人说明',
  `hr_comment` TEXT COMMENT 'HR备注',
  `hr_review_time` DATETIME COMMENT 'HR审核时间',
  `apply_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_job_seeker` (`job_seeker_id`),
  KEY `idx_job` (`job_id`),
  KEY `idx_resume` (`resume_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招聘投递申请表';
