package online.yueyun.storage.service.impl;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.config.StorageProperties;
import online.yueyun.storage.exception.StorageException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储服务实现
 * 
 * @author yueyun
 */
@Slf4j
@Service("minioStorageService")
public class MinioStorageServiceImpl extends AbstractStorageServiceImpl {

    private final MinioClient minioClient;
    private final StorageProperties.Minio minioConfig;

    public MinioStorageServiceImpl(StorageProperties storageProperties) {
        if (storageProperties == null || storageProperties.getMinio() == null) {
            throw new IllegalArgumentException("MinIO配置不能为空");
        }
        this.minioConfig = storageProperties.getMinio();
        this.minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        log.info("初始化MinIO存储服务，端点: {}", minioConfig.getEndpoint());
    }

    @Override
    protected String doUploadFile(InputStream inputStream, String objectName, String bucketName, Map<String, String> metadata) {
        try {
            // 构建PutObjectArgs参数
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .contentType(metadata.getOrDefault("contentType", "application/octet-stream"))
                    .stream(inputStream, -1, 10485760) // 10MB分块
                    .headers(metadata)
                    .build();

            // 上传文件
            minioClient.putObject(putObjectArgs);

            // 返回文件URL
            return getFileUrl(bucketName, objectName, 7 * 24 * 60 * 60); // 默认7天有效期
        } catch (Exception e) {
            log.error("MinIO上传文件失败", e);
            throw new StorageException("MinIO上传文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;
        } catch (Exception e) {
            log.error("MinIO删除文件失败", e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName, int expiry) {
        try {
            if (expiry > 0) {
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .method(Method.GET)
                                .expiry(expiry, TimeUnit.SECONDS)
                                .build());
            } else {
                return String.format("%s/%s/%s", minioConfig.getEndpoint(), bucketName, objectName);
            }
        } catch (Exception e) {
            log.error("MinIO获取文件URL失败", e);
            throw new StorageException("MinIO获取文件URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
                log.info("创建MinIO存储桶成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO创建存储桶失败", e);
            throw new StorageException("MinIO创建存储桶失败: " + e.getMessage(), e);
        }
    }
} 