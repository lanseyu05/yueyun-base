package online.yueyun.mq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import online.yueyun.mq.model.MessageConsumed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息消费记录Mapper接口
 *
 * @author YueYun
 * @since 1.0.0
 */
@Mapper
public interface MessageConsumedMapper extends BaseMapper<MessageConsumed> {
} 