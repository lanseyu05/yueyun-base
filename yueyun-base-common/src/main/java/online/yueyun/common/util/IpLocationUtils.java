package online.yueyun.common.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP地址库工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class IpLocationUtils {
    /**
     * IP地址库文件路径
     */
    private static final String IP_LOCATION_FILE = "ip/ip_location.txt";
    
    /**
     * IP地址库缓存
     */
    private static final Map<String, IpLocation> IP_LOCATION_CACHE = new ConcurrentHashMap<>();
    
    /**
     * IP地址库数据
     */
    private static final List<IpLocation> IP_LOCATION_LIST = new ArrayList<>();

    static {
        loadIpLocationData();
    }

    /**
     * 加载IP地址库数据
     */
    private static void loadIpLocationData() {
        try {
            ClassPathResource resource = new ClassPathResource(IP_LOCATION_FILE);
            loadIpLocationDataFromFile(resource.getInputStream());
        } catch (IOException e) {
            log.error("加载IP地址库失败", e);
        }
    }

    /**
     * 从文件加载IP地址库数据
     *
     * @param file 文件
     */
    public static void reloadIpLocationData(File file) {
        try {
            loadIpLocationDataFromFile(new java.io.FileInputStream(file));
        } catch (IOException e) {
            log.error("重新加载IP地址库失败", e);
        }
    }

    /**
     * 从文件路径加载IP地址库数据
     *
     * @param filePath 文件路径
     */
    public static void reloadIpLocationData(String filePath) {
        try {
            loadIpLocationDataFromFile(new java.io.FileInputStream(filePath));
        } catch (IOException e) {
            log.error("重新加载IP地址库失败", e);
        }
    }

    /**
     * 从输入流加载IP地址库数据
     *
     * @param inputStream 输入流
     */
    private static void loadIpLocationDataFromFile(java.io.InputStream inputStream) throws IOException {
        // 清空现有数据
        IP_LOCATION_CACHE.clear();
        IP_LOCATION_LIST.clear();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (StringUtils.hasText(line)) {
                    String[] parts = line.split("\t");
                    if (parts.length >= 7) {
                        IpLocation location = new IpLocation();
                        location.setStartIp(parts[0]);
                        location.setEndIp(parts[1]);
                        location.setCountry(parts[2]);
                        location.setProvince(parts[3]);
                        location.setCity(parts[4]);
                        location.setDistrict(parts[5]);
                        location.setAdCode(parts[6]);
                        IP_LOCATION_LIST.add(location);
                    }
                }
            }
        }
        log.info("IP地址库加载完成，共{}条记录", IP_LOCATION_LIST.size());
    }

    /**
     * 获取IP地理位置信息
     *
     * @param ip IP地址
     * @return 地理位置信息
     */
    public static IpLocation getLocation(String ip) {
        if (!StringUtils.hasText(ip)) {
            return null;
        }

        // 先从缓存中获取
        IpLocation location = IP_LOCATION_CACHE.get(ip);
        if (location != null) {
            return location;
        }

        // 二分查找IP地址库
        long ipLong = ipToLong(ip);
        int low = 0;
        int high = IP_LOCATION_LIST.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            IpLocation midLocation = IP_LOCATION_LIST.get(mid);
            long startIp = ipToLong(midLocation.getStartIp());
            long endIp = ipToLong(midLocation.getEndIp());

            if (ipLong >= startIp && ipLong <= endIp) {
                // 找到匹配的IP段，缓存结果
                IP_LOCATION_CACHE.put(ip, midLocation);
                return midLocation;
            }

            if (ipLong < startIp) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        return null;
    }

    /**
     * IP地址转长整型
     *
     * @param ip IP地址
     * @return 长整型
     */
    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | Integer.parseInt(parts[i]);
        }
        return result;
    }

    /**
     * IP地理位置信息
     */
    @Data
    public static class IpLocation {
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