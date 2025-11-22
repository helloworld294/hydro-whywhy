-- 创建云剪贴板表
CREATE TABLE IF NOT EXISTS `clipboard` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` varchar(32) NOT NULL COMMENT '用户ID',
  `content` longtext NOT NULL COMMENT '剪贴板内容',
  `title` varchar(100) DEFAULT '未命名' COMMENT '标题/备注',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`),
  KEY `idx_gmt_modified` (`gmt_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='云剪贴板表';

