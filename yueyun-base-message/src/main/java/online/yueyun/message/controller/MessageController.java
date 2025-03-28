package online.yueyun.message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.message.enums.MessageChannelEnum;
import online.yueyun.message.model.Message;
import online.yueyun.message.model.MessageResult;
import online.yueyun.message.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息服务控制器
 * 
 * @author yueyun
 */
@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    
    /**
     * 发送消息
     *
     * @param message 消息对象
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResult> send(@RequestBody Message message) {
        return ResponseEntity.ok(messageService.send(message));
    }
    
    /**
     * 异步发送消息
     *
     * @param message 消息对象
     * @return 消息ID
     */
    @PostMapping("/send/async")
    public ResponseEntity<Map<String, Object>> sendAsync(@RequestBody Message message) {
        String messageId = messageService.sendAsync(message);
        
        Map<String, Object> result = new HashMap<>(2);
        result.put("messageId", messageId);
        result.put("status", "accepted");
        
        return ResponseEntity.accepted().body(result);
    }
    
    /**
     * 使用指定渠道发送消息
     *
     * @param message 消息对象
     * @param channel 渠道编码
     * @return 发送结果
     */
    @PostMapping("/send/channel/{channel}")
    public ResponseEntity<MessageResult> sendWithChannel(
            @RequestBody Message message, 
            @PathVariable String channel) {
        
        MessageChannelEnum channelEnum = MessageChannelEnum.getByCode(channel);
        return ResponseEntity.ok(messageService.sendWithChannel(message, channelEnum));
    }
    
    /**
     * 使用模板发送消息
     *
     * @param templateId 模板ID
     * @param params 模板参数
     * @param receivers 接收人列表
     * @return 发送结果
     */
    @PostMapping("/send/template")
    public ResponseEntity<MessageResult> sendWithTemplate(
            @RequestParam String templateId, 
            @RequestParam Map<String, Object> params, 
            @RequestParam List<String> receivers) {
        
        return ResponseEntity.ok(messageService.sendWithTemplate(templateId, params, receivers));
    }
    
    /**
     * 使用模板通过指定渠道发送消息
     *
     * @param templateId 模板ID
     * @param params 模板参数
     * @param receivers 接收人列表
     * @param channel 渠道编码
     * @return 发送结果
     */
    @PostMapping("/send/template/channel/{channel}")
    public ResponseEntity<MessageResult> sendWithTemplateAndChannel(
            @RequestParam String templateId, 
            @RequestParam Map<String, Object> params, 
            @RequestParam List<String> receivers, 
            @PathVariable String channel) {
        
        MessageChannelEnum channelEnum = MessageChannelEnum.getByCode(channel);
        return ResponseEntity.ok(messageService.sendWithTemplate(templateId, params, receivers, channelEnum));
    }
    
    /**
     * 批量发送消息
     *
     * @param messages 消息列表
     * @return 发送结果列表
     */
    @PostMapping("/send/batch")
    public ResponseEntity<List<MessageResult>> batchSend(@RequestBody List<Message> messages) {
        return ResponseEntity.ok(messageService.batchSend(messages));
    }
    
    /**
     * 获取支持的渠道列表
     *
     * @return 渠道列表
     */
    @GetMapping("/channels")
    public ResponseEntity<Map<String, Object>> getChannels() {
        Map<String, Object> result = new HashMap<>();
        
        for (MessageChannelEnum channel : MessageChannelEnum.values()) {
            Map<String, String> channelInfo = new HashMap<>(2);
            channelInfo.put("code", channel.getCode());
            channelInfo.put("desc", channel.getDesc());
            
            result.put(channel.name(), channelInfo);
        }
        
        return ResponseEntity.ok(result);
    }
} 