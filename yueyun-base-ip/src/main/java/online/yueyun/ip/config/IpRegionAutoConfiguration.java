package online.yueyun.ip.config;


import lombok.extern.slf4j.Slf4j;
import online.yueyun.ip.service.IpRegionService;
import online.yueyun.ip.service.impl.Ip2RegionServiceImpl;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IP地址检索自动配置类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(IpRegionProperties.class)
@ConditionalOnProperty(prefix = "yueyun.ip", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IpRegionAutoConfiguration {

    /**
     * IP地址检索器
     *
     * @param properties 配置属性
     * @return 检索器
     * @throws IOException IO异常
     */
    @Bean
    @ConditionalOnMissingBean
    public Searcher ipSearcher(IpRegionProperties properties) throws IOException {
        byte[] dbBytes;
        String dbPath = properties.getDbPath();
        if (StringUtils.hasText(dbPath)) {
            // 使用指定的数据库文件
            log.info("使用指定IP数据库: {}", dbPath);
            Path path = Paths.get(dbPath);
            if (!Files.exists(path)) {
                throw new IOException("IP数据库文件不存在: " + dbPath);
            }
            dbBytes = Files.readAllBytes(path);
        } else {
            // 使用内置数据库文件
            log.info("使用内置IP数据库");
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            dbBytes = resource.getInputStream().readAllBytes();
        }

        // 创建缓存配置
        String cacheType = properties.getCacheType();
        if ("memory".equalsIgnoreCase(cacheType)) {
            log.info("使用内存缓存模式");
            return Searcher.newWithBuffer(dbBytes);
        } else {
            log.info("使用文件缓存模式");
            String tmpPath = System.getProperty("java.io.tmpdir") + "/ip2region.xdb";
            Path path = Paths.get(tmpPath);
            Files.write(path, dbBytes);
            return Searcher.newWithFile(tmpPath);
        }
    }

    /**
     * IP地址缓存
     *
     * @param properties 配置属性
     * @return 缓存
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "yueyun.ip", name = "enable-cache", havingValue = "true", matchIfMissing = true)
    public LRUCache<String, String> ipCache(IpRegionProperties properties) {
        log.info("初始化IP地址缓存，大小: {}, 过期时间: {}秒", properties.getCacheSize(), properties.getCacheExpire());
        return CacheUtil.newLRUCache(properties.getCacheSize(), properties.getCacheExpire());
    }

    /**
     * IP地址检索服务
     *
     * @param searcher 检索器
     * @param ipCache 缓存
     * @param properties 配置属性
     * @return 服务
     */
    @Bean
    @ConditionalOnMissingBean
    public IpRegionService ipRegionService(Searcher searcher, LRUCache<String, String> ipCache, IpRegionProperties properties) {
        log.info("初始化IP地址检索服务");
        return new Ip2RegionServiceImpl(searcher, ipCache, properties);
    }
} 