package com.mcd.plantation.util;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

public final class AESUtils {
	
	private static final String CIPHER = "AES";
	public static final int KEY_SIZE_128 = 128;
	public static final int KEY_SIZE_256 = 256;
	
	private AESUtils() {}
	
	public static Key generateKey(int keySize) throws NoSuchAlgorithmException {
	    KeyGenerator keyGenerator = KeyGenerator.getInstance(CIPHER);
	    keyGenerator.init(keySize);
	    return keyGenerator.generateKey();
	}
	
	public static String generateKeyAsString(int keySize) throws NoSuchAlgorithmException {
		Key key =  generateKey(keySize);
		String encodedKey = Base64Utils.encodeToString(key.getEncoded());
	    return encodedKey;
	}
}