package online.yueyun.storage.service.impl;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.config.StorageProperties;
import online.yueyun.storage.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * 本地存储服务实现类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    private final StorageProperties properties;
    private final Path rootLocation;

    public StorageServiceImpl(StorageProperties properties) {
        this.properties = properties;
        this.rootLocation = Paths.get(properties.getLocal().getPath());
        init();
        log.info("初始化存储服务，存储类型: {}", properties.getType());
    }

    /**
     * 初始化存储目录
     */
    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("文件为空");
            }

            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("文件名不能为空");
            }

            // 构建目标路径
            Path targetPath = rootLocation.resolve(bucketName).resolve(originalFilename);
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回文件访问URL
            return getFileUrl(bucketName, originalFilename, 0);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName) {
        try {
            if (inputStream == null) {
                throw new RuntimeException("文件流为空");
            }

            if (originalFilename == null) {
                throw new RuntimeException("文件名不能为空");
            }

            // 构建目标路径
            Path targetPath = rootLocation.resolve(bucketName).resolve(originalFilename);
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回文件访问URL
            return getFileUrl(bucketName, originalFilename, 0);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String originalFilename, String bucketName, Map<String, String> metadata) {
        // 本地存储不支持元数据，直接调用无元数据的方法
        return uploadFile(inputStream, originalFilename, bucketName);
    }

    @Override
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            Path targetPath = rootLocation.resolve(bucketName).resolve(objectName);
            return Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName, int expiry) {
        // 本地存储不支持过期时间，直接返回相对路径
        return "/storage/" + bucketName + "/" + objectName;
    }

    @Override
    public void ensureBucketExists(String bucketName) {
        try {
            Path bucketPath = rootLocation.resolve(bucketName);
            Files.createDirectories(bucketPath);
        } catch (IOException e) {
            throw new RuntimeException("创建存储桶失败", e);
        }
    }
} 