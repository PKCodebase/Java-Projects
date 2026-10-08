package com.mcd.plantation.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import jakarta.xml.bind.DatatypeConverter;

public class PgAESUtilityClient {

	public SecretKeySpec secretKey;
	public byte[] key;
	
	public void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key, "AES");
			// The key material itself is never printed — it comes from the
			// PG_AES_KEY env var and must stay out of stdout/log files.
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String base64(byte[] bytes) {
		return DatatypeConverter.printBase64Binary(bytes);
	}
	public static byte[] base64(String str) {
		return DatatypeConverter.parseBase64Binary(str);
	}
	public String encrypt(String strToEncrypt, String secret) {
		try {
			this.setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return URLEncoder.encode(base64(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8))));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}
	public String decrypt(String strToDecrypt, String secret) {
		try {
			this.setKey(secret);
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			String fs = new String(cipher.doFinal(base64(strToDecrypt)), "UTF-8");
			return fs;
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}
	
}