package online.yueyun.ip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IP信息
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpInfo {

    /**
     * IP地址
     */
    private String ip;

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
     * 运营商
     */
    private String isp;

    /**
     * 地区代码
     */
    private String areaCode;

    /**
     * 原始信息
     */
    private String rawRegion;
}