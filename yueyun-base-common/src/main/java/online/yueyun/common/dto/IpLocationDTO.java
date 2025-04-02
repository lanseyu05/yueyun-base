package online.yueyun.common.dto;

import lombok.Data;

/**
 * IP地址DTO
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
public class IpLocationDTO {
    /**
     * 起始IP
     */
    private String startIp;
    
    /**
     * 结束IP
     */
    private String endIp;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 区县
     */
    private String district;
    
    /**
     * 行政区划编码
     */
    private String adCode;
} 