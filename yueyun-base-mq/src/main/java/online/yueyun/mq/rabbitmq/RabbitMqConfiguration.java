package online.yueyun.mq.rabbitmq;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
 * RabbitMQ配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "yueyun.mq.rabbit-mq", name = "enabled", havingValue = "true")
public class RabbitMqConfiguration {

    /**
     * 配置RabbitMQ连接工厂
     */
    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory rabbitConnectionFactory(MqProperties properties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(properties.getRabbitMq().getHost());
        connectionFactory.setPort(properties.getRabbitMq().getPort());
        connectionFactory.setUsername(properties.getRabbitMq().getUsername());
        connectionFactory.setPassword(properties.getRabbitMq().getPassword());
        connectionFactory.setVirtualHost(properties.getRabbitMq().getVirtualHost());
        // 启用发布确认
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        // 启用发布退回
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }
    
    /**
     * 配置RabbitMQ消息转换器
     */
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 配置RabbitMQ模板
     */
    @Bean
    @ConditionalOnMissingBean(RabbitTemplate.class)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        // 开启强制消息
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }
    
    /**
     * 配置RabbitMQ管理员
     */
    @Bean
    @ConditionalOnMissingBean(RabbitAdmin.class)
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    
    /**
     * 配置RabbitMQ消息服务实现
     */
    @Bean
    @ConditionalOnMissingBean(MessageService.class)
    public MessageService messageService(RabbitTemplate rabbitTemplate,
                                       ConnectionFactory connectionFactory,
                                       MqProperties properties,
                                       MessageRecordService messageRecordService,
                                       MessageConsumedService messageConsumedService) {
        log.info("初始化RabbitMQ消息服务: host={}, port={}", 
                properties.getRabbitMq().getHost(), properties.getRabbitMq().getPort());
        return new RabbitMqMessageServiceImpl(
                rabbitTemplate,
                connectionFactory,
                properties,
                messageRecordService,
                messageConsumedService);
    }
} 