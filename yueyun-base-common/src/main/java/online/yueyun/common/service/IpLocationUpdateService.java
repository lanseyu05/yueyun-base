package online.yueyun.common.service;

import lombok.extern.slf4j.Slf4j;
import online.yueyun.common.config.CommonProperties;
import online.yueyun.common.converter.IpLocationConverter;
import online.yueyun.common.dto.IpLocationDTO;
import online.yueyun.common.util.QQWry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * IP地址库更新服务
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
@Service
public class IpLocationUpdateService {

    @Autowired
    private CommonProperties properties;

    @Autowired
    private IpLocationService ipLocationService;

    @Autowired
    private IpLocationConverter ipLocationConverter;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 纯真IP地址库下载地址
     */
    private static final String QQWRY_DAT_URL = "https://update.cz88.net/ip/qqwry.rar";

    /**
     * 临时文件目录
     */
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 每天凌晨2点更新IP地址库
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateIpLocation() {
        if (!properties.getAmap().isEnabled()) {
            log.warn("IP地理位置服务未启用，跳过更新");
            return;
        }

        log.info("开始更新IP地址库 - {}", LocalDateTime.now().format(DATE_FORMATTER));
        try {
            // 下载IP地址库文件
            String rarFile = downloadIpLocationFile();
            if (rarFile == null) {
                return;
            }

            // 解压并处理IP地址库文件
            String datFile = extractIpLocationFile(rarFile);
            if (datFile == null) {
                return;
            }

            // 解析IP地址库文件
            List<QQWry.IPLocation> locations = parseIpLocationFile(datFile);
            if (locations == null || locations.isEmpty()) {
                return;
            }

            // 转换为DTO
            List<IpLocationDTO> dtos = ipLocationConverter.toDTOs(locations);
            log.info("成功转换{}条IP地址数据为DTO", dtos.size());

            // 更新数据库中的数据
            ipLocationService.updateIpLocationData(dtos);

            // 清理临时文件
            cleanupTempFiles(rarFile, datFile);

            log.info("IP地址库更新完成，共更新{}条记录 - {}", 
                dtos.size(), 
                LocalDateTime.now().format(DATE_FORMATTER));
        } catch (Exception e) {
            log.error("更新IP地址库失败", e);
        }
    }

    /**
     * 下载IP地址库文件
     */
    private String downloadIpLocationFile() {
        String rarFile = TEMP_DIR + "qqwry_" + System.currentTimeMillis() + ".rar";
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                QQWRY_DAT_URL,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                try (FileOutputStream out = new FileOutputStream(rarFile)) {
                    out.write(response.getBody());
                }
                return rarFile;
            }
        } catch (Exception e) {
            log.error("下载IP地址库文件失败", e);
        }
        return null;
    }

    /**
     * 解压IP地址库文件
     */
    private String extractIpLocationFile(String rarFile) {
        String datFile = TEMP_DIR + "qqwry_" + System.currentTimeMillis() + ".dat";
        try {
            // 使用7-Zip解压文件
            ProcessBuilder pb = new ProcessBuilder(
                "7z", "x", rarFile, "-o" + TEMP_DIR, "-y"
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 重命名解压后的文件
                File dat = new File(TEMP_DIR + "qqwry.dat");
                if (dat.exists()) {
                    dat.renameTo(new File(datFile));
                    return datFile;
                }
            }
        } catch (Exception e) {
            log.error("解压IP地址库文件失败", e);
        }
        return null;
    }

    /**
     * 解析IP地址库文件
     */
    private List<QQWry.IPLocation> parseIpLocationFile(String datFile) {
        try {
            // 使用纯真IP地址库解析工具解析文件
            QQWry qqwry = new QQWry(new File(datFile));
            return qqwry.getAllIPLocations();
        } catch (Exception e) {
            log.error("解析IP地址库文件失败", e);
        }
        return null;
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(String... files) {
        for (String file : files) {
            if (file != null) {
                try {
                    File f = new File(file);
                    if (f.exists()) {
                        f.delete();
                    }
                } catch (Exception e) {
                    log.warn("清理临时文件失败: {}", file, e);
                }
            }
        }
    }
} 