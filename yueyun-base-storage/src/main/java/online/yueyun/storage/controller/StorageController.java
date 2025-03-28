package online.yueyun.storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.yueyun.storage.config.StorageProperties;
import online.yueyun.storage.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件存储控制器
 * 
 * @author yueyun
 */
@Slf4j
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;
    private final StorageProperties storageProperties;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return uploadToBucket(file, getBucketName());
    }
    
    /**
     * 上传文件到指定存储桶
     *
     * @param file 文件
     * @param bucketName 存储桶名称
     * @return 上传结果
     */
    @PostMapping("/upload/{bucketName}")
    public ResponseEntity<Map<String, Object>> uploadToBucket(
            @RequestParam("file") MultipartFile file,
            @PathVariable String bucketName) {
        
        try {
            // 上传文件并获取URL
            String fileUrl = storageService.uploadFile(file, bucketName);
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>(4);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("fileType", file.getContentType());
            result.put("fileUrl", fileUrl);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("error", "文件上传失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestParam("objectName") String objectName) {
        return deleteFromBucket(objectName, getBucketName());
    }
    
    /**
     * 从指定存储桶删除文件
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @return 删除结果
     */
    @DeleteMapping("/delete/{bucketName}")
    public ResponseEntity<Map<String, Object>> deleteFromBucket(
            @RequestParam("objectName") String objectName,
            @PathVariable String bucketName) {
        
        boolean deleted = storageService.deleteFile(bucketName, objectName);
        
        Map<String, Object> result = new HashMap<>(2);
        result.put("success", deleted);
        result.put("message", deleted ? "文件删除成功" : "文件删除失败");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取文件URL
     *
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 文件URL
     */
    @GetMapping("/url")
    public ResponseEntity<Map<String, Object>> getUrl(
            @RequestParam("objectName") String objectName,
            @RequestParam(value = "expiry", defaultValue = "3600") Integer expiry) {
        
        return getUrlFromBucket(objectName, getBucketName(), expiry);
    }
    
    /**
     * 从指定存储桶获取文件URL
     *
     * @param objectName 对象名称
     * @param bucketName 存储桶名称
     * @param expiry 过期时间（秒）
     * @return 文件URL
     */
    @GetMapping("/url/{bucketName}")
    public ResponseEntity<Map<String, Object>> getUrlFromBucket(
            @RequestParam("objectName") String objectName,
            @PathVariable String bucketName,
            @RequestParam(value = "expiry", defaultValue = "3600") Integer expiry) {
        
        try {
            String fileUrl = storageService.getFileUrl(bucketName, objectName, expiry);
            
            Map<String, Object> result = new HashMap<>(2);
            result.put("fileUrl", fileUrl);
            result.put("expiry", expiry);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            Map<String, Object> error = new HashMap<>(2);
            error.put("error", "获取文件URL失败");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 获取默认存储桶名称
     *
     * @return 存储桶名称
     */
    private String getBucketName() {
        // 根据当前存储类型获取默认桶名
        String type = storageProperties.getType();
        return switch (type) {
            case "minio" -> storageProperties.getMinio().getDefaultBucketName();
            case "aliyun-oss" -> storageProperties.getAliyunOss().getDefaultBucketName();
            default -> "default";
        };
    }
} 