# 悦芸基础组件 - XXL-JOB任务调度模块

本模块提供了基于XXL-JOB的分布式任务调度功能，支持定时任务、分片任务等多种任务类型。

## 功能特性

- 自动配置XXL-JOB执行器，无需额外编写配置代码
- 提供统一的任务处理器抽象基类，简化任务开发
- 支持任务分片，可以在多个执行器上并行执行同一个任务
- 提供任务参数解析工具，方便处理复杂参数
- 完善的任务执行日志和异常处理机制
- 提供通用调度器，支持通过配置调用任意Spring Bean方法
- 提供多种报警通知方式，支持日志、邮件、钉钉、企业微信等多种报警渠道

## 使用方法

### 1. 添加依赖

在项目的`pom.xml`文件中添加依赖：

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-job</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. 启用XXL-JOB

在Spring Boot应用的启动类上添加`@EnableXxlJob`注解：

```java
import online.yueyun.job.annotation.EnableXxlJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableXxlJob
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置XXL-JOB

在`application.yml`或`application.properties`中添加XXL-JOB相关配置：

```yaml
yueyun:
  xxl:
    job:
      # 是否启用XXL-JOB，默认为true
      enabled: true
      
      # 调度中心配置
      admin:
        # 调度中心地址，多个地址用逗号分隔
        addresses: http://xxl-job-admin.example.com/xxl-job-admin
      
      # 执行器配置
      executor:
        # 执行器应用名称，默认为spring.application.name
        app-name: ${spring.application.name}
        
        # 执行器端口，默认为9999
        port: 9999
        
        # 执行器日志路径，默认为./logs/xxl-job
        log-path: ./logs/xxl-job
        
        # 执行器访问令牌，为空则不需要令牌
        access-token: 
        
      # 报警通知配置
      alarm:
        # 是否启用报警通知，默认为true
        enabled: true
        
        # 报警通知器列表，可同时启用多个
        notifiers:
          - log     # 日志报警
          # - email   # 邮件报警
          # - dingtalk # 钉钉报警
          # - wechatwork # 企业微信报警
```

### 4. 创建任务处理器

#### 方式一：继承AbstractJobHandler

继承`AbstractJobHandler`抽象类，实现自定义任务处理器：

```java
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.job.handler.AbstractJobHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class YourJobHandler extends AbstractJobHandler {

    @XxlJob("yourJobHandler")
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
        log.info("任务执行中: 参数={}, 分片索引={}, 分片总数={}", param, shardIndex, shardTotal);
        
        // 实现具体的任务逻辑
        
        log.info("任务执行完成");
    }
}
```

#### 方式二：使用通用调度器

无需编写处理器代码，只需在XXL-JOB调度中心配置任务，使用`scheduleJobHandler`处理器，并在参数中指定要调用的Bean和方法：

- JobHandler: `scheduleJobHandler`
- 执行参数: `yourService.doTask` 或 `com.your.package.YourService.doTask`

### 5. 在XXL-JOB调度中心配置任务

1. 登录XXL-JOB调度中心
2. 在"执行器管理"中确认执行器已注册
3. 在"任务管理"中新增任务：
   - 执行器：选择您的应用对应的执行器
   - JobHandler：填写您定义的任务处理器名称，例如"yourJobHandler"
   - 调度类型：选择"CRON"并设置执行规则
   - 执行参数：根据需要设置
   - 分片参数：如需分片执行，设置分片数量
   - 路由策略：根据需要选择
   - 阻塞处理策略：根据需要选择
   - 任务超时时间：设置合适的任务超时时间
   - 失败重试次数：设置合适的重试次数

## 高级功能

### 任务分片

```java
@Override
protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
    if (shardTotal > 1) {
        // 分片任务处理
        log.info("当前分片: {}/{}", shardIndex + 1, shardTotal);
        
        // 根据shardIndex和shardTotal对数据进行分片处理
        // 例如：处理ID % shardTotal == shardIndex的数据
    } else {
        // 非分片任务处理
        log.info("非分片任务");
    }
}
```

### 复杂参数处理

使用`JobParamUtils`工具类处理JSON格式的任务参数：

```java
import online.yueyun.job.util.JobParamUtils;

