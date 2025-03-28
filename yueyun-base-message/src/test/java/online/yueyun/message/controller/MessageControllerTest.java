package online.yueyun.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.enums.MessageTypeEnum;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import online.yueyun.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 消息控制器测试类
 *
 * @author yueyun
 */
@WebMvcTest(MessageController.class)
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    /**
     * 测试发送消息接口
     */
    @Test
    public void testSend() throws Exception {
        // 准备测试数据
        Message message = createTestMessage();
        MessageResult expectedResult = createSuccessResult();

        // 模拟服务调用
        when(messageService.send(any(Message.class))).thenReturn(expectedResult);

        // 执行请求并验证
        mockMvc.perform(post("/message/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(expectedResult.getMessageId()));
    }

    /**
     * 测试异步发送消息接口
     */
    @Test
    public void testSendAsync() throws Exception {
        // 准备测试数据
        Message message = createTestMessage();
        String messageId = "async-msg-" + UUID.randomUUID().toString();

        // 模拟服务调用
        when(messageService.sendAsync(any(Message.class))).thenReturn(messageId);

        // 执行请求并验证
        mockMvc.perform(post("/message/send/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.messageId").value(messageId))
                .andExpect(jsonPath("$.status").value("accepted"));
    }

    /**
     * 测试使用指定渠道发送消息接口
     */
    @Test
    public void testSendWithChannel() throws Exception {
        // 准备测试数据
        Message message = createTestMessage();
        MessageResult expectedResult = createSuccessResult();
        String channelCode = MessageChannelEnum.EMAIL.getCode();

        // 模拟服务调用
        when(messageService.sendWithChannel(any(Message.class), any(MessageChannelEnum.class)))
                .thenReturn(expectedResult);

        // 执行请求并验证
        mockMvc.perform(post("/message/send/channel/" + channelCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value(expectedResult.getMessageId()));
    }

    /**
     * 测试使用模板发送消息接口
     */
    @Test
    public void testSendWithTemplate() throws Exception {
        // 准备测试数据
        String templateId = "test-template-001";
        Map<String, Object> params = new HashMap<>();
        params.put("username", "测试用户");
        params.put("code", "123456");
        List<String> receivers = Arrays.asList("user1@example.com", "user2@example.com");
        MessageResult expectedResult = createSuccessResult();

        // 模拟服务调用
        when(messageService.sendWithTemplate(anyString(), anyMap(), anyList()))
                .thenReturn(expectedResult);

        // 执行请求并验证
        mockMvc.perform(post("/message/send/template")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("templateId", templateId)
                        .param("params", objectMapper.writeValueAsString(params))
                        .param("receivers", String.join(",", receivers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 测试批量发送消息接口
     */
    @Test
    public void testBatchSend() throws Exception {
        // 准备测试数据
        List<Message> messages = new ArrayList<>();
        messages.add(createTestMessage());
        messages.add(createTestMessage());

        List<MessageResult> expectedResults = new ArrayList<>();
        expectedResults.add(createSuccessResult());
        expectedResults.add(createSuccessResult());

        // 模拟服务调用
        when(messageService.batchSend(anyList())).thenReturn(expectedResults);

        // 执行请求并验证
        mockMvc.perform(post("/message/send/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messages)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[1].success").value(true));
    }

    /**
     * 测试获取支持的渠道列表接口
     */
    @Test
    public void testGetChannels() throws Exception {
        mockMvc.perform(get("/message/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.EMAIL.code").value(MessageChannelEnum.EMAIL.getCode()))
                .andExpect(jsonPath("$.SMS.code").value(MessageChannelEnum.SMS.getCode()))
                .andExpect(jsonPath("$.FEISHU.code").value(MessageChannelEnum.FEISHU.getCode()))
                .andExpect(jsonPath("$.DINGTALK.code").value(MessageChannelEnum.DINGTALK.getCode()));
    }

    /**
     * 创建测试消息对象
     */
    private Message createTestMessage() {
        Message message = new Message();
        message.setTitle("测试消息");
        message.setContent("这是一条测试消息内容");
        message.setType(MessageTypeEnum.NOTIFICATION);
        message.setSender("system");
        message.setReceivers(Arrays.asList("user1@example.com", "user2@example.com"));
        message.setChannel(MessageChannelEnum.EMAIL);
        message.setTemplateId("tpl-001");
        message.setExtraParams(new HashMap<String, Object>() {{
            put("username", "测试用户");
            put("time", new Date());
        }});
        return message;
    }

    /**
     * 创建成功的发送结果对象
     */
    private MessageResult createSuccessResult() {
        MessageResult result = new MessageResult();
        result.setSuccess(true);
        result.setMessageId("msg-" + UUID.randomUUID().toString().substring(0, 8));
        result.setSendTime(new Date());
        return result;
    }
} 