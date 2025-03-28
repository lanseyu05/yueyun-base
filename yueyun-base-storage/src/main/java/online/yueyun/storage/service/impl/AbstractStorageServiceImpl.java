package online.yueyun.storage.service.impl;

import online.yueyun.storage.exception.StorageException;
import online.yueyun.storage.service.StorageService;
import online.yueyun.storage.util.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象存储服务实现
 * 提供公共的存储服务实现逻辑
 * 
 * @author yueyun
 */
public abstract class AbstractStorageServiceImpl implements StorageService {

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("上传文件不能为空");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            String originalFilename = file.getOriginalFilename();
            Map<String, String> metadata = new HashMap<>(4);
            metadata.put("contentType", file.getContentType());
            metadata.put("size", String.valueOf(file.getSize()));

            return uploadFile(inputStream, originalFilename, bucketName, metadata);
        } catch (IOException e) {
            throw new StorageException("读取上传文件失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName) {
        return uploadFile(inputStream, originalFilename, bucketName, new HashMap<>(0));
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName, Map<String, String> metadata) {
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        
        // 生成对象名
        String objectName = FileUtil.generateObjectName(originalFilename);
        
        // 获取文件内容类型
        String contentType = FileUtil.getContentType(originalFilename);
        
        // 补充元数据
        if (!metadata.containsKey("contentType")) {
            metadata.put("contentType", contentType);
        }
        
        return doUploadFile(inputStream, objectName, bucketName, metadata);
    }
    
    /**
     * 实际执行文件上传的方法，由子类实现
     *
     * @param inputStream 文件输入流
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @param metadata 元数据
     * @return 文件访问URL
     */
    protected abstract String doUploadFile(InputStream inputStream, String objectName, String bucketName, Map<String, String> metadata);
} 