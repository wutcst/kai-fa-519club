package cn.edu.whut.sept.zuul.infrastructure.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import cn.edu.whut.sept.zuul.infrastructure.persistence.PersistenceException;

/**
 * 密码加盐哈希（SHA-256），供 H2 用户表存储。
 */
public final class PasswordHasher {

    private static final int SALT_BYTES = 16;

    private PasswordHasher() {
    }

    /**
     * 生成存储格式：Base64(salt):Base64(hash)。
     *
     * @param rawPassword 明文密码
     * @return 哈希串
     */
    public static String hashPassword(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = digest(rawPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 校验明文密码是否匹配存储哈希。
     *
     * @param rawPassword 明文密码
     * @param storedHash 存储哈希
     * @return 匹配返回 true
     */
    public static boolean matches(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        byte[] actual = digest(rawPassword, salt);
        if (expected.length != actual.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length; i++) {
            diff |= expected[i] ^ actual[i];
        }
        return diff == 0;
    }

    private static byte[] digest(String rawPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new PersistenceException("密码哈希算法不可用", exception);
        }
    }
}
