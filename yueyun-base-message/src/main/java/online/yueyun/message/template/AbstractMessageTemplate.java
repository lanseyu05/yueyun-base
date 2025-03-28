package online.yueyun.message.template;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;
import online.yueyun.message.model.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象消息模板
 * 提供基础模板实现，子类只需要实现特定的方法
 *
 * @author yueyun
 */
@Slf4j
public abstract class AbstractMessageTemplate implements MessageTemplate {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @Getter
    private final String templateId;

    @Getter
    private final String templateName;

    @Getter
    private final String templateDescription;

    @Getter
    private final String defaultTitle;

    @Getter
    private final List<String> requiredParams;

    @Getter
    private final String templateContent;

    @Getter
    private final MessageTypeEnum messageType;

    @Getter
    private final MessageChannelEnum defaultChannel;

    /**
     * 构造函数
     *
     * @param templateId          模板ID
     * @param templateName        模板名称
     * @param templateDescription 模板描述
     * @param defaultTitle        默认标题
     * @param requiredParams      必需的参数列表
     * @param templateContent     模板内容
     * @param messageType         消息类型
     * @param defaultChannel      默认发送渠道
     */
    protected AbstractMessageTemplate(
            String templateId,
            String templateName,
            String templateDescription,
            String defaultTitle,
            List<String> requiredParams,
            String templateContent,
            MessageTypeEnum messageType,
            MessageChannelEnum defaultChannel) {

        this.templateId = templateId;
        this.templateName = templateName;
        this.templateDescription = templateDescription;
        this.defaultTitle = defaultTitle;
        this.requiredParams = requiredParams != null ? requiredParams : new ArrayList<>();
        this.templateContent = templateContent;
        this.messageType = messageType != null ? messageType : MessageTypeEnum.NOTIFICATION;
        this.defaultChannel = defaultChannel != null ? defaultChannel : MessageChannelEnum.EMAIL;
    }

    @Override
    public String render(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        // 验证必填参数
        if (!validateParams(params)) {
            throw new IllegalArgumentException("缺少必要的模板参数，模板：" + templateId);
        }

        try {
            // 使用Spring EL表达式解析模板
            EvaluationContext context = new StandardEvaluationContext();

            // 将参数加入上下文
            params.forEach(context::setVariable);

            // 解析模板
            Expression expression = EXPRESSION_PARSER.parseExpression(templateContent);
            return expression.getValue(context, String.class);
        } catch (Exception e) {
            log.error("渲染模板失败，模板ID：{}，错误：{}", templateId, e.getMessage(), e);
            throw new RuntimeException("渲染模板失败，模板ID：" + templateId, e);
        }
    }

    @Override
    public Message createMessage(Map<String, Object> params, List<String> receivers) {
        if (params == null) {
            params = new HashMap<>();
        }

        Message message = new Message();
        message.setTitle(getDefaultTitle());
        message.setContent(render(params));
        message.setType(getMessageType());
        message.setReceivers(receivers);
        message.setChannel(getDefaultChannel());
        message.setTemplateId(getTemplateId());
        message.setExtraParams(params);

        return message;
    }

    @Override
    public boolean validateParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return requiredParams.isEmpty();
        }

        for (String param : requiredParams) {
            if (!params.containsKey(param) || params.get(param) == null ||
                    (params.get(param) instanceof String && StringUtils.isBlank((String) params.get(param)))) {
                log.warn("参数验证失败，缺少必需参数：{}，模板ID：{}", param, templateId);
                return false;
            }
        }

        return true;
    }

    /**
     * 子类可以覆盖此方法来提供更复杂的模板渲染逻辑
     *
     * @param params 参数列表
     * @return 渲染后的内容
     */
    protected String doRender(Map<String, Object> params) {
        return render(params);
    }
} 