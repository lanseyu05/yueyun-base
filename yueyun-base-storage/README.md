# 悦芸文件存储模块

> 提供统一的文件存储服务，支持MinIO、阿里云OSS等多种存储方式，易于扩展。

## 功能特性

- 支持多种存储方式，可通过配置切换
  - MinIO对象存储
  - 阿里云OSS对象存储
  - 可扩展其他存储方式
- 统一的文件上传、删除、访问API
- 自动创建存储桶
- 支持文件元数据管理
- 提供Ant Design Pro上传组件

## 快速开始

### 引入依赖

```xml
<dependency>
    <groupId>online.yueyun</groupId>
    <artifactId>yueyun-base-storage</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 配置文件

```yaml
yueyun:
  storage:
    # 存储类型：minio, aliyun-oss
    type: minio
    
    # MinIO配置
    minio:
      endpoint: http://localhost:9000
      access-key: minioadmin
      secret-key: minioadmin
      default-bucket-name: yueyun
      connect-timeout: 10
      secure: false
      
    # 阿里云OSS配置
    aliyun-oss:
      endpoint: https://oss-cn-hangzhou.aliyuncs.com
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      default-bucket-name: yueyun
      connect-timeout: 10
```

### 示例代码

```java
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final StorageService storageService;
    
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        // 上传到默认存储桶
        return storageService.uploadFile(file, "yueyun");
    }
    
    @GetMapping("/url")
    public String getUrl(@RequestParam String objectName) {
        // 获取文件访问URL，有效期1小时
        return storageService.getFileUrl("yueyun", objectName, 3600);
    }
    
    @DeleteMapping("/delete")
    public boolean delete(@RequestParam String objectName) {
        // 删除文件
        return storageService.deleteFile("yueyun", objectName);
    }
}
```

## 前端集成

### 安装依赖

```bash
npm install --save antd @ant-design/icons
```

### 引入组件

```tsx
import React, { useState } from 'react';
import { message } from 'antd';
import FileUpload from '@/components/FileUpload';
import type { UploadFile } from 'antd/es/upload/interface';

const MyComponent: React.FC = () => {
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  const handleChange = (files: UploadFile[]) => {
    setFileList(files);
  };

  const handleSuccess = (response: any) => {
    message.success('上传成功：' + response.fileName);
    console.log('文件URL:', response.fileUrl);
  };

  return (
    <FileUpload 
      action="/storage/upload"
      bucketName="yueyun"
      accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
      maxSize={10}
      fileList={fileList}
      onChange={handleChange}
      onSuccess={handleSuccess}
    />
  );
};

export default MyComponent;
```

## 扩展新的存储实现

1. 创建新的存储服务实现类，继承`AbstractStorageServiceImpl`：

```java
@Slf4j
@Service("customStorageService")
public class CustomStorageServiceImpl extends AbstractStorageServiceImpl {

    @Override
    protected String doUploadFile(InputStream inputStream, String objectName, String bucketName, Map<String, String> metadata) {
        // 实现自定义存储逻辑
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        // 实现删除逻辑
    }

    @Override
    public String getFileUrl(String bucketName, String objectName, int expiry) {
        // 实现获取URL逻辑
    }

    @Override
    public void ensureBucketExists(String bucketName) {
        // 实现创建存储桶逻辑
    }
}
```

2. 将新存储类型添加到枚举类：

```java
public enum StorageTypeEnum {
    MINIO("minio", "MinIO对象存储"),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS对象存储"),
    CUSTOM("custom", "自定义存储");
    // ...
}
```

3. 更新配置类中的存储类型选择逻辑：

```java
@Bean
@Primary
public StorageService storageService() {
    StorageTypeEnum storageType = StorageTypeEnum.getByCode(storageProperties.getType());
    return switch (storageType) {
        case MINIO -> applicationContext.getBean("minioStorageService", StorageService.class);
        case ALIYUN_OSS -> applicationContext.getBean("aliyunOssStorageService", StorageService.class);
        case CUSTOM -> applicationContext.getBean("customStorageService", StorageService.class);
        default -> applicationContext.getBean("minioStorageService", StorageService.class);
    };
}
``` 