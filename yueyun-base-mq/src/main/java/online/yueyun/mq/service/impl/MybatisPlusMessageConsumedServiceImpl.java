package online.yueyun.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mq.mapper.MessageConsumedMapper;
import online.yueyun.mq.model.MessageConsumed;
import online.yueyun.mq.service.MessageConsumedService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消息消费记录服务MyBatis-Plus实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class MybatisPlusMessageConsumedServiceImpl extends ServiceImpl<MessageConsumedMapper, MessageConsumed> implements MessageConsumedService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsConsumed(String msgId, String consumerGroup) {
        try {
            MessageConsumed messageConsumed = MessageConsumed.builder()
                    .msgId(msgId)
                    .consumerGroup(consumerGroup)
                    .consumeTime(LocalDateTime.now())
                    .build();
            return super.save(messageConsumed);
        } catch (DuplicateKeyException e) {
            log.debug("消息已经被消费: msgId={}, consumerGroup={}", msgId, consumerGroup);
            return true; // 消息已经被消费，也算成功
        } catch (Exception e) {
            log.error("记录消息消费失败: msgId={}, consumerGroup={}", msgId, consumerGroup, e);
            return false;
        }
    }

    @Override
    public boolean isConsumed(String msgId, String consumerGroup) {
        LambdaQueryWrapper<MessageConsumed> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MessageConsumed::getMsgId, msgId)
                .eq(MessageConsumed::getConsumerGroup, consumerGroup);
        return super.count(queryWrapper) > 0;
    }
} 