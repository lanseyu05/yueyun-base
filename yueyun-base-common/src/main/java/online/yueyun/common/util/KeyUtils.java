package online.yueyun.common.util;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 密钥工具类
 *
 * @author YueYun
 * @since 1.0.0
 */
@Slf4j
public class KeyUtils {

    static {
        // 注册 BouncyCastle 提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成ES256密钥对
     *
     * @param privateKeyPath 私钥文件路径
     * @param publicKeyPath  公钥文件路径
     */
    public static void generateKeyPair(String privateKeyPath, String publicKeyPath) {
        try {
            // 确保目录存在
            Path privateKeyDir = Paths.get(privateKeyPath).getParent();
            Path publicKeyDir = Paths.get(publicKeyPath).getParent();
            if (privateKeyDir != null) {
                Files.createDirectories(privateKeyDir);
            }
            if (publicKeyDir != null) {
                Files.createDirectories(publicKeyDir);
            }

            // 生成密钥对
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            KeyPair keyPair = generator.generateKeyPair();

            // 保存私钥
            try (FileWriter writer = new FileWriter(privateKeyPath)) {
                writer.write("-----BEGIN PRIVATE KEY-----\n");
                writer.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
                writer.write("\n-----END PRIVATE KEY-----");
            }

            // 保存公钥
            try (FileWriter writer = new FileWriter(publicKeyPath)) {
                writer.write("-----BEGIN PUBLIC KEY-----\n");
                writer.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
                writer.write("\n-----END PUBLIC KEY-----");
            }

            log.info("密钥对生成成功");
        } catch (Exception e) {
            log.error("生成密钥对失败", e);
            throw new RuntimeException("生成密钥对失败", e);
        }
    }

    /**
     * 从文件加载私钥
     *
     * @param privateKeyPath 私钥文件路径
     * @return 私钥
     */
    public static PrivateKey loadPrivateKey(String privateKeyPath) {
        try {
            String privateKeyPEM = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
            privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (Exception e) {
            log.error("加载私钥失败", e);
            throw new RuntimeException("加载私钥失败", e);
        }
    }

    /**
     * 从文件加载公钥
     *
     * @param publicKeyPath 公钥文件路径
     * @return 公钥
     */
    public static PublicKey loadPublicKey(String publicKeyPath) {
        try {
            String publicKeyPEM = new String(Files.readAllBytes(Paths.get(publicKeyPath)));
            publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (Exception e) {
            log.error("加载公钥失败", e);
            throw new RuntimeException("加载公钥失败", e);
        }
    }
} 