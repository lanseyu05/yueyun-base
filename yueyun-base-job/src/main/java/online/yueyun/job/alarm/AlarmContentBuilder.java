package online.yueyun.job.alarm;

import online.yueyun.job.entity.JobAlarmConfig;
import online.yueyun.job.model.JobInfo;

/**
 * 告警内容构建器接口
 */
public interface AlarmContentBuilder {

    /**
     * 构建告警内容
     *
     * @param jobInfo       任务信息
     * @param alarmTemplate 告警模板
     * @return 告警内容
     */
    String buildContent(JobInfo jobInfo, String alarmTemplate);
} 