package online.yueyun.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 纯真IP地址库解析工具
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class QQWry {
    /**
     * 文件头长度
     */
    private static final int HEADER_LENGTH = 8;

    /**
     * 索引区长度
     */
    private static final int INDEX_LENGTH = 7;

    /**
     * 文件头
     */
    private final byte[] header;

    /**
     * 索引区
     */
    private final byte[] index;

    /**
     * 数据区
     */
    private final byte[] data;

    /**
     * 构造函数
     *
     * @param file IP地址库文件
     */
    public QQWry(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fc = fis.getChannel()) {
            // 读取文件头
            header = new byte[HEADER_LENGTH];
            fc.read(java.nio.ByteBuffer.wrap(header));

            // 读取索引区
            int indexLength = readInt(header, 0);
            int dataLength = readInt(header, 4);
            index = new byte[indexLength];
            data = new byte[dataLength];
            fc.read(java.nio.ByteBuffer.wrap(index));
            fc.read(java.nio.ByteBuffer.wrap(data));
        }
    }

    /**
     * 获取所有IP地址信息
     *
     * @return IP地址信息列表
     */
    public List<IPLocation> getAllIPLocations() {
        List<IPLocation> locations = new ArrayList<>();
        int indexLength = index.length;
        int offset = 0;

        while (offset < indexLength) {
            // 读取起始IP
            long startIp = readLong(index, offset);
            offset += 4;

            // 读取结束IP
            long endIp = readLong(index, offset);
            offset += 4;

            // 读取数据区偏移
            int dataOffset = readInt(index, offset);
            offset += 3;

            // 解析数据区
            IPLocation location = parseData(dataOffset);
            if (location != null) {
                location.setStartIp(longToIp(startIp));
                location.setEndIp(longToIp(endIp));
                locations.add(location);
            }
        }

        return locations;
    }

    /**
     * 解析数据区
     */
    private IPLocation parseData(int offset) {
        try {
            // 读取标志
            byte flag = data[offset];
            offset++;

            // 读取国家
            String country;
            if (flag == 1) {
                int countryOffset = readInt(data, offset);
                country = readString(countryOffset);
            } else {
                country = readString(offset);
            }

            // 读取省份
            String province = readString(offset + country.length() + 1);

            // 读取城市
            String city = readString(offset + country.length() + province.length() + 2);

            // 读取区县
            String district = readString(offset + country.length() + province.length() + city.length() + 3);

            // 读取行政区划编码
            String adCode = readString(offset + country.length() + province.length() + city.length() + district.length() + 4);

            IPLocation location = new IPLocation();
            location.setCountry(country);
            location.setProvince(province);
            location.setCity(city);
            location.setDistrict(district);
            location.setAdCode(adCode);
            return location;
        } catch (Exception e) {
            log.error("解析数据区失败", e);
            return null;
        }
    }

    /**
     * 读取字符串
     */
    private String readString(int offset) {
        try {
            int end = offset;
            while (end < data.length && data[end] != 0) {
                end++;
            }
            return new String(data, offset, end - offset, "GBK");
        } catch (Exception e) {
            log.error("读取字符串失败", e);
            return "";
        }
    }

    /**
     * 读取整数
     */
    private int readInt(byte[] bytes, int offset) {
        return java.nio.ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * 读取长整数
     */
    private long readLong(byte[] bytes, int offset) {
        return java.nio.ByteBuffer.wrap(bytes, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
    }

    /**
     * 长整数转IP地址
     */
    private String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }

    /**
     * IP地址信息
     */
    @Data
    public static class IPLocation {
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
} 