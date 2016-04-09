package com.example.guilhermecortes.contactmanager;

/**
 * Created by Will on 4/5/2016.
 */
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {
    public static String encrypt(final String unencryptedMessage, final String key) {
        final byte[] keyAsBytes = Base64.decode(key, Base64.DEFAULT);
        final byte[] messageAsBytes = unencryptedMessage.getBytes(Charset.forName("UTF-8"));

        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            final SecretKeySpec secretKeySpec = new SecretKeySpec(keyAsBytes, "AES");

            final byte[] ivData = new byte[blockSize];
            final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(ivData);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            final byte[] encryptedMessage = cipher.doFinal(messageAsBytes);

            final byte[] ivAndEncryptedMessage = new byte[ivData.length + encryptedMessage.length];
            System.arraycopy(ivData, 0, ivAndEncryptedMessage, 0, blockSize);
            System.arraycopy(encryptedMessage, 0, ivAndEncryptedMessage, blockSize, encryptedMessage.length);

            return Base64.encodeToString(ivAndEncryptedMessage, Base64.DEFAULT);
        }
        catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Key argument is not valid.");
        }
        catch (GeneralSecurityException e) {
            throw new IllegalStateException("Exception occurred during encryption.");
        }
    }

    public static String decrypt(final String ivAndEncryptedMessage, final String key) {
        final byte[] keyAsBytes = Base64.decode(key, Base64.DEFAULT);
        final byte[] ivAndEncryptedMessageAsBytes = ivAndEncryptedMessage.getBytes(Charset.forName("UTF-8"));

        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            final SecretKeySpec secretKeySpec = new SecretKeySpec(keyAsBytes, "AES");

            final byte[] ivData = new byte[blockSize];
            System.arraycopy(ivAndEncryptedMessageAsBytes, 0, ivData, 0, blockSize);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            final byte[] encryptedMessage = new byte[ivAndEncryptedMessageAsBytes.length - blockSize];
            System.arraycopy(ivAndEncryptedMessageAsBytes, blockSize, encryptedMessage, 0, encryptedMessage.length);

            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);

            final byte[] encodedMessage = cipher.doFinal(encryptedMessage);

            return new String(encodedMessage, Charset.forName("UTF-8"));
        }
        catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Key argument is not valid.");
        }
        catch (GeneralSecurityException e) {
            throw new IllegalStateException("Exception occurred during encryption: ." + e.getMessage());
        }
    }



    /*
    public static String encrypt(String seed, String cleartext)
            throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String decrypt(String seed, String encrypted)
            throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }*/

}