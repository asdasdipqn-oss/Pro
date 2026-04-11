-- 初始化角色数据
-- 如果角色已存在则忽略

-- 管理员角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `status`, `sort`, `create_time`, `update_time`, `deleted`)
VALUES
(1, 'ADMIN', '超级管理员', '拥有所有权限', 1, 1, 1, NOW(), NOW(), 0);

-- HR管理员角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `status`, `sort`, `create_time`, `update_time`, `deleted`)
VALUES
(2, 'HR', 'HR管理员', '拥有招聘管理权限', 2, 1, 2, NOW(), NOW(), 0);

-- 普通员工角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `status`, `sort`, `create_time`, `update_time`, `deleted`)
VALUES
(3, 'EMPLOYEE', '普通员工', '普通员工，拥有基本权限', 4, 1, 3, NOW(), NOW(), 0);

-- 求职者角色
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `status`, `sort`, `create_time`, `update_time`, `deleted`)
VALUES
(4, 'CANDIDATE', '求职者', '求职者，只能查看和投递简历', 4, 1, 4, NOW(), NOW(), 0);

-- 为ADMIN角色分配所有菜单权限（动态查询）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE deleted = 0;

-- 确保招聘管理父菜单存在
INSERT IGNORE INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`, `deleted`)
VALUES
(0, '招聘管理', 1, '/recruit', NULL, 'briefcase', 15, 1, 1, NOW(), NOW(), 0);

-- 插入招聘管理子菜单
INSERT IGNORE INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`, `visible`, `create_time`, `update_time`, `deleted`, `permission`)
VALUES
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' AND deleted = 0 LIMIT 1) AS t), '招聘岗位', 2, '/recruit/jobs', 'recruit/JobListView', 'briefcase', 1, 1, 1, NOW(), NOW(), 0, 'recruit:job:view'),
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' AND deleted = 0 LIMIT 1) AS t), '简历管理', 2, '/recruit/resumes', 'recruit/ResumeListView', 'file', 2, 1, 1, NOW(), NOW(), 0, 'recruit:resume:view'),
((SELECT id FROM (SELECT id FROM sys_menu WHERE path = '/recruit' AND deleted = 0 LIMIT 1) AS t), '面试管理', 2, '/recruit/interviews', 'recruit/InterviewListView', 'chat', 3, 1, 1, NOW(), NOW(), 0, 'recruit:interview:view');

-- 为HR角色分配招聘管理菜单权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.deleted = 0 AND (m.path LIKE '/recruit%' OR m.parent_id IN (SELECT id FROM sys_menu WHERE path = '/recruit'));

-- 为员工角色分配基本菜单权限（工作台、个人中心等）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, m.id
FROM `sys_menu` m
WHERE m.deleted = 0 AND m.menu_type = 2 AND (
    m.path LIKE '/attendance/%' OR
    m.path LIKE '/leave/%' OR
    m.path = '/profile' OR
    m.path = '/notification' OR
    m.path = '/salary/my'
);

-- 打印调试信息
SELECT '角色初始化完成' AS message;
SELECT * FROM `sys_role` WHERE deleted = 0;
SELECT 'HR角色分配的菜单' AS message, m.* FROM `sys_menu` m
INNER JOIN `sys_role_menu` rm ON m.id = rm.menu_id
WHERE rm.role_id = 2 AND m.deleted = 0;
