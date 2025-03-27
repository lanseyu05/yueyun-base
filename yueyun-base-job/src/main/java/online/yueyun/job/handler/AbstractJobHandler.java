package online.yueyun.job.handler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;

/**
 * XXL-JOB任务处理器抽象基类
 * 提供通用的任务执行流程和异常处理
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractJobHandler {

    /**
     * 任务执行入口
     * 由XXL-JOB调用，使用@XxlJob注解标记
     */
    @XxlJob("${xxlJobName}")
    public void execute() {
        // 获取任务参数
        String param = XxlJobHelper.getJobParam();
        
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        
        // 记录任务开始
        log.info("XXL-JOB任务开始执行: 任务名称={}, 任务参数={}, 分片索引={}, 分片总数={}", 
                this.getClass().getSimpleName(), param, shardIndex, shardTotal);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行任务前的准备工作
            beforeExecute(param, shardIndex, shardTotal);
            
            // 执行具体任务
            doExecute(param, shardIndex, shardTotal);
            
            // 执行任务后的清理工作
            afterExecute(param, shardIndex, shardTotal);
            
            // 记录执行成功
            long cost = System.currentTimeMillis() - startTime;
            log.info("XXL-JOB任务执行成功: 任务名称={}, 耗时={}ms", 
                    this.getClass().getSimpleName(), cost);
            
            // 设置任务结果
            XxlJobHelper.handleSuccess("执行成功");
        } catch (Exception e) {
            // 记录执行失败
            long cost = System.currentTimeMillis() - startTime;
            log.error("XXL-JOB任务执行失败: 任务名称={}, 耗时={}ms", 
                    this.getClass().getSimpleName(), cost, e);
            
            // 设置任务失败并记录失败原因
            XxlJobHelper.handleFail("执行失败: " + e.getMessage());
            
            // 执行异常处理
            onException(e, param, shardIndex, shardTotal);
        }
    }
    
    /**
     * 执行任务前的准备工作
     *
     * @param param 任务参数
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     * @throws Exception 执行异常
     */
    protected void beforeExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 默认实现为空，子类可以根据需要覆盖
    }
    
    /**
     * 执行具体任务
     * 子类必须实现此方法
     *
     * @param param 任务参数
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     * @throws Exception 执行异常
     */
    protected abstract void doExecute(String param, int shardIndex, int shardTotal) throws Exception;
    
    /**
     * 执行任务后的清理工作
     *
     * @param param 任务参数
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     * @throws Exception 执行异常
     */
    protected void afterExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 默认实现为空，子类可以根据需要覆盖
    }
    
    /**
     * 执行异常处理
     *
     * @param e 异常
     * @param param 任务参数
     * @param shardIndex 分片索引
     * @param shardTotal 分片总数
     */
    protected void onException(Exception e, String param, int shardIndex, int shardTotal) {
        // 默认实现为空，子类可以根据需要覆盖
    }
} 