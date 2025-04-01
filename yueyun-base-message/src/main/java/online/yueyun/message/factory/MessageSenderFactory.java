package online.yueyun.message.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息发送器工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSenderFactory {
    private final List<MessageSender> messageSenders;
    private final Map<String, MessageSender> senderMap = new HashMap<>();

    /**
     * 初始化方法，加载所有消息发送器
     */
    @PostConstruct
    public void init() {
        messageSenders.forEach(sender -> {
            String channel = sender.getChannel();
            senderMap.put(channel, sender);
            log.info("注册消息发送器: {}", channel);
        });
    }

    /**
     * 获取消息发送器
     *
     * @param channel 消息渠道
     * @return 消息发送器
     */
    public MessageSender getMessageSender(String channel) {
        MessageSender sender = senderMap.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("不支持的消息渠道: " + channel);
        }
        return sender;
    }

    /**
     * 获取消息发送器
     *
     * @param channelEnum 消息渠道枚举
     * @return 消息发送器
     */
    public MessageSender getMessageSender(MessageChannelEnum channelEnum) {
        return getMessageSender(channelEnum.getCode());
    }
} 