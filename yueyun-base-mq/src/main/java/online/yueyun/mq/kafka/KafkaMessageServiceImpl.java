package online.yueyun.mq.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.model.MessageRecord;
import online.yueyun.mq.service.AbstractMessageService;
import online.yueyun.mq.service.MessageConsumedService;
import online.yueyun.mq.service.MessageRecordService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Kafka消息服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class KafkaMessageServiceImpl extends AbstractMessageService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ConcurrentMessageListenerContainer<String, byte[]> container;
    private final MqProperties properties;
    private final ObjectMapper objectMapper;

    public KafkaMessageServiceImpl(KafkaTemplate<String, byte[]> kafkaTemplate,
                                   ConcurrentMessageListenerContainer<String, byte[]> container,
                                   MqProperties properties,
                                   MessageRecordService messageRecordService,
                                   MessageConsumedService messageConsumedService) {
        super(properties, messageRecordService, messageConsumedService);
        this.kafkaTemplate = kafkaTemplate;
        this.container = container;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成Kafka特有的消息ID
     * 可以根据需求自定义ID生成逻辑
     */
    @Override
    protected <T> String generateMessageId(String topic, String key, T message) {
        // 使用默认的UUID生成逻辑，如需自定义可重写此方法
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

            // 创建Kafka消息
            ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(actualTopic, key, msgBytes);

            // 添加消息ID到头部
            producerRecord.headers().add("msgId", msgId.getBytes(StandardCharsets.UTF_8));

            // 添加其他头部
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((name, value) -> {
                    if (value != null) {
                        producerRecord.headers().add(name, value.toString().getBytes(StandardCharsets.UTF_8));
                    }
                });
            }

            // 同步发送消息
            SendResult<String, byte[]> sendResult = kafkaTemplate.send(producerRecord).get();

            log.debug("发送Kafka消息成功: topic={}, key={}, message={}, offset={}, partition={}",
                    actualTopic, key, message, sendResult.getRecordMetadata().offset(),
                    sendResult.getRecordMetadata().partition());

            // 更新消息状态为已发送
            messageRecord.setStatus(MessageRecord.Status.SENT.getValue());
            messageRecord.setMsgId(msgId);
            messageRecordService.update(messageRecord);

            return true;
        } catch (Exception e) {
            log.error("发送Kafka消息失败: topic={}, key={}, message={}", topic, key, message, e);
            return false;
        }
    }

    @Override
    public <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message, Map<String, Object> headers) {
        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();
        String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();

        try {
            byte[] msgBytes = serializeMessage(message);

            // 生成消息ID
            String msgId = generateMessageId(actualTopic, key, message);

            // 先保存消息记录
            MessageRecord messageRecord = createMessageRecord(actualTopic, msgId, message);
            messageRecordService.save(messageRecord);

            // 创建Kafka消息
            ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(actualTopic, key, msgBytes);

            // 添加消息ID到头部
            producerRecord.headers().add("msgId", msgId.getBytes(StandardCharsets.UTF_8));

            // 添加其他头部
            if (headers != null && !headers.isEmpty()) {
                headers.forEach((name, value) -> {
                    if (value != null) {
                        producerRecord.headers().add(name, value.toString().getBytes(StandardCharsets.UTF_8));
                    }
                });
            }

            // 异步发送消息
            CompletableFuture<SendResult<String, byte[]>> future = kafkaTemplate.send(producerRecord);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("异步发送Kafka消息成功: topic={}, key={}, message={}, offset={}, partition={}",
                            actualTopic, key, message, result.getRecordMetadata().offset(),
                            result.getRecordMetadata().partition());

                    // 更新消息状态为已发送
                    messageRecord.setStatus(MessageRecord.Status.SENT.getValue());
                    messageRecord.setMsgId(msgId);
                    messageRecordService.update(messageRecord);

                    resultFuture.complete(true);
                } else {
                    log.error("异步发送Kafka消息失败: topic={}, key={}, message={}",
                            actualTopic, key, message, ex);

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
            log.error("准备异步发送Kafka消息失败: topic={}, key={}, message={}",
                    actualTopic, key, message, e);
            resultFuture.complete(false);
        }

        return resultFuture;
    }

    @Override
    public <T> void subscribe(String topic, String group, Class<T> messageType, Consumer<T> consumer) {
        String actualTopic = StringUtils.hasText(topic) ? topic : properties.getDefaultTopic();
        String actualGroup = StringUtils.hasText(group) ? group : properties.getDefaultGroup();

        // 设置消费者监听器
        container.getContainerProperties().setGroupId(actualGroup);
        container.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // 注册消息监听器
        container.setupMessageListener((MessageListener<String, byte[]>) record -> {
            // 获取消息ID
            String msgId = getHeaderValue(record, "msgId");

            // 幂等性检查，防止重复消费
            if (messageConsumedService.isConsumed(msgId, actualGroup)) {
                log.debug("消息已被消费，跳过: topic={}, msgId={}, key={}, group={}",
                        record.topic(), msgId, record.key(), actualGroup);
                return;
            }

            try {
                String msgBody = new String(record.value(), StandardCharsets.UTF_8);
                T typedMessage = objectMapper.readValue(msgBody, messageType);

                // 开始消费消息
                consumer.accept(typedMessage);

                // 标记消息为已消费
                messageConsumedService.markAsConsumed(msgId, actualGroup);

                log.debug("消费Kafka消息成功: topic={}, key={}, messageId={}, message={}",
                        record.topic(), record.key(), msgId, msgBody);
            } catch (Exception e) {
                log.error("消费Kafka消息失败: topic={}, messageId={}",
                        record.topic(), msgId, e);
                // Kafka消费失败处理逻辑
            }
        });

        // 启动消费者
        if (!container.isRunning()) {
            container.start();
        }

        log.info("Kafka消费者启动成功: topic={}, group={}", actualTopic, actualGroup);
    }

    /**
     * 获取消息头的值
     */
    private String getHeaderValue(ConsumerRecord<String, byte[]> record, String headerName) {
        Header header = record.headers().lastHeader(headerName);
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * 创建Kafka特有的消息记录对象
     */
    @Override
    protected <T> MessageRecord createMessageRecord(String topic, String key, T message) throws JsonProcessingException {
        MessageRecord record = super.createMessageRecord(topic, key, message);
        // 如果需要为Kafka设置特有属性，可以在这里进行设置
        // 例如可以设置Kafka特有的重试次数
        record.setMaxRetryCount(properties.getKafka().getProducer().getRetries());
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