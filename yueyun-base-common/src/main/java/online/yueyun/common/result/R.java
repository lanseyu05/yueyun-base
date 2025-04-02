package online.yueyun.common.result;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @author YueYun
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok() {
        return restResult(null, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), true);
    }

    /**
     * 成功返回结果
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok(T data) {
        return restResult(data, ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), true);
    }

    /**
     * 成功返回结果
     *
     * @param data    返回数据
     * @param message 返回消息
     * @param <T>     数据类型
     * @return 返回结果
     */
    public static <T> R<T> ok(T data, String message) {
        return restResult(data, ResultCode.SUCCESS.getCode(), message, true);
    }

    /**
     * 失败返回结果
     *
     * @param <T> 数据类型
     * @return 返回结果
     */
    public static <T> R<T> failed() {
        return restResult(null, ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(), false);
    }

    /**
     * 失败返回结果
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 返回结果
     */
    public static <T> R<T> failed(String message) {
        return restResult(null, ResultCode.FAILED.getCode(), message, false);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param <T>       数据类型
     * @return 返回结果
     */
    public static <T> R<T> failed(IResultCode errorCode) {
        return restResult(null, errorCode.getCode(), errorCode.getMessage(), false);
    }
    
    /**
     * 失败返回结果
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 返回结果
     */
    public static <T> R<T> failed(Integer code, String message) {
        return restResult(null, code, message, false);
    }

    /**
     * 构造返回结果
     *
     * @param data    返回数据
     * @param code    状态码
     * @param message 返回消息
     * @param success 是否成功
     * @param <T>     数据类型
     * @return 返回结果
     */
    private static <T> R<T> restResult(T data, Integer code, String message, Boolean success) {
        R<T> r = new R<>();
        r.setData(data);
        r.setCode(code);
        r.setMessage(message);
        r.setSuccess(success);
        return r;
    }
} 