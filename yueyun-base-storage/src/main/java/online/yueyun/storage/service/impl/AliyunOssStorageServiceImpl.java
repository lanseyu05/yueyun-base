package online.yueyun.storage.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.config.StorageProperties;
import online.yueyun.storage.exception.StorageException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * 阿里云OSS存储服务实现
 * 
 * @author yueyun
 */
@Slf4j
@Service("aliyunOssStorageService")
public class AliyunOssStorageServiceImpl extends AbstractStorageServiceImpl {

    private final StorageProperties.Oss ossConfig;
    private final OSS ossClient;

    public AliyunOssStorageServiceImpl(StorageProperties storageProperties) {
        if (storageProperties == null || storageProperties.getOss() == null) {
            throw new IllegalArgumentException("阿里云OSS配置不能为空");
        }
        this.ossConfig = storageProperties.getOss();
        this.ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKey(),
                ossConfig.getSecretKey());
        log.info("初始化阿里云OSS存储服务，端点: {}", ossConfig.getEndpoint());
    }

    @Override
    protected String doUploadFile(InputStream inputStream, String objectName, String bucketName, Map<String, String> metadata) {
        try {
            // 创建上传Object的元数据
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(metadata.getOrDefault("contentType", "application/octet-stream"));
            
            // 添加自定义元数据
            metadata.forEach((key, value) -> {
                if (!key.equals("contentType")) {
                    objectMetadata.addUserMetadata("x-oss-meta-" + key, value);
                }
            });

            // 上传文件
            ossClient.putObject(bucketName, objectName, inputStream, objectMetadata);

            // 返回文件URL
            return getFileUrl(bucketName, objectName, 7 * 24 * 60 * 60); // 默认7天有效期
        } catch (Exception e) {
            log.error("阿里云OSS上传文件失败", e);
            throw new StorageException("阿里云OSS上传文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
            return true;
        } catch (Exception e) {
            log.error("阿里云OSS删除文件失败", e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName, int expiry) {
        try {
            if (expiry > 0) {
                // 设置URL过期时间
                Date expirationDate = new Date(System.currentTimeMillis() + expiry * 1000L);
                
                // 生成以GET方法访问的签名URL
                URL url = ossClient.generatePresignedUrl(bucketName, objectName, expirationDate);
                
                return url.toString();
            } else {
                // 返回永久访问URL
                return String.format("https://%s.%s/%s", bucketName, ossConfig.getEndpoint(), objectName);
            }
        } catch (Exception e) {
            log.error("阿里云OSS获取文件URL失败", e);
            throw new StorageException("阿里云OSS获取文件URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) {
        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                ossClient.createBucket(bucketName);
                log.info("创建阿里云OSS存储桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("阿里云OSS创建存储桶失败", e);
            throw new StorageException("阿里云OSS创建存储桶失败: " + e.getMessage(), e);
        }
    }
} 