package com.dingchunru.app.tool;

import android.text.TextUtils;
import com.dingchunru.app.data.Notes;
import javax.crypto.SecretKey;

/**
 * 便签加解密统一入口。
 *
 * snippet 和 content 本是同一份数据，触发器会自动同步。
 * 所以两者用同一个 AAD（noteId）加密，共用一份密文，
 * 不需要"先写 data 再覆盖 note"那套绕路。
 */
public class NoteCryptoHelper {

    public static final int NOT_ENCRYPTED = 0;
    public static final int ENCRYPTED     = 1;

    /** 统一 AAD：就用 noteId */
    private static String aad(long noteId) {
        return String.valueOf(noteId);
    }

    // ---------- 加密 ----------
    /** 加密正文，用于写 data.content */
    public static String encryptContent(long noteId, String plain) {
        SecretKey key = KeyManager.getSessionKey();
        if (key == null || plain == null) return null;
        try {
            return CryptoUtils.encrypt(plain, key, aad(noteId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ---------- 解密 ----------
    /** 解密 snippet 或 content，AAD 统一 */
    public static String decrypt(long noteId, String encrypted, int isEncrypted) {
        if (isEncrypted != ENCRYPTED || TextUtils.isEmpty(encrypted)) {
            return encrypted;
        }
        SecretKey key = KeyManager.getSessionKey();
        if (key == null) return null;   // null 表示"未解锁"

        try {
            return CryptoUtils.decrypt(encrypted, key, aad(noteId));
        } catch (Exception e) {
            e.printStackTrace();
            return "[解密失败]";
        }
    }

    public static boolean isLocked() {
        return KeyManager.getSessionKey() == null;
    }
}