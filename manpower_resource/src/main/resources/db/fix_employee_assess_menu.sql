-- 修复员工“我的考核”菜单不可见问题
-- 执行方式：mysql --ssl-mode=DISABLED -h<host> -P<port> -u<user> -p<password> -D manpower_resource < fix_employee_assess_menu.sql

USE manpower_resource;

-- 1) 修正考核菜单路由配置（确保是可点击菜单，且路径与前端路由一致）
UPDATE sys_menu
SET parent_id = 13,
    menu_type = 2,
    path = '/assess/task',
    component = 'assess/AssessTaskView',
    visible = 1,
    status = 1,
    update_time = NOW()
WHERE id = 143;

UPDATE sys_menu
SET parent_id = 13,
    menu_type = 2,
    path = '/assess/my',
    component = 'assess/MyAssessView',
    visible = 1,
    status = 1,
    update_time = NOW()
WHERE id = 144;

-- 2) 补齐 EMPLOYEE 角色父目录权限（避免有子菜单但无父目录导致不显示）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, 13
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 4 AND menu_id = 13
);

-- 3) 幂等补齐 EMPLOYEE 对“我的考核”菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 4, 144
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 4 AND menu_id = 144
);

-- 4) 验证
SELECT id, parent_id, menu_name, menu_type, path, component, visible, status, deleted
FROM sys_menu
WHERE id IN (13, 143, 144)
ORDER BY id;

SELECT role_id, menu_id
FROM sys_role_menu
WHERE role_id = 4
  AND menu_id IN (13, 144)
ORDER BY menu_id;
