-- 培训需求表
CREATE TABLE IF NOT EXISTS `train_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '需求ID',
  `request_code` VARCHAR(50) NOT NULL COMMENT '需求编号',
  `train_title` VARCHAR(200) NOT NULL COMMENT '培训主题',
  `train_type` INT NOT NULL COMMENT '培训类型：1-入职培训 2-技能培训 3-管理培训 4-安全培训 5-其他',
  `expected_date` DATETIME COMMENT '期望培训时间',
  `expected_hours` INT COMMENT '期望培训时长（小时）',
  `request_reason` TEXT COMMENT '培训需求描述',
  `participant_count` INT COMMENT '参与人数',
  `status` INT NOT NULL DEFAULT 0 COMMENT '审批状态：0-待审批 1-已通过 2-已拒绝 3-已取消',
  `approval_comment` VARCHAR(500) COMMENT '审批意见',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approval_time` DATETIME COMMENT '审批时间',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `update_time` DATETIME NOT NULL COMMENT '更新时间',
  `deleted` INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_applicant` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培训需求表';

-- 插入培训需求菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`)
VALUES
-- 培训需求（所有人可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/train/my' LIMIT 1) AS t), '培训需求', 2, '/train/request', NULL, 'edit', 20, 1, 1, NOW(), NOW()),
-- 培训审核（管理员/HR/经理可见）
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/train/list' LIMIT 1) AS t), '培训审核', 2, '/train/approval', NULL, 'check', 21, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `menu_name`=VALUES(`menu_name`);

-- 为管理员角色分配培训审核菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id
FROM `sys_role` r, `sys_menu` m
WHERE r.`role_code` IN ('ADMIN', 'HR', 'MANAGER')
  AND m.`path` = '/train/approval';
