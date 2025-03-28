package online.yueyun.storage.enums;

import lombok.Getter;

/**
 * 存储类型枚举
 * 
 * @author yueyun
 */
@Getter
public enum StorageTypeEnum {
    
    /**
     * MinIO对象存储
     */
    MINIO("minio", "MinIO对象存储"),
    
    /**
     * 阿里云OSS对象存储
     */
    ALIYUN_OSS("aliyun-oss", "阿里云OSS对象存储");
    
    /**
     * 存储类型编码
     */
    private final String code;
    
    /**
     * 存储类型描述
     */
    private final String desc;
    
    StorageTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据编码获取存储类型
     *
     * @param code 存储类型编码
     * @return 存储类型枚举
     */
    public static StorageTypeEnum getByCode(String code) {
        for (StorageTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return MINIO; // 默认使用MinIO
    }
} 