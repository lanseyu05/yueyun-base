package online.yueyun.job.model;

import lombok.Data;

/**
 * 任务信息实体类
 */
@Data
public class JobInfo {
    
    /**
     * 任务ID
     */
    private Long jobId;
    
    /**
     * 任务名称
     */
    private String jobName;
    
    /**
     * 任务处理器
     */
    private String jobHandler;
    
    /**
     * 任务参数
     */
    private String jobParam;
    
    /**
     * 触发时间
     */
    private String triggerTime;
    
    /**
     * 日志内容
     */
    private String logContent;
    
    /**
     * 错误信息
     */
    private String errorMsg;
} 