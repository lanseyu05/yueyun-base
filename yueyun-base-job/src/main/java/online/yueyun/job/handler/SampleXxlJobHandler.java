package online.yueyun.job.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例任务处理器
 */
@Slf4j
@Component
public class SampleXxlJobHandler {

    /**
     * 示例任务1
     */
    @XxlJob("sampleJobHandler")
    public void sampleJobHandler() {
        log.info("开始执行示例任务");
        XxlJobHelper.log("XXL-JOB, 任务执行开始");
        
        // 模拟任务执行
        if (Math.random() < 0.5) {
            throw new RuntimeException("模拟任务执行失败");
        }
        
        log.info("示例任务执行成功");
        XxlJobHelper.log("XXL-JOB, 任务执行成功");
        XxlJobHelper.handleSuccess();
    }

    /**
     * 示例任务2 - 分片任务
     */
    @XxlJob("sampleShardingJobHandler")
    public void sampleShardingJobHandler() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        
        log.info("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        XxlJobHelper.log("XXL-JOB, 分片任务执行开始");
        
        // 业务逻辑
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                log.info("第 {} 片, 命中分片开始处理", i);
                // 模拟任务执行
                if (Math.random() < 0.5) {
                    throw new RuntimeException("模拟分片任务执行失败");
                }
            } else {
                log.info("第 {} 片, 忽略", i);
            }
        }
        
        log.info("分片任务执行成功");
        XxlJobHelper.log("XXL-JOB, 分片任务执行成功");
        XxlJobHelper.handleSuccess();
    }
} 