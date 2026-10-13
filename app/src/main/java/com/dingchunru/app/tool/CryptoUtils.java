package com.dingchunru.app.tool;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {
    private static final String TAG = "CryptoUtils";

    // AES/GCM 参数
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String AES_ALGO = "AES";
    private static final int GCM_IV_LENGTH = 12;   // GCM 推荐 12 字节 IV
    private static final int GCM_TAG_LENGTH = 128; // 认证标签 128 bit

    // PBKDF2 参数
    private static final String PBKDF2_ALGO = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 10000;
    private static final int AES_KEY_LENGTH = 256; // 256 bit 密钥
    private static final int SALT_LENGTH = 16;

    /**
     * 从用户密钥派生 AES 密钥
     */
    public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGO);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES_ALGO);
    }
    /**
     * 加密
     */
    public static String encrypt(String plain, SecretKey key) throws Exception{
        if (plain == null)
            return null;

        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[iv.length + cipherBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

        return Base64.encodeToString(combined, Base64.NO_WRAP);
    }

    /**
     * 解密
     */
    public static String decrypt(String encoded, SecretKey key) throws Exception {
        if (encoded == null) return null;

        byte[] combined = Base64.decode(encoded, Base64.NO_WRAP);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

        byte[] cipherBytes = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /**
     * 生成随机盐
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * salt 转 Base64
     */
    public static String saltToBase64(byte[] salt) {
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    /**
     * Base64 转 salt
     */
    public static byte[] saltFromBase64(String s) {
        return Base64.decode(s, Base64.NO_WRAP);
    }
}
