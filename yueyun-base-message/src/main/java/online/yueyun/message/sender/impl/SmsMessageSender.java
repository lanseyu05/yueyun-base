package online.yueyun.message.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.SmsProperties;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import online.yueyun.message.sender.sms.SmsProviderFactory;
import org.springframework.stereotype.Component;

/**
 * 短信消息发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsMessageSender implements MessageSender {

    private final SmsProperties smsProperties;
    private final SmsProviderFactory smsProviderFactory;

    @Override
    public boolean send(MessageRequest request) {
        if (!smsProperties.isEnabled()) {
            log.warn("短信发送功能未启用");
            return false;
        }

        try {
            log.info("发送短信消息，接收人: {}, 内容: {}",
                    String.join(",", request.getReceivers()),
                    request.getContent());

            // 使用默认的短信服务提供商
            return smsProviderFactory.getDefaultProvider().send(request);
        } catch (Exception e) {
            log.error("发送短信消息异常", e);
            return false;
        }
    }

    @Override
    public String getChannel() {
        return MessageChannelEnum.SMS.getCode();
    }
}