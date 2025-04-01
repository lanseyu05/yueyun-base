package online.yueyun.storage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 存储服务配置属性
 */
@Data
@ConfigurationProperties(prefix = "yueyun.storage")
public class StorageProperties {
    /**
     * 存储类型：local, minio, oss, cos
     */
    private String type = "local";

    /**
     * 本地存储配置
     */
    private Local local = new Local();

    /**
     * MinIO存储配置
     */
    private Minio minio = new Minio();

    /**
     * 阿里云OSS配置
     */
    private Oss oss = new Oss();

    /**
     * 腾讯云COS配置
     */
    private Cos cos = new Cos();

    /**
     * 本地存储配置
     */
    @Data
    public static class Local {
        /**
         * 存储路径
         */
        private String path = "upload";
    }

    /**
     * MinIO存储配置
     */
    @Data
    public static class Minio {
        /**
         * 服务地址
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 访问密钥
         */
        private String secretKey;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }

    /**
     * 阿里云OSS配置
     */
    @Data
    public static class Oss {
        /**
         * 服务地址
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 访问密钥
         */
        private String secretKey;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }

    /**
     * 腾讯云COS配置
     */
    @Data
    public static class Cos {
        /**
         * 服务地址
         */
        private String endpoint;

        /**
         * 访问密钥
         */
        private String accessKey;

        /**
         * 访问密钥
         */
        private String secretKey;

        /**
         * 存储桶名称
         */
        private String bucketName;
    }
} 