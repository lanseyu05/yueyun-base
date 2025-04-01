package online.yueyun.storage.service.impl;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.config.StorageProperties;
import online.yueyun.storage.enums.StorageTypeEnum;
import online.yueyun.storage.service.StorageProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MinIO存储服务提供者实现
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class MinioStorageProvider implements StorageProvider {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    public MinioStorageProvider(StorageProperties properties) {
        this.properties = properties;
        this.minioClient = MinioClient.builder()
                .endpoint(properties.getMinio().getEndpoint())
                .credentials(properties.getMinio().getAccessKey(), properties.getMinio().getSecretKey())
                .build();
        log.info("初始化MinIO存储服务，端点: {}", properties.getMinio().getEndpoint());
    }

    @Override
    public String getType() {
        return StorageTypeEnum.MINIO.getCode();
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        try {
            ensureBucketExists(bucketName);
            String objectName = generateObjectName(file.getOriginalFilename());
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getFileUrl(bucketName, objectName, 0);
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName) {
        try {
            ensureBucketExists(bucketName);
            String objectName = generateObjectName(originalFilename);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, -1, -1)
                    .build());
            return getFileUrl(bucketName, objectName, 0);
        } catch (Exception e) {
            log.error("上传文件流失败", e);
            throw new RuntimeException("上传文件流失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName, Map<String, String> metadata) {
        try {
            ensureBucketExists(bucketName);
            String objectName = generateObjectName(originalFilename);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, -1, -1)
                    .headers(metadata)
                    .build());
            return getFileUrl(bucketName, objectName, 0);
        } catch (Exception e) {
            log.error("上传文件流失败", e);
            throw new RuntimeException("上传文件流失败", e);
        }
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName, int expiry) {
        try {
            if (expiry > 0) {
                return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .method(Method.GET)
                        .expiry(expiry, TimeUnit.SECONDS)
                        .build());
            } else {
                return String.format("%s/%s/%s", properties.getMinio().getEndpoint(), bucketName, objectName);
            }
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("创建存储桶: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查/创建存储桶失败", e);
            throw new RuntimeException("检查/创建存储桶失败", e);
        }
    }

    /**
     * 生成对象名称
     *
     * @param originalFilename 原始文件名
     * @return 对象名称
     */
    private String generateObjectName(String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().replace("-", "") + extension;
    }
} 