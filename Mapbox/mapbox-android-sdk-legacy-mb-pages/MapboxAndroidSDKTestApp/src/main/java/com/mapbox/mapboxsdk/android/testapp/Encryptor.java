package com.mapbox.mapboxsdk.android.testapp;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import android.util.Base64;
import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Arrays;

public class Encryptor {
    public static String encrypt(final String unencryptedMessage, final String hexKey) {
        try {
            // Convert message and key to bytes
            final byte[] hexKeyAsBytes = Base64.decode(hexKey, Base64.DEFAULT);
            final byte[] encodedMessage = unencryptedMessage.getBytes(Charset.forName("UTF-8"));

            // Get the cipher, block size, and secret key spec
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hexKeyAsBytes, "AES");

            // Build a random IV
            final byte[] ivData = new byte[blockSize];
            final IvParameterSpec ivParameterSpec = buildIvParameterSpec(ivData);

            // Encrypt the message
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            final byte[] encryptedMessage = cipher.doFinal(encodedMessage);

            // Concatenate the IV and the encrypted message
            final int messageLength = ivData.length + encryptedMessage.length;
            final byte[] ivAndEncryptedMessage = new byte[messageLength];
            System.arraycopy(ivData, 0, ivAndEncryptedMessage, 0, blockSize);
            System.arraycopy(encryptedMessage, 0, ivAndEncryptedMessage, blockSize, encryptedMessage.length);

            // Return the result as a string
            return Base64.encodeToString(ivAndEncryptedMessage,Base64.DEFAULT);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Key argument is not a valid AES key: " + e.getMessage(), e);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unexpected exception during encryption: " + e.getMessage(), e);
        }
    }

    public static String decrypt(final String ivAndEncryptedMessage, final String hexKey) {
        try {
            // Covert message and key to bytes
            final byte[] hexKeyAsBytes = Base64.decode(hexKey, Base64.DEFAULT);
            final byte[] encodedIvAndEncryptedMessage = Base64.decode(ivAndEncryptedMessage, Base64.DEFAULT);

            // Get the cipher, block size, and secret key spec
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hexKeyAsBytes, "AES");

            // Get the IV
            final byte[] ivData = new byte[blockSize];
            System.arraycopy(encodedIvAndEncryptedMessage, 0, ivData, 0, blockSize);
            final IvParameterSpec ivParameterSpec = new IvParameterSpec(ivData);

            // Get the encrypted message
            final int encryptedMessageLength = encodedIvAndEncryptedMessage.length - blockSize;
            final byte[] encryptedMessage = new byte[encryptedMessageLength];
            System.arraycopy(encodedIvAndEncryptedMessage, blockSize, encryptedMessage, 0, encryptedMessage.length);

            // Decrypt the message
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            final byte[] encodedMessage = cipher.doFinal(encryptedMessage);

            // Return the result as a string
            return new String(encodedMessage, Charset.forName("UTF-8"));
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Key argument does not contain a valid AES key: " + e.getMessage(), e);
        } catch (BadPaddingException e) {
            Log.e("Decrypt", "BadPaddingException: " + e.getMessage());
            return null;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unexpected exception during decryption: " + e.getMessage(), e);
        }
    }

    public static IvParameterSpec buildIvParameterSpec(byte[] ivData)
        throws NoSuchAlgorithmException {
        final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        rnd.nextBytes(ivData);
        return new IvParameterSpec(ivData);
    }

    public static String buildMac(final String key, final String data1, final String data2, final String data3) {
        String concatenatedMessage = data1 + data2 + data3 + authCode;
        return encrypt(concatenatedMessage, key);
    }

    private static final String authCode = "a4027119f0ef686219b636be44a9f415fc61339c3f09162ca6c4c0f0cc40687a";
}