package com.mcd.plantation.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

	public static final String ALGO_MD5 = "MD5";
	public static final String ALGO_SHA256 = "SHA-256";
	public static final String ALGO_SHA512 = "SHA-512";
	
	private HashUtil() {}
	
	public static String calculateHash(String dataString, String hashAlgo) throws NoSuchAlgorithmException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(hashAlgo);
			byte[] encodedhash = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));
			String hash = bytesToHex(encodedhash);
			return hash;
		} catch (NoSuchAlgorithmException exception) {
			throw exception;
		}
	}
	
	public static byte[] calculateHashBytes(String dataString, String hashAlgo) throws NoSuchAlgorithmException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(hashAlgo);
			byte[] encodedhash = digest.digest(dataString.getBytes(StandardCharsets.UTF_8));
			return encodedhash;
		} catch (NoSuchAlgorithmException exception) {
			throw exception;
		}
	}
	
	private static String bytesToHex(byte[] hash) {
	    StringBuilder hexString = new StringBuilder(2 * hash.length);
	    for (int i = 0; i < hash.length; i++) {
	        String hex = Integer.toHexString(0xff & hash[i]);
	        if(hex.length() == 1) {
	            hexString.append('0');
	        }
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
}
