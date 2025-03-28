package online.yueyun.storage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * 文件存储服务接口
 * 定义了文件存储的基本操作，支持多种存储实现方式
 * 
 * @author yueyun
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param file 文件对象
     * @param bucketName 存储桶名称
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String bucketName);

    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param originalFilename 原始文件名
     * @param bucketName 存储桶名称
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String originalFilename, String bucketName);

    /**
     * 上传文件
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
     * 获取文件访问URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expiry 有效期（秒）
     * @return 文件访问URL
     */
    String getFileUrl(String bucketName, String objectName, int expiry);

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     */
    void ensureBucketExists(String bucketName);
} 