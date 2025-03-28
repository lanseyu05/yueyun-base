package online.yueyun.message.service.impl;

import com.larksuite.oapi.service.im.v1.ImService;
import com.larksuite.oapi.service.im.v1.model.CreateMessageReq;
import com.larksuite.oapi.service.im.v1.model.CreateMessageResp;
import com.larksuite.oapi.service.im.v1.model.MessageContent;
import com.larksuite.oapi.core.AppSettings;
import com.larksuite.oapi.core.Config;
import com.larksuite.oapi.core.Domain;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.MessageProperties;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 飞书消息发送器
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeishuMessageSender extends AbstractMessageSender {

    private final MessageProperties messageProperties;
    
    @Override
    public MessageChannelEnum getChannel() {
        return MessageChannelEnum.FEISHU;
    }

    @Override
    public boolean isEnabled() {
        return messageProperties.getFeishu().isEnabled();
    }

    @Override
    protected MessageResult doSend(Message message) throws MessageException {
        // 判断是否使用Webhook方式发送
        if (messageProperties.getFeishu().isWebhookEnabled() && !CollectionUtils.isEmpty(messageProperties.getFeishu().getWebhookUrls())) {
            return sendWithWebhook(message);
        } else {
            return sendWithSdk(message);
        }
    }

    @Override
    protected boolean validateChannelSpecific(Message message) {
        // 飞书消息必须有内容
        if (StringUtils.isBlank(message.getContent())) {
            log.warn("飞书消息内容不能为空");
            return false;
        }
        
        // 使用SDK方式必须有接收人ID
        if (!messageProperties.getFeishu().isWebhookEnabled()) {
            for (String receiver : message.getReceivers()) {
                if (!isValidFeishuUserId(receiver)) {
                    log.warn("无效的飞书用户ID: {}", receiver);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 使用SDK方式发送飞书消息
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithSdk(Message message) throws MessageException {
        MessageProperties.FeishuConfig config = messageProperties.getFeishu();
        
        // 创建飞书应用配置
        AppSettings appSettings = new AppSettings();
        appSettings.setAppID(config.getAppId());
        appSettings.setAppSecret(config.getAppSecret());
        
        // 创建配置
        Config larkConfig = new Config(Domain.FeiShu, appSettings);
        
        // 创建IM服务
        ImService imService = new ImService(larkConfig);
        
        try {
            // 为每个接收者发送消息
            Map<String, Object> resultMap = new HashMap<>();
            boolean allSuccess = true;
            
            for (String receiver : message.getReceivers()) {
                // 创建消息请求
                CreateMessageReq req = new CreateMessageReq();
                req.setReceiveID(receiver);
                req.setMsgType("text");
                
                // 设置消息内容
                MessageContent content = new MessageContent();
                
                if (StringUtils.isNotBlank(message.getTitle())) {
                    content.setText(message.getTitle() + "\n\n" + message.getContent());
                } else {
                    content.setText(message.getContent());
                }
                
                req.setContent(content);
                
                // 发送消息
                CreateMessageResp resp = imService.message().create(req);
                
                if (resp != null && resp.getCode() == 0) {
                    resultMap.put("receiver_" + receiver, "success");
                } else {
                    resultMap.put("receiver_" + receiver, "failed: " + (resp != null ? resp.getMsg() : "无响应"));
                    allSuccess = false;
                }
            }
            
            if (allSuccess) {
                return MessageResult.success(message.getMessageId(), resultMap);
            } else {
                return MessageResult.failure(message.getMessageId(), "部分或全部消息发送失败");
            }
        } catch (Exception e) {
            log.error("飞书消息发送失败", e);
            throw new MessageException("飞书消息发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用Webhook方式发送飞书消息
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithWebhook(Message message) throws MessageException {
        MessageProperties.FeishuConfig config = messageProperties.getFeishu();
        
        if (CollectionUtils.isEmpty(config.getWebhookUrls())) {
            throw new MessageException("飞书Webhook地址未配置");
        }
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Map<String, Object> resultMap = new HashMap<>();
            boolean allSuccess = true;
            
            for (String webhookUrl : config.getWebhookUrls()) {
                HttpPost httpPost = new HttpPost(webhookUrl);
                httpPost.setHeader("Content-Type", "application/json");
                
                // 构建消息内容
                String content = message.getContent().replace("\n", "\\n");
                String title = StringUtils.isNotBlank(message.getTitle()) ? message.getTitle() : "通知消息";
                
                String json = "{\"msg_type\":\"text\",\"content\":{\"text\":\"" + title + "\\n\\n" + content + "\"}}";
                
                // 设置请求体
                StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
                httpPost.setEntity(entity);
                
                // 发送请求
                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getCode();
                    if (statusCode == 200) {
                        resultMap.put("webhook", webhookUrl);
                    } else {
                        resultMap.put("webhook_error", "HTTP 状态码: " + statusCode);
                        allSuccess = false;
                    }
                }
            }
            
            if (allSuccess) {
                return MessageResult.success(message.getMessageId(), resultMap);
            } else {
                return MessageResult.failure(message.getMessageId(), "飞书Webhook消息发送失败");
            }
        } catch (IOException e) {
            log.error("飞书Webhook消息发送失败", e);
            throw new MessageException("飞书Webhook消息发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证飞书用户ID格式
     *
     * @param userId 用户ID
     * @return 是否有效
     */
    private boolean isValidFeishuUserId(String userId) {
        // 这里简单判断非空，实际中可能需要更复杂的验证
        return StringUtils.isNotBlank(userId);
    }
} 