package online.yueyun.datapermission.enums;

/**
 * 数据权限类型枚举
 *
 * @author YueYun
 * @since 1.0.0
 */
public enum DataPermissionTypeEnum {
    
    /**
     * 全部数据
     */
    ALL("all", "全部数据"),
    
    /**
     * 仅本人数据
     */
    SELF("self", "仅本人数据"),
    
    /**
     * 自定义数据
     */
    CUSTOM("custom", "自定义数据");

    private final String code;
    private final String desc;

    DataPermissionTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static DataPermissionTypeEnum getByCode(String code) {
        for (DataPermissionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 