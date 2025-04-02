package online.yueyun.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IP地址库实体
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_ip_location", autoResultMap = true)
public class IpLocationEntity extends BaseEntity {
    /**
     * 起始IP
     */
    @TableField("start_ip")
    private String startIp;

    /**
     * 结束IP
     */
    @TableField("end_ip")
    private String endIp;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区县
     */
    @TableField("district")
    private String district;

    /**
     * 行政区划编码
     */
    @TableField("ad_code")
    private String adCode;
} 