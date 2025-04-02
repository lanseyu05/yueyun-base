package online.yueyun.job.alarm.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.job.alarm.AlarmContentBuilder;
import online.yueyun.job.model.JobInfo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 默认告警内容构建器  默认实现不看模板
 */
@Slf4j
@Component
public class DefaultAlarmContentBuilder implements AlarmContentBuilder {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String buildContent(JobInfo jobInfo, String alarmTemplate) {
        StringBuilder content = new StringBuilder();
        content.append("【任务告警】\n");
        content.append("任务ID：").append(jobInfo.getJobId()).append("\n");
        content.append("任务名称：").append(jobInfo.getJobName()).append("\n");
        content.append("任务处理器：").append(jobInfo.getJobHandler()).append("\n");
        content.append("任务参数：").append(jobInfo.getJobParam()).append("\n");
        content.append("触发时间：").append(jobInfo.getTriggerTime()).append("\n");
        content.append("执行日志：").append(jobInfo.getLogContent()).append("\n");
        content.append("错误信息：").append(jobInfo.getErrorMsg()).append("\n");
        content.append("告警时间：").append(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return content.toString();
    }
} 