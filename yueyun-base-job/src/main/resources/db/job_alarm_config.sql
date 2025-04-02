CREATE TABLE `job_alarm_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `job_id` bigint(20) NOT NULL COMMENT '任务ID',
  `alarm_type` tinyint(4) NOT NULL COMMENT '告警类型（1：邮件 2：钉钉）',
  `receivers` varchar(500) NOT NULL COMMENT '告警接收人（邮件地址或钉钉用户ID，多个用逗号分隔）',
  `alarm_template` text COMMENT '告警模板',
  `enabled` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否启用（0：禁用 1：启用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_alarm` (`job_id`,`alarm_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务告警配置表'; 