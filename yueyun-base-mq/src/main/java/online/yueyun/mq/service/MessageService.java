package online.yueyun.mq.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 消息服务接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface MessageService {
    
    /**
     * 同步发送消息
     *
     * @param topic 主题
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 发送结果，成功返回true，失败返回false
     */
    <T> boolean send(String topic, T message);
    
    /**
     * 同步发送消息
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 发送结果，成功返回true，失败返回false
     */
    <T> boolean send(String topic, String key, T message);
    
    /**
     * 同步发送消息
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param headers 消息头
     * @param <T> 消息类型
     * @return 发送结果，成功返回true，失败返回false
     */
    <T> boolean send(String topic, String key, T message, Map<String, Object> headers);
    
    /**
     * 异步发送消息
     *
     * @param topic 主题
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 异步发送结果
     */
    <T> CompletableFuture<Boolean> sendAsync(String topic, T message);
    
    /**
     * 异步发送消息
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param <T> 消息类型
     * @return 异步发送结果
     */
    <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message);
    
    /**
     * 异步发送消息
     *
     * @param topic 主题
     * @param key 消息键
     * @param message 消息内容
     * @param headers 消息头
     * @param <T> 消息类型
     * @return 异步发送结果
     */
    <T> CompletableFuture<Boolean> sendAsync(String topic, String key, T message, Map<String, Object> headers);
    
    /**
     * 订阅消息
     *
     * @param topic 主题
     * @param group 消费组
     * @param clazz 消息类型
     * @param consumer 消息消费者
     * @param <T> 消息类型
     */
    <T> void subscribe(String topic, String group, Class<T> clazz, Consumer<T> consumer);
} 