@Override
protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
    // 解析JSON参数
    String value = JobParamUtils.getString(param, "key");
    Integer count = JobParamUtils.getInteger(param, "count", 10);
    
    // 使用参数执行任务
    log.info("使用参数: value={}, count={}", value, count);
}
```

### 通用调度器使用示例

1. 创建您的业务服务：

```java
@Service
public class EmailService {
    
    /**
     * 发送每日报告邮件
     */
    public void sendDailyReport() {
        // 实现发送邮件的业务逻辑
        System.out.println("发送每日报告邮件");
    }
}
```

2. 在XXL-JOB管理平台创建任务：
   - JobHandler: `scheduleJobHandler`
   - 执行参数: `emailService.sendDailyReport`
   - Cron: `0 0 9 * * ?` (每天上午9点执行)

这样就可以定时调用`EmailService`的`sendDailyReport`方法，无需编写额外的任务处理器。

### 报警通知

当任务执行失败时，系统会自动发送报警通知。支持以下几种报警方式：

#### 1. 日志报警（默认启用）

将任务失败信息记录到日志文件中。

#### 2. 邮件报警

通过邮件发送任务失败通知，需要配置Spring Mail相关参数：

```yaml
# 任务报警配置
yueyun:
  xxl:
    job:
      alarm:
        notifiers:
          - log
          - email
        email:
          enabled: true
          from: no-reply@example.com
          to: admin@example.com,alert@example.com
          include-stack-trace: true

# Spring Mail配置
spring:
  mail:
    host: smtp.example.com
    port: 25
    username: no-reply@example.com
    password: password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### 3. 钉钉报警

通过钉钉机器人WebHook发送任务失败通知：

```yaml
yueyun:
  xxl:
    job:
      alarm:
        notifiers:
          - log
          - dingtalk
        ding-talk:
          enabled: true
          web-hook-url: https://oapi.dingtalk.com/robot/send?access_token=xxx
          secret: SEC000000000000000000000
          at-mobiles: 13800138000,13900139000
          at-all: false
```

#### 4. 企业微信报警

通过企业微信机器人WebHook发送任务失败通知：

```yaml
yueyun:
  xxl:
    job:
      alarm:
        notifiers:
          - log
          - wechatwork
        we-chat-work:
          enabled: true
          web-hook-url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
          mentioned-list: wangwu|lisi
          mentioned-mobile-list: 13800138000|13900139000
```

## 注意事项

1. 每个任务处理器只能处理一种类型的任务，不要在一个处理器中混合多种业务逻辑
2. 任务执行时间不宜过长，建议控制在配置的超时时间内
3. 对于大型任务，建议使用分片执行
4. 任务执行过程中的异常会被记录在XXL-JOB调度中心日志中
5. 任务执行过程中产生的详细日志会保存在配置的logPath目录下
6. 通用调度器目前只支持无参数方法的调用，如需传参请使用自定义任务处理器
7. 邮件报警需要配置正确的SMTP服务器参数
8. 钉钉和企业微信报警需要配置正确的WebHook地址

## 示例代码

- `SampleXxlJobHandler.java`: 提供了一个完整的任务处理器示例
- `ScheduleJobHandler.java`: 通用任务调度处理器，支持通过配置调用任意Spring Bean方法
- `LogAlarmNotifier.java`: 日志报警通知实现
- `EmailAlarmNotifier.java`: 邮件报警通知实现
- `DingTalkAlarmNotifier.java`: 钉钉报警通知实现
- `WeChatWorkAlarmNotifier.java`: 企业微信报警通知实现

## XXL-JOB调度中心

XXL-JOB调度中心是一个单独的应用，需要单独部署，参考[XXL-JOB官方文档](https://www.xuxueli.com/xxl-job/)进行部署和配置。 