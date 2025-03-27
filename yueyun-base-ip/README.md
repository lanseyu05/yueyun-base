# IP地址检索模块 (yueyun-base-ip)

IP地址检索模块是YueYun基础框架的一部分，提供了IP地址的归属地查询功能，基于ip2region实现，支持离线IP地址库，查询快速高效。

## 特性

- 提供IP地址快速归属地查询
- 支持内存映射和文件加载两种方式
- 可配置内置IP地址库或自定义IP地址库
- 支持缓存机制，提高查询效率
- 自动配置和条件装配
- 通过注解方式快速启用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-ip</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 启用IP地址检索功能

在应用的启动类上添加`@EnableIpRegion`注解：

```java
@SpringBootApplication
@EnableIpRegion
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### 3. 配置IP地址检索属性（可选）

在`application.yml`或`application.properties`中配置IP检索属性：

```yaml
yueyun:
  ip:
    enabled: true
    # 数据库文件路径（为空则使用内置数据库）
    db-path: 
    # 自动加载数据库
    auto-load: true
    # 缓存类型：memory, file
    cache-type: memory
    # 是否启用缓存
    enable-cache: true
    # 缓存大小
    cache-size: 5000
    # 缓存过期时间（秒）
    cache-expire: 3600
```

### 4. 使用IpRegionService查询IP地址

```java
@RestController
@RequestMapping("/ip")
public class IpController {
    
    @Autowired
    private IpRegionService ipRegionService;
    
    @GetMapping("/search")
    public IpInfo search(@RequestParam String ip) {
        return ipRegionService.search(ip);
    }
    
    @GetMapping("/current")
    public IpInfo getCurrentIp(HttpServletRequest request) {
        return ipRegionService.searchFromRequest(request);
    }
}
```

## IP信息结构

`IpInfo`类包含以下字段：

- `ip`: IP地址
- `country`: 国家
- `province`: 省份
- `city`: 城市
- `district`: 区县
- `isp`: 运营商
- `areaCode`: 地区代码
- `rawRegion`: 原始信息

## 模块架构

```
yueyun-base-ip
├── annotation              - 注解
│   └── EnableIpRegion      - 启用IP地址检索功能注解
├── config                  - 配置
│   ├── IpRegionProperties  - 配置属性类
│   └── IpRegionAutoConfiguration - 自动配置类
├── model                   - 模型
│   └── IpInfo              - IP信息类
├── service                 - 服务
│   ├── IpRegionService     - IP地址检索服务接口
│   └── impl                - 实现类
│       └── Ip2RegionServiceImpl - 基于ip2region的服务实现
└── utils                   - 工具类
    └── IpUtils             - IP工具类
```

## 高级用法

### 1. 使用自定义IP地址库

默认情况下，模块使用内置的ip2region数据库文件。如果您需要使用自定义的IP地址库文件，可以配置`db-path`属性：

```yaml
yueyun:
  ip:
    db-path: /path/to/your/ip2region.xdb
```

### 2. 优化内存使用

如果您的应用内存资源有限，可以选择使用文件缓存模式：

```yaml
yueyun:
  ip:
    cache-type: file
```

### 3. 判断IP是否为内网IP

```java
boolean isInternal = ipRegionService.isInternalIp("192.168.1.1");
// 或者使用工具类
boolean isInternal = IpUtils.isInternalIp("192.168.1.1");
```

## 注意事项

1. IP地址库的更新需要手动进行
2. 内存缓存模式下查询更快，但会占用更多内存
3. 文件缓存模式下内存占用更少，但查询速度可能略慢
4. 缓存机制可以提高查询效率，但会占用一定内存 