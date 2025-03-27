package online.yueyun.mq.rocketmq;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.service.MessageConsumedService;
import online.yueyun.mq.service.MessageRecordService;
import online.yueyun.mq.service.MessageService;

import lombok.extern.slf4j.Slf4j;

/**
 * RocketMQ配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "yueyun.mq.rocket-mq", name = "enabled", havingValue = "true")
public class RocketMqConfiguration {

    /**
     * 配置RocketMQ生产者
     */
    @Bean
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    public DefaultMQProducer defaultMQProducer(MqProperties properties) throws MQClientException {
        log.info("初始化RocketMQ生产者: nameServer={}, group={}", 
                properties.getRocketMq().getNameServer(), 
                properties.getRocketMq().getProducerGroup());
        
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(properties.getRocketMq().getNameServer());
        producer.setProducerGroup(properties.getRocketMq().getProducerGroup());
        producer.setSendMsgTimeout(properties.getRocketMq().getSendMessageTimeout());
        producer.setCompressMsgBodyOverHowmuch(properties.getRocketMq().getCompressMsgBodyOverHowmuch());
        producer.setRetryTimesWhenSendFailed(3);
        producer.setRetryTimesWhenSendAsyncFailed(3);
        
        // 启动生产者
        producer.start();
        log.info("RocketMQ生产者启动成功");
        
        // 应用关闭时关闭生产者
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭RocketMQ生产者");
            producer.shutdown();
        }));
        
        return producer;
    }

    /**
     * 配置RocketMQ消费者
     */
    @Bean
    @ConditionalOnMissingBean(DefaultMQPushConsumer.class)
    public DefaultMQPushConsumer defaultMQPushConsumer(MqProperties properties) throws MQClientException {
        log.info("初始化RocketMQ消费者: nameServer={}, group={}", 
                properties.getRocketMq().getNameServer(), 
                properties.getRocketMq().getConsumerGroup());
        
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        consumer.setNamesrvAddr(properties.getRocketMq().getNameServer());
        consumer.setConsumerGroup(properties.getRocketMq().getConsumerGroup());

        //todo 设置主题模式
//        setTopicPattern(consumer, properties.getRocketMq().getDefaultTopic());

        // 不自动启动消费者，需要手动订阅后再启动
        // consumer.setStartDeliverTime(System.currentTimeMillis()); // 此方法不存在于DefaultMQPushConsumer
        
        // 应用关闭时关闭消费者
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("关闭RocketMQ消费者");
            consumer.shutdown();
        }));
        
        return consumer;
    }

    /**
     * 设置主题模式
     *
     * @param consumer RocketMQ消费者
     * @param topicPattern 主题模式
     */
    private void setTopicPattern(DefaultMQPushConsumer consumer, String topicPattern) {
        try {
            consumer.subscribe(topicPattern, "*");
        } catch (MQClientException e) {
            log.error("设置主题模式失败: topicPattern={}", topicPattern, e);
        }
    }

    /**
     * 配置RocketMQ消息服务实现
     */
    @Bean
    @ConditionalOnMissingBean(MessageService.class)
    public MessageService messageService(
            DefaultMQProducer mqProducer, 
            DefaultMQPushConsumer mqPushConsumer, 
            MqProperties properties,
            MessageRecordService messageRecordService,
            MessageConsumedService messageConsumedService) {
        return new RocketMqMessageServiceImpl(
                mqProducer, 
                mqPushConsumer, 
                properties, 
                messageRecordService, 
                messageConsumedService);
    }
} 