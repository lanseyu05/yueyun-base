package online.yueyun.storage.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 * 
 * @author yueyun
 */
public class FileUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private FileUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取文件的MD5值
     *
     * @param file 文件对象
     * @return 文件MD5值
     */
    public static String getFileMd5(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 获取文件后缀名
     *
     * @param filename 文件名
     * @return 文件后缀
     */
    public static String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    /**
     * 生成文件存储路径
     *
     * @param originalFilename 原始文件名
     * @return 文件存储路径
     */
    public static String generateObjectName(String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(DATE_FORMATTER);
        String time = now.format(TIME_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        String extension = getFileExtension(originalFilename);
        if (StringUtils.isNotBlank(extension)) {
            return String.format("%s/%s/%s.%s", date, time, uuid, extension);
        } else {
            return String.format("%s/%s/%s", date, time, uuid);
        }
    }

    /**
     * 获取文件内容类型
     *
     * @param filename 文件名
     * @return 内容类型
     */
    public static String getContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }
} 