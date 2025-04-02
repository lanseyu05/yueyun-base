# YueYun Base Job 模块

## 简介
`yueyun-base-job` 是基于XXL-JOB的分布式任务调度模块，提供了简单易用的定时任务管理功能。

## 主要功能

### 1. 任务管理
- 任务创建
- 任务修改
- 任务删除
- 任务暂停/恢复
- 任务执行记录

### 2. 任务类型
- Bean模式
- GLUE模式
- Shell模式
- Python模式
- 自定义模式

### 3. 任务特性
- 分布式执行
- 任务分片
- 任务重试
- 任务告警
- 任务依赖

### 4. 监控管理
- 执行日志
- 调度日志
- 告警记录
- 任务统计
- 执行报表

## 使用示例

### 任务定义
```java
@Component
@RequiredArgsConstructor
public class SampleJob {
    
    private final UserService userService;
    
    @XxlJob("sampleJobHandler")
    public void sampleJobHandler() {
        // 任务执行逻辑
        log.info("开始执行示例任务");
        userService.processData();
        log.info("示例任务执行完成");
    }
    
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        
        // 分片执行逻辑
        List<User> users = userService.getUsersByShard(shardIndex, shardTotal);
        for (User user : users) {
            userService.processUser(user);
        }
    }
}
```

### 任务配置
```java
@Configuration
@RequiredArgsConstructor
public class XxlJobConfig {
    
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    
    @Value("${xxl.job.accessToken}")
    private String accessToken;
    
    @Value("${xxl.job.executor.appname}")
    private String appname;
    
    @Value("${xxl.job.executor.ip}")
    private String ip;
    
    @Value("${xxl.job.executor.port}")
    private int port;
    
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAccessToken(accessToken);
        executor.setAppname(appname);
        executor.setIp(ip);
        executor.setPort(port);
        return executor;
    }
}
```

### 任务调用
```java
@Service
@RequiredArgsConstructor
public class JobService {
    
    private final XxlJobSpringExecutor xxlJobExecutor;
    
    public void triggerJob(String jobHandler) {
        // 触发任务
        JobTriggerPoolHelper.trigger(jobHandler, TriggerTypeEnum.MANUAL, -1, null, null);
    }
    
    public void triggerJobWithParam(String jobHandler, String param) {
        // 带参数触发任务
        JobTriggerPoolHelper.trigger(jobHandler, TriggerTypeEnum.MANUAL, -1, param, null);
    }
}
```

## 配置说明

### 基础配置
```yaml
xxl:
  job:
    admin:
      addresses: http://xxl-job-admin:8080/xxl-job-admin
    accessToken: default_token
    executor:
      appname: yueyun-job-executor
      ip: 
      port: 9999
      logpath: /data/applogs/xxl-job/jobhandler
      logretentiondays: 30
```

### 任务配置
```yaml
yueyun:
  job:
    # 是否开启任务
    enabled: true
    # 任务执行超时时间（秒）
    timeout: 300
    # 任务重试次数
    retry-count: 3
    # 任务重试间隔（秒）
    retry-interval: 30
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- XXL-JOB 2.4.0+ 