package online.yueyun.excel.listener;

import java.util.List;

/**
 * Excel读取监听器接口
 *
 * @author YueYun
 * @since 1.0.0
 */
public interface ReadListener<T> {

    /**
     * 处理一批数据
     *
     * @param dataList 数据列表
     */
    void invoke(List<T> dataList);

    /**
     * 读取完成回调
     */
    default void doAfterAll() {
        // 默认空实现
    }

    /**
     * 读取出错回调
     *
     * @param exception 异常信息
     * @param context 上下文
     * @throws Exception 继续向上抛出异常
     */
    default void onException(Exception exception, ReadContext context) throws Exception {
        throw exception;
    }

    /**
     * 读取上下文
     */
    interface ReadContext {
        /**
         * 获取当前行号
         *
         * @return 当前行号
         */
        int getRowIndex();

        /**
         * 获取当前Sheet索引
         *
         * @return 当前Sheet索引
         */
        int getSheetIndex();

        /**
         * 获取当前Sheet名称
         *
         * @return 当前Sheet名称
         */
        String getSheetName();

        /**
         * 获取总行数（可能不准确）
         *
         * @return 总行数
         */
        Integer getTotalRows();
    }
} 