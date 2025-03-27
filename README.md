# 悦芸基础组件库（YueYun Base）

这是一个基于Spring Boot 3的基础组件库，提供多种常用的服务和中间件集成。此项目的设计目标是让开发者能够按需加载并简单配置即可使用各种功能组件。

## 特点

- 模块化设计，按需引入
- 基于Spring Boot 3最新版本
- 持久层使用MyBatis-Plus，提供强大的CRUD和数据权限功能
- 集成常用中间件和服务
- 简单的配置方式，开箱即用
- 高度可扩展

## 包含模块

| 模块名 | 功能描述 |
| ------ | -------- |
| yueyun-base-common | 通用工具和基础类库 |
| yueyun-base-datapermission | 数据权限控制，灵活配置数据访问策略 |
| yueyun-base-ip | IP地址检索和解析功能 |
| yueyun-base-excel | 基于EasyExcel的Excel导入导出功能 |
| yueyun-base-job | 定时任务调度框架集成 |
| yueyun-base-skywalking | SkyWalking链路追踪集成 |
| yueyun-base-mq | 消息队列集成（Kafka、RocketMQ、RabbitMQ） |
| yueyun-base-mybatisplus | MyBatis-Plus增强功能 |
| yueyun-base-redis | Redis缓存操作和分布式锁实现 |

## 使用方法

1. 在你的项目中添加所需模块的依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-xxx</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. 在application.yml中进行相关配置

```yaml
yueyun:
  xxx:
    enabled: true
    # 其他配置项
```

3. 在启动类上添加相关注解（如需要）

```java
@EnableYueyunXXX
```

## 版本要求

- Java 17+
- Spring Boot 3.2.0+

## 许可证

本项目基于 MIT 许可证发布 