package online.yueyun.message.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.MessageProperties;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 钉钉消息发送器
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DingtalkMessageSender extends AbstractMessageSender {

    private final MessageProperties messageProperties;
    
    // 访问令牌缓存
    private String accessToken;
    private long tokenExpireTime;
    
    @Override
    public MessageChannelEnum getChannel() {
        return MessageChannelEnum.DINGTALK;
    }

    @Override
    public boolean isEnabled() {
        return messageProperties.getDingtalk().isEnabled();
    }

    @Override
    protected MessageResult doSend(Message message) throws MessageException {
        // 判断是否使用Webhook方式发送
        if (messageProperties.getDingtalk().isWebhookEnabled() && !CollectionUtils.isEmpty(messageProperties.getDingtalk().getWebhookUrls())) {
            return sendWithWebhook(message);
        } else {
            return sendWithSdk(message);
        }
    }

    @Override
    protected boolean validateChannelSpecific(Message message) {
        // 钉钉消息必须有内容
        if (StringUtils.isBlank(message.getContent())) {
            log.warn("钉钉消息内容不能为空");
            return false;
        }
        
        // 使用SDK方式必须有接收人ID
        if (!messageProperties.getDingtalk().isWebhookEnabled()) {
            for (String receiver : message.getReceivers()) {
                if (!isValidDingtalkUserId(receiver)) {
                    log.warn("无效的钉钉用户ID: {}", receiver);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 使用SDK方式发送钉钉消息
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithSdk(Message message) throws MessageException {
        try {
            // 获取访问令牌
            String token = getAccessToken();
            
            // 创建钉钉客户端
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
            
            // 创建请求
            OapiMessageCorpconversationAsyncsendV2Request req = new OapiMessageCorpconversationAsyncsendV2Request();
            req.setAgentId(Long.valueOf(messageProperties.getDingtalk().getAppKey()));
            
            // 设置接收人
            req.setUseridList(String.join(",", message.getReceivers()));
            
            // 构建消息内容
            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
            msg.setMsgtype("text");
            
            OapiMessageCorpconversationAsyncsendV2Request.Text text = new OapiMessageCorpconversationAsyncsendV2Request.Text();
            
            if (StringUtils.isNotBlank(message.getTitle())) {
                text.setContent(message.getTitle() + "\n\n" + message.getContent());
            } else {
                text.setContent(message.getContent());
            }
            
            msg.setText(text);
            req.setMsg(msg);
            
            // 发送消息
            OapiMessageCorpconversationAsyncsendV2Response response = client.execute(req, token);
            
            // 处理结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("response", response);
            
            if (response.isSuccess()) {
                return MessageResult.success(message.getMessageId(), resultMap);
            } else {
                return MessageResult.failure(message.getMessageId(), "钉钉消息发送失败: " + response.getErrmsg());
            }
        } catch (ApiException e) {
            log.error("钉钉消息发送失败", e);
            throw new MessageException("钉钉消息发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用Webhook方式发送钉钉消息
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithWebhook(Message message) throws MessageException {
        MessageProperties.DingtalkConfig config = messageProperties.getDingtalk();
        
        if (CollectionUtils.isEmpty(config.getWebhookUrls())) {
            throw new MessageException("钉钉Webhook地址未配置");
        }
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            Map<String, Object> resultMap = new HashMap<>();
            boolean allSuccess = true;
            
            for (String webhookUrl : config.getWebhookUrls()) {
                // 判断是否需要签名
                String finalUrl = webhookUrl;
                if (StringUtils.isNotBlank(config.getSignSecret())) {
                    finalUrl = addSignature(webhookUrl, config.getSignSecret());
                }
                
                HttpPost httpPost = new HttpPost(finalUrl);
                httpPost.setHeader("Content-Type", "application/json");
                
                // 构建消息内容
                String content = message.getContent();
                String title = StringUtils.isNotBlank(message.getTitle()) ? message.getTitle() : "通知消息";
                
                String json = "{\"msgtype\":\"text\",\"text\":{\"content\":\"" + title + "\\n\\n" + content + "\"}}";
                
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
                return MessageResult.failure(message.getMessageId(), "钉钉Webhook消息发送失败");
            }
        } catch (Exception e) {
            log.error("钉钉Webhook消息发送失败", e);
            throw new MessageException("钉钉Webhook消息发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取钉钉访问令牌
     *
     * @return 访问令牌
     * @throws ApiException API异常
     */
    private String getAccessToken() throws ApiException {
        long now = System.currentTimeMillis();
        
        // 检查令牌是否有效
        if (StringUtils.isNotBlank(accessToken) && now < tokenExpireTime) {
            return accessToken;
        }
        
        // 获取新令牌
        MessageProperties.DingtalkConfig config = messageProperties.getDingtalk();
        
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(config.getAppKey());
        request.setAppsecret(config.getAppSecret());
        request.setHttpMethod("GET");
        
        OapiGettokenResponse response = client.execute(request);
        
        if (response.isSuccess()) {
            accessToken = response.getAccessToken();
            tokenExpireTime = now + (response.getExpiresIn() * 1000L - 60000L); // 提前1分钟过期
            return accessToken;
        } else {
            throw new ApiException("获取钉钉访问令牌失败: " + response.getErrmsg());
        }
    }
    
    /**
     * 为钉钉Webhook添加签名
     *
     * @param url 原始URL
     * @param secret 密钥
     * @return 带签名的URL
     * @throws Exception 异常
     */
    private String addSignature(String url, String secret) throws Exception {
        long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String sign = URLEncoder.encode(Base64.encodeBase64String(signData), "UTF-8");
        
        return url + (url.contains("?") ? "&" : "?") + 
                "timestamp=" + timestamp + "&sign=" + sign;
    }
    
    /**
     * 验证钉钉用户ID格式
     *
     * @param userId 用户ID
     * @return 是否有效
     */
    private boolean isValidDingtalkUserId(String userId) {
        // 这里简单判断非空，实际中可能需要更复杂的验证
        return StringUtils.isNotBlank(userId);
    }
}