# 定时任务模块 (yueyun-base-job)

定时任务模块是YueYun基础框架的一部分，基于XXL-JOB实现分布式任务调度，提供统一的任务调度接口和抽象基类，简化定时任务的开发和管理。

## 特性

- 基于XXL-JOB实现分布式任务调度
- 提供统一的任务处理器抽象基类
- 支持任务分片和并行处理
- 支持任务参数解析和日志记录
- 提供任务执行前置、后置和异常处理扩展点
- 自动配置和条件装配
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-job</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用XXL-JOB功能

在应用的启动类上添加`@EnableXxlJob`注解：

```java
@SpringBootApplication
@EnableXxlJob
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置XXL-JOB属性

在`application.yml`或`application.properties`中配置XXL-JOB属性：

```yaml
yueyun:
  xxl:
    job:
      enabled: true
      admin:
        # 调度中心地址
        addresses: http://xxl-job-admin:8080/xxl-job-admin
      executor:
        # 执行器AppName
        app-name: ${spring.application.name}
        # 执行器注册方式
        address-type: AUTO
        # 执行器IP（可选）
        ip: 
        # 执行器端口
        port: 9999
        # 执行器日志路径
        log-path: ./logs/xxl-job
        # 执行器日志保留天数
        log-retention-days: 30
        # 访问令牌（可选）
        access-token: 
```

### 4. 创建任务处理器

方式一：继承`AbstractJobHandler`抽象类

```java
@Component
public class DemoJobHandler extends AbstractJobHandler {

    // 使用XXL-JOB注解指定任务处理器名称
    @XxlJob("demoJobHandler")
    @Override
    public void execute() {
        // 调用父类方法，自动处理异常和日志
        super.execute();
    }

    @Override
    protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 实现具体任务逻辑
        JobParamUtils.log("执行任务，参数: {}, 分片: {}/{}", param, shardIndex, shardTotal);
        
        // 任务处理逻辑
        // ...
    }
    
    @Override
    protected void beforeExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 任务执行前的准备工作
        JobParamUtils.log("任务准备阶段...");
    }
    
    @Override
    protected void afterExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 任务执行后的清理工作
        JobParamUtils.log("任务清理阶段...");
    }
    
    @Override
    protected void onException(Exception e, String param, int shardIndex, int shardTotal) {
        // 异常处理
        JobParamUtils.log("任务执行异常: {}", e.getMessage());
    }
}
```

方式二：直接使用XXL-JOB注解

```java
@Component
public class SimpleJobHandler {

    @XxlJob("simpleJobHandler")
    public void execute() {
        String param = XxlJobHelper.getJobParam();
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        
        try {
            // 任务处理逻辑
            XxlJobHelper.log("执行任务，参数: {}, 分片: {}/{}", param, shardIndex, shardTotal);
            
            // 设置成功
            XxlJobHelper.handleSuccess();
        } catch (Exception e) {
            // 设置失败
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
```

### 5. 使用任务参数工具类

```java
// 获取任务参数
String param = JobParamUtils.getJobParam();

// 解析JSON参数为对象
UserDto user = JobParamUtils.getJobParam(UserDto.class);

// 解析参数为Map
Map<String, Object> paramMap = JobParamUtils.getJobParamMap();

// 输出任务日志
JobParamUtils.log("处理用户数据: {}", user.getName());

// 设置任务结果
JobParamUtils.handleSuccess("处理成功");
```

## 模块架构

```
yueyun-base-job
├── annotation              - 注解
│   ├── EnableXxlJob        - 启用XXL-JOB功能注解
│   └── XxlJob              - XXL-JOB任务注解
├── config                  - 配置
│   ├── XxlJobProperties    - 配置属性类
│   └── XxlJobAutoConfiguration - 自动配置类
├── handler                 - 处理器
│   └── AbstractJobHandler  - 任务处理器抽象基类
└── util                    - 工具类
    └── JobParamUtils       - 任务参数工具类
```

## 高级用法

### 1. 任务分片

```java
@Component
public class ShardingJobHandler extends AbstractJobHandler {

    @XxlJob("shardingJobHandler")
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 根据分片参数处理数据
        List<Long> allIds = getAllDataIds();
        
        // 对数据进行分片
        List<Long> shardIds = getShardingIds(allIds, shardIndex, shardTotal);
        
        // 处理分片数据
        for (Long id : shardIds) {
            // 处理单条数据...
            JobParamUtils.log("处理数据ID: {}", id);
        }
    }
    
    /**
     * 获取分片数据
     */
    private List<Long> getShardingIds(List<Long> allIds, int shardIndex, int shardTotal) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < allIds.size(); i++) {
            if (i % shardTotal == shardIndex) {
                result.add(allIds.get(i));
            }
        }
        return result;
    }
    
    /**
     * 获取所有数据ID
     */
    private List<Long> getAllDataIds() {
        // 从数据库或其他数据源获取ID列表
        return Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }
}
```

### 2. 使用JSON任务参数

```java
@Component
public class JsonParamJobHandler extends AbstractJobHandler {

    @XxlJob("jsonParamJobHandler")
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void doExecute(String param, int shardIndex, int shardTotal) throws Exception {
        // 解析JSON参数
        JobConfig config = JobParamUtils.getJobParam(JobConfig.class);
        if (config == null) {
            JobParamUtils.log("无效的任务参数");
            return;
        }
        
        // 使用参数执行任务
        JobParamUtils.log("任务配置: 批次大小={}, 超时时间={}", 
                config.getBatchSize(), config.getTimeout());
                
        // 处理任务逻辑...
    }
    
    /**
     * 任务配置类
     */
    @Data
    public static class JobConfig {
        private int batchSize;
        private int timeout;
        private String dataSource;
        private List<String> tables;
    }
}
```

## 注意事项

1. 需要先部署XXL-JOB调度中心
2. 执行器端口默认为9999，可以根据实际情况修改
3. 生产环境建议设置访问令牌增强安全性
4. 多个应用使用同一个执行器AppName时会路由到集群中的某一个实例
5. 使用分片时，确保数据可以均匀分布到各个分片 