package online.yueyun.job.alarm;

import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.model.JobInfo;

/**
 * 告警处理器接口
 */
public interface AlarmHandler {
    
    /**
     * 获取告警类型
     *
     * @return 告警类型
     */
    Integer getAlarmType();
    
    /**
     * 发送告警
     *
     * @param config 接收人列表,告警模板
     * @param jobInfo 任务信息
     */
    void sendAlarm(JobAlarmConfig config, JobInfo jobInfo);
} 