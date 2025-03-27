-- 消息记录表
CREATE TABLE IF NOT EXISTS `mq_message_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `msg_id` varchar(64) NOT NULL COMMENT '消息ID',
    `business_key` varchar(128) DEFAULT NULL COMMENT '业务键',
    `topic` varchar(64) NOT NULL COMMENT '消息主题',
    `tag` varchar(64) DEFAULT NULL COMMENT '消息标签（RocketMQ专用）',
    `content` text NOT NULL COMMENT '消息内容（JSON格式）',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '消息状态：0-待发送，1-已发送，2-发送失败，3-已消费，4-消费失败',
    `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
    `max_retry_count` int(11) NOT NULL DEFAULT '3' COMMENT '最大重试次数',
    `consumer_group` varchar(64) DEFAULT NULL COMMENT '消费组',
    `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_id` (`msg_id`),
    INDEX `idx_business_key` (`business_key`),
    INDEX `idx_topic` (`topic`),
    INDEX `idx_status` (`status`),
    INDEX `idx_next_retry_time` (`next_retry_time`),
    INDEX `idx_consumer_group` (`consumer_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- 消息消费记录表（用于幂等性控制）
CREATE TABLE IF NOT EXISTS `mq_message_consumed` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `msg_id` varchar(64) NOT NULL COMMENT '消息ID',
    `consumer_group` varchar(64) NOT NULL COMMENT '消费组',
    `consume_time` datetime NOT NULL COMMENT '消费时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_msg_id_consumer_group` (`msg_id`, `consumer_group`),
    INDEX `idx_consumer_group` (`consumer_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息消费记录表'; 