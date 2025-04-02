package online.yueyun.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务告警配置实体类
 */
@Data
@TableName("job_alarm_config")
public class JobAlarmConfig {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 任务ID
     */
    private Long jobId;
    
    /**
     * 告警类型（1：邮件 2：钉钉）
     */
    private Integer alarmType;
    
    /**
     * 告警接收人（邮件地址或钉钉用户ID，多个用逗号分隔）
     */
    private String receivers;
    
    /**
     * 告警模板
     */
    private String alarmTemplate;
    
    /**
     * 是否启用（0：禁用 1：启用）
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
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新人
     */
    private String updateBy;
} 