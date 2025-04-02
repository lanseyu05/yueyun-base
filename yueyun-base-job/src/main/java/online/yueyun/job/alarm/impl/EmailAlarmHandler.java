package online.yueyun.job.alarm.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.job.entity.JobAlarmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import online.yueyun.job.alarm.AlarmContentBuilder;
import online.yueyun.job.alarm.AlarmHandler;
import online.yueyun.job.model.JobInfo;

/**
 * 邮件告警处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAlarmHandler implements AlarmHandler {

    private final JavaMailSender mailSender;

    private final AlarmContentBuilder alarmContentBuilder;

    @Override
    public Integer getAlarmType() {
        return 1;
    }

    @Override
    public void sendAlarm(JobAlarmConfig config, JobInfo jobInfo) {
        String receivers = config.getReceivers();
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(receivers.split(","));
            message.setSubject("任务执行告警 - " + jobInfo.getJobName());

            // 构建告警内容
            String content = alarmContentBuilder.buildContent(jobInfo, config.getAlarmTemplate());
            message.setText(content);

            mailSender.send(message);
            log.info("Email alarm sent successfully to: {}", receivers);
        } catch (Exception e) {
            log.error("Failed to send email alarm", e);
        }
    }
} 