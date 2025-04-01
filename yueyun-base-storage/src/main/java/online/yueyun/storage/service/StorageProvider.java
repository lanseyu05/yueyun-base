package online.yueyun.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 存储服务提供者接口
 * 用于扩展不同的存储实现，如MinIO、阿里云OSS等
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface StorageProvider {
    
    /**
     * 获取存储类型
     *
     * @return 存储类型
     */
    String getType();

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param bucketName 存储桶名称
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String bucketName);

    /**
     * 上传文件流
     *
     * @param inputStream 文件输入流
     * @param originalFilename 原始文件名
     * @param bucketName 存储桶名称
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String originalFilename, String bucketName);

    /**
     * 上传文件流（带元数据）
     *
     * @param inputStream 文件输入流
     * @param originalFilename 原始文件名
     * @param bucketName 存储桶名称
     * @param metadata 元数据
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String originalFilename, String bucketName, Map<String, String> metadata);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    boolean deleteFile(String bucketName, String objectName);

    /**
     * 获取文件URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 有效期（秒）
     * @return 文件访问URL
     */
    String getFileUrl(String bucketName, String objectName, int expiry);

    /**
     * 确保存储桶存在
     *
     * @param bucketName 存储桶名称
     */
    void ensureBucketExists(String bucketName);
} 