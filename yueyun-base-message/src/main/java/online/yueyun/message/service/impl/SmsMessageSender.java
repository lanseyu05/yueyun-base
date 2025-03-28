package online.yueyun.message.service.impl;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.config.MessageProperties;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.exception.MessageException;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 短信消息发送器
 * 支持阿里云和腾讯云短信服务
 * 
 * @author yueyun
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsMessageSender extends AbstractMessageSender {

    private final MessageProperties messageProperties;
    private final ObjectMapper objectMapper;
    
    // 手机号正则表达式（简化版）
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public MessageChannelEnum getChannel() {
        return MessageChannelEnum.SMS;
    }

    @Override
    public boolean isEnabled() {
        return messageProperties.getSms().isEnabled();
    }

    @Override
    protected MessageResult doSend(Message message) throws MessageException {
        // 根据配置选择短信服务提供商
        String provider = messageProperties.getSms().getProvider();
        
        if ("aliyun".equalsIgnoreCase(provider)) {
            return sendWithAliyun(message);
        } else if ("tencent".equalsIgnoreCase(provider)) {
            return sendWithTencent(message);
        } else {
            throw new MessageException("不支持的短信服务提供商: " + provider);
        }
    }

    @Override
    protected boolean validateChannelSpecific(Message message) {
        // 短信必须有内容
        if (StringUtils.isBlank(message.getContent()) && StringUtils.isBlank(message.getTemplateId())) {
            log.warn("短信内容和模板ID不能同时为空");
            return false;
        }
        
        // 如果使用模板，必须提供模板参数
        if (StringUtils.isNotBlank(message.getTemplateId()) && (message.getTemplateParams() == null || message.getTemplateParams().isEmpty())) {
            log.warn("使用模板发送短信时，模板参数不能为空");
            return false;
        }
        
        // 验证手机号格式
        for (String receiver : message.getReceivers()) {
            if (!isValidPhoneNumber(receiver)) {
                log.warn("无效的手机号: {}", receiver);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 使用阿里云发送短信
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithAliyun(Message message) throws MessageException {
        MessageProperties.SmsConfig.AliyunSmsConfig config = messageProperties.getSms().getAliyun();
        
        // 创建DefaultAcsClient实例并初始化
        DefaultProfile profile = DefaultProfile.getProfile(
                config.getRegionId(),
                config.getAccessKeyId(),
                config.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);
        
        // 创建API请求
        CommonRequest request = new CommonRequest();
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");
        
        // 设置请求参数
        request.putQueryParameter("PhoneNumbers", String.join(",", message.getReceivers()));
        request.putQueryParameter("SignName", config.getSignName());
        
        if (StringUtils.isNotBlank(message.getTemplateId())) {
            request.putQueryParameter("TemplateCode", message.getTemplateId());
            
            try {
                // 将模板参数转换为JSON字符串
                String templateParamJson = objectMapper.writeValueAsString(message.getTemplateParams());
                request.putQueryParameter("TemplateParam", templateParamJson);
            } catch (JsonProcessingException e) {
                throw new MessageException("模板参数JSON转换失败: " + e.getMessage(), e);
            }
        } else {
            throw new MessageException("阿里云短信必须使用模板发送");
        }
        
        try {
            // 发送短信
            CommonResponse response = client.getCommonResponse(request);
            
            // 解析响应
            String data = response.getData();
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("response", data);
            
            // 判断是否发送成功（简单判断，实际中应该解析JSON）
            if (response.getHttpStatus() == 200 && data.contains("\"Code\":\"OK\"")) {
                return MessageResult.success(message.getMessageId(), resultMap);
            } else {
                return MessageResult.failure(message.getMessageId(), "阿里云短信发送失败: " + data);
            }
        } catch (ClientException e) {
            throw new MessageException("阿里云短信发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用腾讯云发送短信
     *
     * @param message 消息对象
     * @return 发送结果
     * @throws MessageException 消息异常
     */
    private MessageResult sendWithTencent(Message message) throws MessageException {
        MessageProperties.SmsConfig.TencentSmsConfig config = messageProperties.getSms().getTencent();
        
        // 创建认证对象
        Credential credential = new Credential(config.getSecretId(), config.getSecretKey());
        
        // 实例化SMS客户端
        SmsClient client = new SmsClient(credential, config.getRegion());
        
        // 实例化请求对象
        SendSmsRequest request = new SendSmsRequest();
        
        // 设置应用ID
        request.setSmsSdkAppId(config.getAppId());
        
        // 设置签名
        request.setSignName(config.getSignName());
        
        // 设置模板ID
        if (StringUtils.isNotBlank(message.getTemplateId())) {
            request.setTemplateId(message.getTemplateId());
            
            // 设置模板参数
            if (message.getTemplateParams() != null && !message.getTemplateParams().isEmpty()) {
                String[] templateParams = message.getTemplateParams().values().stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
                request.setTemplateParamSet(templateParams);
            }
        } else {
            throw new MessageException("腾讯云短信必须使用模板发送");
        }
        
        // 设置手机号
        String[] phoneNumbers = message.getReceivers().stream()
                .map(phone -> "+86" + phone) // 添加国家代码
                .toArray(String[]::new);
        request.setPhoneNumberSet(phoneNumbers);
        
        try {
            // 发送短信
            SendSmsResponse response = client.SendSms(request);
            
            // 解析响应
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("requestId", response.getRequestId());
            resultMap.put("response", response);
            
            // 检查发送状态
            boolean allSuccess = true;
            for (int i = 0; i < response.getSendStatusSet().length; i++) {
                if (!"Ok".equals(response.getSendStatusSet()[i].getCode())) {
                    allSuccess = false;
                    break;
                }
            }
            
            if (allSuccess) {
                return MessageResult.success(message.getMessageId(), resultMap);
            } else {
                return MessageResult.failure(message.getMessageId(), "部分或全部短信发送失败");
            }
        } catch (Exception e) {
            throw new MessageException("腾讯云短信发送失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证手机号格式
     *
     * @param phoneNumber 手机号
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return false;
        }
        
        return PHONE_PATTERN.matcher(phoneNumber).matches();
    }
}