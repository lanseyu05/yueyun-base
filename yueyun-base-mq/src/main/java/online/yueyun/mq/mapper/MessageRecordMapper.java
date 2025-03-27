package online.yueyun.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import online.yueyun.mq.model.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息记录Mapper接口
 *
 * @author YueYun
 * @since 1.0.0
 */
@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
} 