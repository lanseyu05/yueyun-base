# yueyun-base-storage 模块

## 1. 最小化接入方案

### 1.1 添加依赖
```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-storage</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 1.2 基础配置
```yaml
yueyun:
  storage:
    # 存储类型：local, minio, oss, cos
    type: local
    # 本地存储配置
    local:
      path: /data/storage
    # MinIO配置
    minio:
      endpoint: http://localhost:9000
      access-key: your-access-key
      secret-key: your-secret-key
      bucket-name: your-bucket
    # 阿里云OSS配置
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      access-key: your-access-key
      secret-key: your-secret-key
      bucket-name: your-bucket
    # 腾讯云COS配置
    cos:
      region: ap-guangzhou
      access-key: your-access-key
      secret-key: your-secret-key
      bucket-name: your-bucket
```

### 1.3 启用存储服务
```java
@Configuration
@EnableStorageService
public class StorageConfig {
    @Bean
    public StorageService storageService() {
        return new StorageServiceImpl();
    }
}
```

## 2. 详细进阶配置

### 2.1 文件上传
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public String uploadFile(MultipartFile file, String path) {
        return storageService.uploadFile(file, path);
    }
    
    public String uploadFile(InputStream inputStream, String originalFilename, String path) {
        return storageService.uploadFile(inputStream, originalFilename, path);
    }
}
```

### 2.2 文件下载
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public void downloadFile(String path, OutputStream outputStream) {
        storageService.downloadFile(path, outputStream);
    }
    
    public byte[] downloadFileAsBytes(String path) {
        return storageService.downloadFileAsBytes(path);
    }
}
```

### 2.3 文件删除
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public void deleteFile(String path) {
        storageService.deleteFile(path);
    }
    
    public void deleteFiles(List<String> paths) {
        storageService.deleteFiles(paths);
    }
}
```

### 2.4 文件访问URL
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public String getFileUrl(String path) {
        return storageService.getFileUrl(path);
    }
    
    public String getFileUrl(String path, int expiry) {
        return storageService.getFileUrl(path, expiry);
    }
}
```

### 2.5 文件元数据
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public Map<String, String> getFileMetadata(String path) {
        return storageService.getFileMetadata(path);
    }
    
    public void setFileMetadata(String path, Map<String, String> metadata) {
        storageService.setFileMetadata(path, metadata);
    }
}
```

### 2.6 文件列表
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public List<FileInfo> listFiles(String path) {
        return storageService.listFiles(path);
    }
    
    public List<FileInfo> listFiles(String path, String prefix) {
        return storageService.listFiles(path, prefix);
    }
}
```

### 2.7 文件复制
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public void copyFile(String sourcePath, String targetPath) {
        storageService.copyFile(sourcePath, targetPath);
    }
    
    public void moveFile(String sourcePath, String targetPath) {
        storageService.moveFile(sourcePath, targetPath);
    }
}
```

### 2.8 文件校验
```java
@Service
public class FileService {
    @Autowired
    private StorageService storageService;
    
    public boolean exists(String path) {
        return storageService.exists(path);
    }
    
    public long getFileSize(String path) {
        return storageService.getFileSize(path);
    }
    
    public String getFileMd5(String path) {
        return storageService.getFileMd5(path);
    }
} 