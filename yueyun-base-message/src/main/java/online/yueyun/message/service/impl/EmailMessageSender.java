package online.yueyun.message.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.MessageProperties;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 邮件消息发送器
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailMessageSender extends AbstractMessageSender {

    private final JavaMailSender mailSender;
    private final MessageProperties messageProperties;

    @Override
    public MessageChannelEnum getChannel() {
        return MessageChannelEnum.EMAIL;
    }

    @Override
    public boolean isEnabled() {
        return messageProperties.getEmail().isEnabled();
    }

    @Override
    protected MessageResult doSend(Message message) throws MessageException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            // 设置发件人
            String from = StringUtils.isNotBlank(message.getSender()) ? 
                    message.getSender() : messageProperties.getEmail().getDefaultFrom();
            helper.setFrom(from);

            // 设置收件人
            helper.setTo(message.getReceivers().toArray(new String[0]));

            // 设置抄送人
            if (!CollectionUtils.isEmpty(message.getCcList())) {
                helper.setCc(message.getCcList().toArray(new String[0]));
            }

            // 设置密送人
            if (!CollectionUtils.isEmpty(message.getBccList())) {
                helper.setBcc(message.getBccList().toArray(new String[0]));
            }

            // 设置主题
            helper.setSubject(message.getTitle());

            // 设置正文，支持HTML
            helper.setText(message.getContent(), true);

            // 添加附件
            if (!CollectionUtils.isEmpty(message.getAttachments())) {
                for (Message.Attachment attachment : message.getAttachments()) {
                    if (StringUtils.isNotBlank(attachment.getPath())) {
                        File file = new File(attachment.getPath());
                        if (file.exists() && file.isFile()) {
                            FileSystemResource resource = new FileSystemResource(file);
                            helper.addAttachment(attachment.getName(), resource);
                        } else {
                            log.warn("附件文件不存在: {}", attachment.getPath());
                        }
                    }
                }
            }

            // 发送邮件
            mailSender.send(mimeMessage);
            
            // 返回成功结果
            Map<String, Object> resultInfo = new HashMap<>(2);
            resultInfo.put("from", from);
            resultInfo.put("to", message.getReceivers());
            
            return MessageResult.success(message.getMessageId(), resultInfo);
        } catch (MessagingException e) {
            log.error("发送邮件失败", e);
            throw new MessageException("发送邮件失败: " + e.getMessage(), e);
        }
    }

    @Override
    protected boolean validateChannelSpecific(Message message) {
        // 邮件必须有标题
        if (StringUtils.isBlank(message.getTitle())) {
            log.warn("邮件标题不能为空");
            return false;
        }
        
        // 邮件必须有内容
        if (StringUtils.isBlank(message.getContent())) {
            log.warn("邮件内容不能为空");
            return false;
        }
        
        // 验证接收人邮箱格式
        for (String receiver : message.getReceivers()) {
            if (!isValidEmail(receiver)) {
                log.warn("无效的邮箱地址: {}", receiver);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 验证邮箱地址格式
     *
     * @param email 邮箱地址
     * @return 是否有效
     */
    private boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        
        // 简单的邮箱格式验证，包含@和.
        return email.contains("@") && email.contains(".");
    }
} 