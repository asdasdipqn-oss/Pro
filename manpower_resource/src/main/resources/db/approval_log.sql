-- 审批日志表
CREATE TABLE IF NOT EXISTS `approval_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `business_type` varchar(50) NOT NULL COMMENT '业务类型(请假申请/考勤申诉等)',
  `business_id` bigint NOT NULL COMMENT '业务ID',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `action` varchar(50) NOT NULL COMMENT '操作动作(通过/驳回)',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_approver` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批日志表';
