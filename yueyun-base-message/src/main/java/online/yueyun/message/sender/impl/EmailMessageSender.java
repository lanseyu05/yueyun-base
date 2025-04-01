package online.yueyun.message.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.dto.MessageRequest;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.sender.MessageSender;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.UrlResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import java.net.URL;
import java.util.List;

/**
 * 邮件消息发送器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageSender implements MessageSender {
    private final JavaMailSender mailSender;

    @Override
    public boolean send(MessageRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            // 设置发送人
            helper.setFrom(System.getProperty("spring.mail.username"));
            
            // 设置接收人
            helper.setTo(request.getReceivers().toArray(new String[0]));
            
            // 设置抄送人
            List<String> ccList = request.getCcList();
            if (ccList != null && !ccList.isEmpty()) {
                helper.setCc(ccList.toArray(new String[0]));
            }
            
            // 设置密送人
            List<String> bccList = request.getBccList();
            if (bccList != null && !bccList.isEmpty()) {
                helper.setBcc(bccList.toArray(new String[0]));
            }
            
            // 设置主题
            helper.setSubject(request.getTitle());
            
            // 设置内容
            helper.setText(request.getContent(), true);
            
            // 添加附件
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                for (MessageRequest.Attachment attachment : request.getAttachments()) {
                    // 使用UrlResource作为InputStreamSource
                    URL url = new URL(attachment.getUrl());
                    InputStreamSource resource = new UrlResource(url);
                    helper.addAttachment(attachment.getName(), resource, attachment.getContentType());
                }
            }
            
            // 发送邮件
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("发送邮件消息异常", e);
            return false;
        }
    }

    @Override
    public String getChannel() {
        return MessageChannelEnum.EMAIL.getCode();
    }
} 