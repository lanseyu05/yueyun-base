package online.yueyun.common.result;

/**
 * 返回码接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface IResultCode {

    /**
     * 获取状态码
     *
     * @return 状态码
     */
    Integer getCode();

    /**
     * 获取返回消息
     *
     * @return 返回消息
     */
    String getMessage();
} 