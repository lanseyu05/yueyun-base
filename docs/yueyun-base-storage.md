# YueYun Base Storage 模块

## 简介
`yueyun-base-storage` 是文件存储模块，支持本地存储、阿里云OSS、腾讯云COS、七牛云等多种存储方式，提供了统一的文件上传下载接口。

## 主要功能

### 1. 文件上传
- 单文件上传
- 多文件上传
- 分片上传
- 断点续传
- 文件校验

### 2. 文件下载
- 文件下载
- 文件预览
- 文件流式下载
- 文件分享
- 文件权限控制

### 3. 文件管理
- 文件删除
- 文件移动
- 文件复制
- 文件重命名
- 文件列表

### 4. 存储策略
- 本地存储
- 阿里云OSS
- 腾讯云COS
- 七牛云存储
- 自定义存储

## 使用示例

### 存储配置
```java
@Configuration
@RequiredArgsConstructor
public class StorageConfig {
    
    @Value("${storage.type}")
    private String storageType;
    
    @Value("${storage.oss.endpoint}")
    private String ossEndpoint;
    
    @Value("${storage.oss.access-key}")
    private String ossAccessKey;
    
    @Value("${storage.oss.secret-key}")
    private String ossSecretKey;
    
    @Value("${storage.oss.bucket}")
    private String ossBucket;
    
    @Bean
    public StorageService storageService() {
        switch (storageType) {
            case "oss":
                return new OssStorageService(ossEndpoint, ossAccessKey, ossSecretKey, ossBucket);
            case "cos":
                return new CosStorageService();
            case "qiniu":
                return new QiniuStorageService();
            default:
                return new LocalStorageService();
        }
    }
}
```

### 文件上传
```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final StorageService storageService;
    
    public String uploadFile(MultipartFile file) {
        // 生成文件名
        String fileName = generateFileName(file.getOriginalFilename());
        
        // 上传文件
        return storageService.upload(
            file.getInputStream(),
            fileName,
            file.getContentType()
        );
    }
    
    public List<String> uploadFiles(List<MultipartFile> files) {
        return files.stream()
            .map(this::uploadFile)
            .collect(Collectors.toList());
    }
}
```

### 文件下载
```java
@Service
@RequiredArgsConstructor
public class FileService {
    
    private final StorageService storageService;
    
    public void downloadFile(String fileUrl, HttpServletResponse response) {
        // 获取文件信息
        StorageFile file = storageService.getFile(fileUrl);
        
        // 设置响应头
        response.setContentType(file.getContentType());
        response.setHeader(
            "Content-Disposition",
            "attachment;filename=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8)
        );
        
        // 下载文件
        storageService.download(fileUrl, response.getOutputStream());
    }
    
    public String getFileUrl(String fileUrl) {
        // 获取文件访问URL
        return storageService.getFileUrl(fileUrl);
    }
}
```

## 配置说明

### 基础配置
```yaml
storage:
  # 存储类型：local/oss/cos/qiniu
  type: local
  # 文件大小限制（MB）
  max-size: 10
  # 允许的文件类型
  allowed-types: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx
  # 文件存储路径
  path: /data/files
```

### OSS配置
```yaml
storage:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key: your-access-key
    secret-key: your-secret-key
    bucket: your-bucket
    domain: https://your-bucket.oss-cn-hangzhou.aliyuncs.com
```

### COS配置
```yaml
storage:
  cos:
    secret-id: your-secret-id
    secret-key: your-secret-key
    region: ap-guangzhou
    bucket: your-bucket
    domain: https://your-bucket.cos.ap-guangzhou.myqcloud.com
```

### 七牛云配置
```yaml
storage:
  qiniu:
    access-key: your-access-key
    secret-key: your-secret-key
    bucket: your-bucket
    domain: http://your-bucket.qiniudn.com
```

## 依赖要求
- Java 17+
- Spring Boot 3.2.0+
- 阿里云OSS SDK 3.15.1+
- 腾讯云COS SDK 5.6.155+
- 七牛云SDK 7.12.1+ 