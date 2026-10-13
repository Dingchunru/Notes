package com.dingchunru.app.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

/**
 * 密码 / salt / 派生密钥 的会话级管理器
 */
public class KeyManager {

    private static final String PREF_NAME  = "crypto_pref";
    private static final String KEY_SALT   = "salt_b64";
    private static final String KEY_HASH   = "pwd_hash_b64";

    /** 内存中缓存的派生密钥，App 退出即丢失 */
    private static SecretKey sSessionKey;

    // ---------- 初始化 ----------

    /** 是否已经设置过密码 */
    public static boolean isPasswordSet(Context ctx) {
        return prefs(ctx).contains(KEY_HASH);
    }

    /** 首次设置密码 */
    public static void setupPassword(Context ctx, String password) throws Exception {
        byte[] salt = CryptoUtils.generateSalt();
        SecretKey key = CryptoUtils.deriveKey(password, salt);

        prefs(ctx).edit()
                .putString(KEY_SALT, CryptoUtils.saltToBase64(salt))
                .putString(KEY_HASH, hashPassword(password, salt))
                .apply();

        sSessionKey = key;
    }

    /** 验证密码；成功则把派生密钥放入会话 */
    public static boolean unlock(Context ctx, String password) throws Exception {
        byte[] salt = CryptoUtils.saltFromBase64(
                prefs(ctx).getString(KEY_SALT, null));
        String saved = prefs(ctx).getString(KEY_HASH, null);
        if (saved == null) return false;

        if (!saved.equals(hashPassword(password, salt))) {
            return false;
        }
        sSessionKey = CryptoUtils.deriveKey(password, salt);
        return true;
    }

    /** 会话密钥（未解锁时为 null） */
    public static SecretKey getSessionKey() {
        return sSessionKey;
    }

    /** 主动锁定 */
    public static void lock() {
        sSessionKey = null;
    }

    // ---------- 修改密码：重加密全部便签见下文 ----------

    public static void updateSaltAndHash(Context ctx, String newPassword) throws Exception {
        byte[] salt = CryptoUtils.generateSalt();
        SecretKey key = CryptoUtils.deriveKey(newPassword, salt);
        prefs(ctx).edit()
                .putString(KEY_SALT, CryptoUtils.saltToBase64(salt))
                .putString(KEY_HASH, hashPassword(newPassword, salt))
                .apply();
        sSessionKey = key;
    }

    /** 用 PBKDF2 输出再 SHA-256 作为校验值，避免明文比对 */
    private static String hashPassword(String pwd, byte[] salt) throws Exception {
        SecretKey k = CryptoUtils.deriveKey(pwd, salt);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(k.getEncoded());
        return Base64.encodeToString(digest, Base64.NO_WRAP);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}