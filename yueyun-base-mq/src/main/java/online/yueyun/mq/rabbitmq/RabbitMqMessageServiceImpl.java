package online.yueyun.mq.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.model.MessageRecord;
import online.yueyun.mq.service.AbstractMessageService;
import online.yueyun.mq.service.MessageConsumedService;
import online.yueyun.mq.service.MessageRecordService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * RabbitMQ消息服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class RabbitMqMessageServiceImpl extends AbstractMessageService {

    private final RabbitTemplate rabbitTemplate;
    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;

    public RabbitMqMessageServiceImpl(
            RabbitTemplate rabbitTemplate,
            ConnectionFactory connectionFactory,
            MqProperties properties,
            MessageRecordService messageRecordService,
            MessageConsumedService messageConsumedService) {
        super(properties, messageRecordService, messageConsumedService);
        this.rabbitTemplate = rabbitTemplate;
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
    }

    /**
     * 生成RabbitMQ特有的消息ID
     * 可以根据需求自定义ID生成逻辑
     */
    @Override
    protected <T> String generateMessageId(String topic, String key, T message) {
        // 使用默认的UUID生成逻辑，也可以根据需要自定义
        return super.generateMessageId(topic, key, message);
    }

    @Override
    public <T> boolean send(String topic, String key, T message, Map<String, Object> headers) {
        try {
            String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
            byte[] msgBytes = serializeMessage(message);
            
            // 生成消息ID
            String msgId = generateMessageId(actualTopic, key, message);
            
            // 先保存消息记录
            MessageRecord messageRecord = createMessageRecord(actualTopic, msgId, message);
            messageRecordService.save(messageRecord);
            
            // 确保交换机和队列存在
            ensureExchangeAndQueueExist(actualTopic);
            
            // 创建消息属性
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setMessageId(msgId);
            
            // 设置消息Key作为路由键
            String routingKey = StringUtils.hasText(key) ? key : actualTopic;
            
            // 设置消息头
            if (headers != null) {
                headers.forEach((name, value) -> {
                    if (value != null) {
                        messageProperties.setHeader(name, value);
                    }
                });
            }
            
            // 创建消息
            Message amqpMessage = new Message(msgBytes, messageProperties);
            
            // 发送消息
            rabbitTemplate.send(actualTopic, routingKey, amqpMessage);
            log.debug("发送RabbitMQ消息成功: exchange={}, routingKey={}, messageId={}, message={}", 
                    actualTopic, routingKey, msgId, message);
            
            // 更新消息状态为已发送
            messageRecord.setStatus(MessageRecord.Status.SENT.getValue());
            messageRecord.setMsgId(msgId);
            messageRecordService.update(messageRecord);
            
            return true;
        } catch (Exception e) {
            log.error("发送RabbitMQ消息失败: topic={}, key={}, message={}", topic, key, message, e);
            return false;
        }
    }

    @Override
    public <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message, Map<String, Object> headers) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        
        // RabbitMQ的Template本身就是异步的，我们这里只是包装一下
        boolean result = send(topic, key, message, headers);
        resultFuture.complete(result);
        
        return resultFuture;
    }

    @Override
    public <T> void subscribe(String topic, String group, Class<T> messageType, Consumer<T> consumer) {
        String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
        String actualGroup = StringUtils.hasText(group) ? group : properties.getDefaultGroup();
        
        // 确保交换机和队列存在
        ensureExchangeAndQueueExist(actualTopic);
        
        // 创建消息监听容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(actualGroup);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 设置消息监听器
        container.setMessageListener((ChannelAwareMessageListener) (message, channel) -> {
            String msgId = message.getMessageProperties().getMessageId();
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            
            // 幂等性检查，防止重复消费
            if (messageConsumedService.isConsumed(msgId, actualGroup)) {
                log.debug("消息已被消费，跳过: exchange={}, msgId={}, routingKey={}, group={}", 
                        message.getMessageProperties().getReceivedExchange(), 
                        msgId, 
                        message.getMessageProperties().getReceivedRoutingKey(), 
                        actualGroup);
                // 确认消息已处理
                channel.basicAck(deliveryTag, false);
                return;
            }
            
            try {
                String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
                T typedMessage = objectMapper.readValue(msgBody, messageType);
                
                // 开始消费消息
                consumer.accept(typedMessage);
                
                // 标记消息为已消费
                messageConsumedService.markAsConsumed(msgId, actualGroup);
                
                log.debug("消费RabbitMQ消息成功: exchange={}, routingKey={}, messageId={}, message={}", 
                        message.getMessageProperties().getReceivedExchange(), 
                        message.getMessageProperties().getReceivedRoutingKey(), 
                        msgId, 
                        msgBody);
                
                // 手动确认消息
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("消费RabbitMQ消息失败: exchange={}, messageId={}", 
                        message.getMessageProperties().getReceivedExchange(), 
                        msgId, e);
                
                try {
                    // 消费失败，重新放回队列
                    channel.basicNack(deliveryTag, false, true);
                } catch (Exception ex) {
                    log.error("拒绝RabbitMQ消息失败", ex);
                }
            }
        });
        
        // 启动监听容器
        container.start();
        
        log.info("RabbitMQ消费者启动成功: exchange={}, queue={}", actualTopic, actualGroup);
    }
    
    /**
     * 确保交换机和队列存在
     */
    private void ensureExchangeAndQueueExist(String exchange) {
        // 声明主题交换机
        TopicExchange topicExchange = new TopicExchange(exchange, true, false);
        rabbitAdmin.declareExchange(topicExchange);
        
        // 声明默认队列（与交换机同名）
        Queue queue = new Queue(exchange, true);
        rabbitAdmin.declareQueue(queue);
        
        // 将队列绑定到交换机
        Binding binding = BindingBuilder.bind(queue).to(topicExchange).with("#");
        rabbitAdmin.declareBinding(binding);
    }
} 