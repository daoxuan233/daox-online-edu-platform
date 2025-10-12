package com.daox.online.uilts;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * 邮箱验证码生成器类
 */
@Component
public class EmailVerificationCodeGenerator {
    // 定义验证码的长度为 6 位
    private static final int CODE_LENGTH = 6;
    // 定义验证码允许使用的字符集合，包含大小写字母和数字
    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    // 使用 SecureRandom 类来生成安全的随机数，增强随机性和安全性
    private static final SecureRandom random = new SecureRandom();

    // 生成邮箱验证码的主要方法
    public static String generateVerificationCode() {
        // 创建一个 StringBuilder 对象，用于构建基础的随机验证码
        StringBuilder baseCode = new StringBuilder();
        // 循环 CODE_LENGTH 次，每次从 ALLOWED_CHARS 中随机选取一个字符添加到 baseCode 中
        for (int i = 0; i < CODE_LENGTH; i++) {
            // 生成一个 0 到 ALLOWED_CHARS 长度之间的随机整数
            int randomIndex = random.nextInt(ALLOWED_CHARS.length());
            // 根据随机索引从 ALLOWED_CHARS 中取出字符并添加到 baseCode 中
            baseCode.append(ALLOWED_CHARS.charAt(randomIndex));
        }

        // 获取当前的日期和时间，并按照指定的格式转换为字符串
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        // 调用 generateUniqueKey 方法生成一个唯一的密钥
        String uniqueKey = generateUniqueKey();

        // 将基础验证码、时间戳和唯一密钥组合成一个字符串，增加验证码的复杂度
        String combined = baseCode + timestamp + uniqueKey;
        try {
            // 获取 SHA-256 哈希算法的实例
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 将组合后的字符串转换为字节数组，并使用 SHA-256 算法进行哈希计算
            byte[] encodedHash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            // 使用 Base64 对哈希结果进行编码，将字节数组转换为字符串
            String hashed = Base64.getUrlEncoder().withoutPadding().encodeToString(encodedHash);
            // 从编码后的字符串中截取前 CODE_LENGTH 位作为最终的验证码
            return hashed.substring(0, CODE_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // 如果指定的哈希算法（SHA-256）不可用，抛出运行时异常
            throw new RuntimeException(e);
        }
    }

    // 生成唯一密钥的方法
    private static String generateUniqueKey() {
        // 创建一个长度为 16 字节的字节数组，用于存储随机生成的密钥
        byte[] keyBytes = new byte[16];
        // 使用 SecureRandom 类向字节数组中填充随机字节
        random.nextBytes(keyBytes);
        // 使用 Base64 对字节数组进行编码，并去除填充字符，将其转换为字符串
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }
}
