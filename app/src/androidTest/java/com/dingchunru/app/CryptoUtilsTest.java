package com.dingchunru.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.crypto.SecretKey;

import com.dingchunru.app.tool.CryptoUtils;

@RunWith(AndroidJUnit4.class)
public class CryptoUtilsTest {

    private SecretKey key;
    private byte[] salt;

    @Before
    public void setUp() throws Exception {
        salt = CryptoUtils.generateSalt();
        key = CryptoUtils.deriveKey("test-password-123", salt);
    }

    @Test
    public void testEncryptDecrypt() throws Exception {
        String plain = "hello world 你好";

        String cipher = CryptoUtils.encrypt(plain, key, null);
        assertNotNull(cipher);
        assertNotEquals(plain, cipher);

        String decrypted = CryptoUtils.decrypt(cipher, key, null);
        assertEquals(plain, decrypted);
    }

    @Test
    public void testNullInput() throws Exception {
        assertNull(CryptoUtils.encrypt(null, key, null));
        assertNull(CryptoUtils.decrypt(null, key, null));
    }

    @Test
    public void testEmptyString() throws Exception {
        String plain = "";
        String cipher = CryptoUtils.encrypt(plain, key, null);
        assertEquals(plain, CryptoUtils.decrypt(cipher, key, null));
    }

    @Test
    public void testLongText() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append("abc中文");
        String plain = sb.toString();

        String cipher = CryptoUtils.encrypt(plain, key, null);
        assertEquals(plain, CryptoUtils.decrypt(cipher, key, null));
    }

    @Test
    public void testSameInputDifferentCipher() throws Exception {
        String plain = "same content";
        String c1 = CryptoUtils.encrypt(plain, key, null);
        String c2 = CryptoUtils.encrypt(plain, key, null);
        assertNotEquals(c1, c2);
        assertEquals(plain, CryptoUtils.decrypt(c1, key, null));
        assertEquals(plain, CryptoUtils.decrypt(c2, key, null));
    }

    @Test
    public void testWrongKeyFails() throws Exception {
        String plain = "secret";
        String cipher = CryptoUtils.encrypt(plain, key, null);

        SecretKey wrongKey = CryptoUtils.deriveKey("wrong-password", salt);
        try {
            CryptoUtils.decrypt(cipher, wrongKey, null);
            throw new AssertionError("应该抛异常但没有");
        } catch (Exception e) {
            System.out.println("预期异常: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testTamperedCipherFails() throws Exception {
        String plain = "secret";
        String cipher = CryptoUtils.encrypt(plain, key, null);

        char[] chars = cipher.toCharArray();
        chars[chars.length / 2] = (chars[chars.length / 2] == 'A') ? 'B' : 'A';
        String tampered = new String(chars);

        try {
            CryptoUtils.decrypt(tampered, key, null);
            throw new AssertionError("篡改后应该抛异常");
        } catch (Exception e) {
            System.out.println("预期异常: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testSaltRoundTrip() {
        byte[] salt = CryptoUtils.generateSalt();
        String b64 = CryptoUtils.saltToBase64(salt);
        byte[] restored = CryptoUtils.saltFromBase64(b64);
        assertEquals(salt.length, restored.length);
        for (int i = 0; i < salt.length; i++) {
            assertEquals(salt[i], restored[i]);
        }
    }
}