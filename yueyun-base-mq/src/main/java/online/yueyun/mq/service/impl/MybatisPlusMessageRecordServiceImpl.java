package online.yueyun.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.mq.mapper.MessageRecordMapper;
import online.yueyun.mq.model.MessageRecord;
import online.yueyun.mq.service.MessageRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 消息记录服务MyBatis-Plus实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class MybatisPlusMessageRecordServiceImpl extends ServiceImpl<MessageRecordMapper, MessageRecord> implements MessageRecordService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(MessageRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        record.setUpdateTime(LocalDateTime.now());
        return super.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(String msgId, MessageRecord.Status status) {
        MessageRecord record = this.getByMsgId(msgId);
        if (record == null) {
            log.warn("消息记录不存在: msgId={}", msgId);
            return false;
        }
        record.setStatus(status.getValue());
        record.setUpdateTime(LocalDateTime.now());
        return super.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(MessageRecord record) {
        record.setUpdateTime(LocalDateTime.now());
        return super.updateById(record);
    }

    @Override
    public MessageRecord getByMsgId(String msgId) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MessageRecord::getMsgId, msgId);
        return super.getOne(queryWrapper);
    }

    @Override
    public MessageRecord getByBusinessKey(String businessKey) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MessageRecord::getBusinessKey, businessKey);
        return super.getOne(queryWrapper);
    }

    @Override
    public List<MessageRecord> findToRetry(int limit) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.lambdaQuery();
        // 查找发送失败且重试次数小于最大重试次数的消息
        queryWrapper.eq(MessageRecord::getStatus, MessageRecord.Status.SEND_FAILED.getValue())
                .apply("retry_count < max_retry_count") // 使用原生SQL替代有问题的lt方法
                .le(MessageRecord::getNextRetryTime, LocalDateTime.now())
                .orderByAsc(MessageRecord::getNextRetryTime)
                .last("LIMIT " + limit);
        return super.list(queryWrapper);
    }

    @Override
    public List<MessageRecord> findSentButNotConsumed(String topic, String consumerGroup, int limit) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.lambdaQuery();
        // 查找已发送但未消费的消息
        queryWrapper.eq(MessageRecord::getTopic, topic)
                .eq(MessageRecord::getConsumerGroup, consumerGroup)
                .eq(MessageRecord::getStatus, MessageRecord.Status.SENT.getValue())
                .orderByAsc(MessageRecord::getCreateTime)
                .last("LIMIT " + limit);
        return super.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsConsumed(String msgId, String consumerGroup) {
        MessageRecord record = this.getByMsgId(msgId);
        if (record == null) {
            log.warn("消息记录不存在: msgId={}", msgId);
            return false;
        }
        
        // 只有已发送状态的消息才能标记为已消费
        if (record.getStatus() != MessageRecord.Status.SENT.getValue()) {
            log.warn("消息状态不正确，无法标记为已消费: msgId={}, status={}", msgId, record.getStatus());
            return false;
        }
        
        record.setStatus(MessageRecord.Status.CONSUMED.getValue());
        record.setConsumerGroup(consumerGroup);
        record.setUpdateTime(LocalDateTime.now());
        return super.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsConsumedFailed(String msgId, String consumerGroup, String errorMessage) {
        MessageRecord record = this.getByMsgId(msgId);
        if (record == null) {
            log.warn("消息记录不存在: msgId={}", msgId);
            return false;
        }
        
        // 更新重试次数和下次重试时间
        record.setStatus(MessageRecord.Status.CONSUME_FAILED.getValue());
        record.setConsumerGroup(consumerGroup);
        record.setRetryCount(record.getRetryCount() + 1);
        // 设置下次重试时间，每次重试间隔翻倍，最大间隔1小时
        int delayMinutes = Math.min(60, (int) Math.pow(2, record.getRetryCount()));
        record.setNextRetryTime(LocalDateTime.now().plusMinutes(delayMinutes));
        record.setUpdateTime(LocalDateTime.now());
        return super.updateById(record);
    }

    @Override
    public boolean isConsumed(String msgId, String consumerGroup) {
        LambdaQueryWrapper<MessageRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MessageRecord::getMsgId, msgId)
                .eq(MessageRecord::getConsumerGroup, consumerGroup)
                .eq(MessageRecord::getStatus, MessageRecord.Status.CONSUMED.getValue());
        return super.count(queryWrapper) > 0;
    }
} 