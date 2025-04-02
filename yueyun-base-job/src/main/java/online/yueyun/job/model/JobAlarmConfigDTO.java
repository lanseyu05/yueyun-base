package online.yueyun.job.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务告警配置DTO
 */
@Data
public class JobAlarmConfigDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    private Long jobId;
    
    /**
     * 告警类型：1-邮件 2-钉钉
     */
    private Integer alarmType;
    
    /**
     * 接收人
     */
    private String receivers;
    
    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 