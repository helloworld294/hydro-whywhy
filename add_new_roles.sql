-- 添加新用户角色: 1009 禁止修改个人资料, 1010 禁止修改个人资料&禁言

-- 检查并插入角色1009: 禁止修改个人资料
INSERT INTO `role` (`id`, `role`, `description`, `status`, `gmt_create`, `gmt_modified`)
SELECT 1009, 'no_profile_edit_user', '用户(禁止修改个人资料)', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 1009);

-- 检查并插入角色1010: 禁止修改个人资料&禁言
INSERT INTO `role` (`id`, `role`, `description`, `status`, `gmt_create`, `gmt_modified`)
SELECT 1010, 'no_profile_edit_mute_user', '用户(禁止修改个人资料&禁言)', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `id` = 1010);

-- 为角色1009添加权限 (参考默认用户的权限)
-- 获取默认用户(1002)的权限并复制给1009
INSERT INTO `role_auth` (`auth_id`, `role_id`, `gmt_create`, `gmt_modified`)
SELECT ra.`auth_id`, 1009, NOW(), NOW()
FROM `role_auth` ra
WHERE ra.`role_id` = 1002
AND NOT EXISTS (SELECT 1 FROM `role_auth` WHERE `role_id` = 1009 AND `auth_id` = ra.`auth_id`);

-- 为角色1010添加权限 (参考禁言用户(1005)的权限)
-- 获取禁言用户(1005)的权限并复制给1010
INSERT INTO `role_auth` (`auth_id`, `role_id`, `gmt_create`, `gmt_modified`)
SELECT ra.`auth_id`, 1010, NOW(), NOW()
FROM `role_auth` ra
WHERE ra.`role_id` = 1005
AND NOT EXISTS (SELECT 1 FROM `role_auth` WHERE `role_id` = 1010 AND `auth_id` = ra.`auth_id`);

