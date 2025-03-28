package online.yueyun.storage.config;

import lombok.Data;
import online.yueyun.storage.enums.StorageTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 存储配置属性
 * 
 * @author yueyun
 */
@Data
@ConfigurationProperties(prefix = "yueyun.storage")
public class StorageProperties {

    /**
     * 存储类型，默认为MinIO
     */
    private String type = StorageTypeEnum.MINIO.getCode();
    
    /**
     * MinIO配置
     */
    private final MinioConfig minio = new MinioConfig();
    
    /**
     * 阿里云OSS配置
     */
    private final AliyunOssConfig aliyunOss = new AliyunOssConfig();
    
    /**
     * MinIO配置
     */
    @Data
    public static class MinioConfig {
        /**
         * 服务地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKey;
        
        /**
         * 访问密钥密文
         */
        private String secretKey;
        
        /**
         * 默认存储桶
         */
        private String defaultBucketName = "default";
        
        /**
         * 连接超时（秒）
         */
        private Integer connectTimeout = 10;
        
        /**
         * 是否使用SSL
         */
        private boolean secure = false;
    }
    
    /**
     * 阿里云OSS配置
     */
    @Data
    public static class AliyunOssConfig {
        /**
         * 服务地址
         */
        private String endpoint;
        
        /**
         * 访问密钥
         */
        private String accessKeyId;
        
        /**
         * 访问密钥密文
         */
        private String accessKeySecret;
        
        /**
         * 默认存储桶
         */
        private String defaultBucketName = "default";
        
        /**
         * 连接超时（秒）
         */
        private Integer connectTimeout = 10;
    }
} 