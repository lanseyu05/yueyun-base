package online.yueyun.message.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import org.springframework.stereotype.Component;

/**
 * 微信消息发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatMessageSender implements MessageSender {
    private final WxMpService wxMpService;

    @Override
    public boolean send(MessageRequest request) {
        try {
            // 创建模板消息
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .templateId(request.getTemplateId())
                .toUser(String.join(",", request.getReceivers()))
                .build();
            
            // 设置模板数据
            if (request.getParams() != null) {
                request.getParams().forEach((key, value) -> 
                    templateMessage.addData(new WxMpTemplateData(key, value.toString()))
                );
            }
            
            // 发送消息
            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            return msgId != null;
        } catch (Exception e) {
            log.error("发送微信消息异常", e);
            return false;
        }
    }

    @Override
    public String getChannel() {
        return MessageChannelEnum.WECHAT.getCode();
    }
} 