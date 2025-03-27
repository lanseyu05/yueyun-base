package online.yueyun.mq.rocketmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.util.StringUtils;

import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.model.MessageRecord;
import online.yueyun.mq.service.AbstractMessageService;
import online.yueyun.mq.service.MessageConsumedService;
import online.yueyun.mq.service.MessageRecordService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * RocketMQ消息服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class RocketMqMessageServiceImpl extends AbstractMessageService {

    private final DefaultMQProducer mqProducer;
    private final DefaultMQPushConsumer mqPushConsumer;
    private final MqProperties properties;
    private final ObjectMapper objectMapper;

    public RocketMqMessageServiceImpl(DefaultMQProducer mqProducer, 
                                     DefaultMQPushConsumer mqPushConsumer, 
                                     MqProperties properties,
                                     MessageRecordService messageRecordService,
                                     MessageConsumedService messageConsumedService) {
        super(properties, messageRecordService, messageConsumedService);
        this.mqProducer = mqProducer;
        this.mqPushConsumer = mqPushConsumer;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成RocketMQ特有的消息ID
     * 如果需要特殊的ID生成逻辑，可以在此处实现
     */
    @Override
    protected <T> String generateMessageId(String topic, String key, T message) {
        // 这里使用默认的UUID生成逻辑，如需自定义可重写此方法
        return super.generateMessageId(topic, key, message);
    }

    @Override
    public <T> boolean send(String topic, String key, T message, Map<String, Object> headers) {
        try {
            String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
            byte[] msgBytes = serializeMessage(message);
            
            // 生成消息ID，使用子类可能重写的方法
            String msgId = generateMessageId(actualTopic, key, message);
            
            // 先保存消息记录
            MessageRecord messageRecord = createMessageRecord(actualTopic, msgId, message);
            messageRecordService.save(messageRecord);
            
            Message rocketMessage = new Message(actualTopic, msgBytes);
            
            // 设置消息Key
            if (StringUtils.hasText(key)) {
                rocketMessage.setKeys(key);
            }
            
            // 设置消息头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((name, value) -> {
                    if (value != null) {
                        rocketMessage.putUserProperty(name, value.toString());
                    }
                });
            }
            
            // 同步发送消息
            SendResult sendResult = mqProducer.send(rocketMessage);
            
            // 检查发送结果
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                log.debug("发送RocketMQ消息成功: topic={}, key={}, messageId={}, message={}", 
                        actualTopic, key, sendResult.getMsgId(), message);
                
                // 更新消息状态为已发送
                messageRecord.setStatus(MessageRecord.Status.SENT.getValue());
                messageRecord.setMsgId(sendResult.getMsgId());
                messageRecordService.update(messageRecord);
                
                return true;
            } else {
                log.error("发送RocketMQ消息失败: topic={}, key={}, message={}, status={}", 
                        actualTopic, key, message, sendResult.getSendStatus());
                
                // 更新消息状态为发送失败
                messageRecord.setStatus(MessageRecord.Status.SEND_FAILED.getValue());
                messageRecord.setRetryCount(messageRecord.getRetryCount() + 1);
                // 设置下次重试时间，每次重试间隔翻倍，最大间隔1小时
                int delayMinutes = Math.min(60, (int) Math.pow(2, messageRecord.getRetryCount()));
                messageRecord.setNextRetryTime(LocalDateTime.now().plusMinutes(delayMinutes));
                messageRecordService.update(messageRecord);
                
                return false;
            }
        } catch (Exception e) {
            log.error("发送RocketMQ消息失败: topic={}, key={}, message={}", topic, key, message, e);
            return false;
        }
    }

    @Override
    public <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message, Map<String, Object> headers) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
        
        try {
            byte[] msgBytes = serializeMessage(message);
            
            // 生成消息ID，使用子类可能重写的方法
            String msgId = generateMessageId(actualTopic, key, message);
            
            // 先保存消息记录
            MessageRecord messageRecord = createMessageRecord(actualTopic, msgId, message);
            messageRecordService.save(messageRecord);
            
            Message rocketMessage = new Message(actualTopic, msgBytes);
            
            // 设置消息Key
            if (StringUtils.hasText(key)) {
                rocketMessage.setKeys(key);
            }
            
            // 设置消息头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((name, value) -> {
                    if (value != null) {
                        rocketMessage.putUserProperty(name, value.toString());
                    }
                });
            }
            
            mqProducer.send(rocketMessage, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.debug("异步发送RocketMQ消息成功: topic={}, key={}, messageId={}, message={}", 
                            actualTopic, key, sendResult.getMsgId(), message);
                    
                    // 更新消息状态为已发送
                    messageRecord.setStatus(MessageRecord.Status.SENT.getValue());
                    messageRecord.setMsgId(sendResult.getMsgId());
                    messageRecordService.update(messageRecord);
                    
                    resultFuture.complete(true);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("异步发送RocketMQ消息失败: topic={}, key={}, message={}", 
                            actualTopic, key, message, e);
                    
                    // 更新消息状态为发送失败
                    messageRecord.setStatus(MessageRecord.Status.SEND_FAILED.getValue());
                    messageRecord.setRetryCount(messageRecord.getRetryCount() + 1);
                    // 设置下次重试时间，每次重试间隔翻倍，最大间隔1小时
                    int delayMinutes = Math.min(60, (int) Math.pow(2, messageRecord.getRetryCount()));
                    messageRecord.setNextRetryTime(LocalDateTime.now().plusMinutes(delayMinutes));
                    messageRecordService.update(messageRecord);
                    
                    resultFuture.complete(false);
                }
            });
        } catch (Exception e) {
            log.error("准备异步发送RocketMQ消息失败: topic={}, key={}, message={}", 
                    actualTopic, key, message, e);
            resultFuture.complete(false);
        }
        
        return resultFuture;
    }

    @Override
    public <T> void subscribe(String topic, String group, Class<T> messageType, Consumer<T> consumer) {
        String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
        String actualGroup = StringUtils.hasText(group) ? group : properties.getDefaultGroup();
        
        try {
            // 设置消费者组
            mqPushConsumer.setConsumerGroup(actualGroup);
            
            // 订阅主题
            mqPushConsumer.subscribe(actualTopic, "*");
            
            // 注册消息监听器
            mqPushConsumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt msg : msgs) {
                    // 幂等性检查，防止重复消费
                    if (messageConsumedService.isConsumed(msg.getMsgId(), actualGroup)) {
                        log.debug("消息已被消费，跳过: topic={}, msgId={}, key={}, group={}", 
                                msg.getTopic(), msg.getMsgId(), msg.getKeys(), actualGroup);
                        continue;
                    }
                    
                    try {
                        String msgBody = new String(msg.getBody(), StandardCharsets.UTF_8);
                        T typedMessage = objectMapper.readValue(msgBody, messageType);
                        
                        // 开始消费消息
                        consumer.accept(typedMessage);
                        
                        // 标记消息为已消费
                        messageConsumedService.markAsConsumed(msg.getMsgId(), actualGroup);
                        
                        log.debug("消费RocketMQ消息成功: topic={}, key={}, messageId={}, message={}", 
                                msg.getTopic(), msg.getKeys(), msg.getMsgId(), msgBody);
                    } catch (Exception e) {
                        log.error("消费RocketMQ消息失败: topic={}, messageId={}", 
                                msg.getTopic(), msg.getMsgId(), e);
                        
                        // 记录消费失败，但不重复消费（通过消息重试策略来处理）
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
            
            // 启动消费者
            mqPushConsumer.start();
            log.info("RocketMQ消费者启动成功: topic={}, group={}", actualTopic, actualGroup);
        } catch (MQClientException e) {
            log.error("订阅RocketMQ主题失败: topic={}, group={}", actualTopic, actualGroup, e);
            throw new RuntimeException("订阅RocketMQ主题失败", e);
        }
    }
    
    /**
     * 创建RocketMQ特有的消息记录对象
     */
    @Override
    protected <T> MessageRecord createMessageRecord(String topic, String key, T message) throws JsonProcessingException {
        MessageRecord record = super.createMessageRecord(topic, key, message);
        // 如果需要为RocketMQ设置特有属性，可以在这里进行设置
        record.setMaxRetryCount(properties.getRocketMq().getMaxRetryCount());
        return record;
    }

    /**
     * 序列化消息对象为字节数组
     */
    protected byte[] serializeMessage(Object message) throws JsonProcessingException {
        if (message instanceof String) {
            return ((String) message).getBytes(StandardCharsets.UTF_8);
        } else if (message instanceof byte[]) {
            return (byte[]) message;
        } else {
            return objectMapper.writeValueAsBytes(message);
        }
    }
} 