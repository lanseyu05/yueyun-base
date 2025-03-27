package online.yueyun.mq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mq.config.MqProperties;
import online.yueyun.mq.model.MessageRecord;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 消息服务抽象基类，提供通用功能和模板方法
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMessageService implements MessageService {

    protected final MqProperties properties;
    protected final ObjectMapper objectMapper;
    protected final MessageRecordService messageRecordService;
    protected final MessageConsumedService messageConsumedService;

    public AbstractMessageService(MqProperties properties,
                              MessageRecordService messageRecordService,
                              MessageConsumedService messageConsumedService) {
        this.properties = properties;
        this.messageRecordService = messageRecordService;
        this.messageConsumedService = messageConsumedService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public <T> boolean send(String topic, T message) {
        return send(topic, null, message, null);
    }

    @Override
    public <T> boolean send(String topic, String key, T message) {
        return send(topic, key, message, null);
    }

    @Override
    public <T> CompletableFuture<Boolean> sendAsync(String topic, T message) {
        return sendAsync(topic, null, message, null);
    }

    @Override
    public <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message) {
        return sendAsync(topic, key, message, null);
    }

    /**
     * 生成消息ID
     * 子类可以覆盖此方法提供自定义的消息ID生成逻辑
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 消息ID
     */
    protected <T> String generateMessageId(String topic, String key, T message) {
        return StringUtils.hasText(key) ? key : UUID.randomUUID().toString();
    }

    /**
     * 序列化消息对象为字节数组
     *
     * @param message 消息对象
     * @return 序列化后的字节数组
     * @throws JsonProcessingException JSON处理异常
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

    /**
     * 创建消息记录对象
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 消息记录对象
     * @throws JsonProcessingException JSON处理异常
     */
    protected <T> MessageRecord createMessageRecord(String topic, String key, T message) throws JsonProcessingException {
        String content = message instanceof String ? (String) message : objectMapper.writeValueAsString(message);
        return MessageRecord.builder()
                .businessKey(key)
                .topic(topic)
                .content(content)
                .status(MessageRecord.Status.PENDING.getValue())
                .retryCount(0)
                .maxRetryCount(properties.getMaxRetryCount())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
} 