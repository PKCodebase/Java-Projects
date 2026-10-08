package com.mcd.plantation.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class PgUtil {

	public static String sendPOST(String paymentUrl, String paramter, String jsonBody)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {
		try {
			String fullUrl = paymentUrl + "?" + paramter;
	        URL obj = new URL(fullUrl);
			HttpsURLConnection con = null;
			//OutputStream os = null;

			TrustManager[] trustAllCerts = getTrrrust();
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return false;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			// For POST only - START
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json");
	        con.setRequestProperty("Accept", "application/json");
			System.out.println("Connecting with Server");

			 try (OutputStream os = con.getOutputStream()) {
		            byte[] input = jsonBody.getBytes("UTF-8");
		            os.write(input, 0, input.length);
		            os.flush();
		        }
			 
			int osLength = 0;
			osLength = con.getOutputStream().toString().length();
			//os = con.getOutputStream();
			System.out.println("osLength is " + osLength);
			System.out.println("Connectionestablished with  server ");
			// Request URL (encrypted `param`) and body are deliberately NOT
			// printed: they carry the gateway's encrypted payload.
			String POST_PARAMS = paramter;
			//os.write(POST_PARAMS.getBytes());
			//os.flush();
			//os.close();
			
			// For POST only - END
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);
			if (responseCode == HttpsURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				// Gateway response body is deliberately NOT printed — it holds the
			// payment session reference. Callers log the outcome instead.
				// print result
				return response.toString();

			} else {
				
				System.out.println("POST request not worked");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static String sendHttpPOST(String paymentUrl, String paramter, String jsonBody)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {
	    try {
	        String fullUrl = paymentUrl + "?" + paramter;
	        URL obj = new URL(fullUrl);

	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setRequestMethod("POST");
	        con.setDoOutput(true);

	        con.setRequestProperty("Content-Type", "application/json");
	        con.setRequestProperty("Accept", "application/json");

	        System.out.println("Connecting with Server");

	        try (OutputStream os = con.getOutputStream()) {
	            byte[] input = jsonBody.getBytes("UTF-8");
	            os.write(input, 0, input.length);
	            os.flush();
	        }

	        int responseCode = con.getResponseCode();
	        System.out.println("POST Response Code :: " + responseCode);

	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
	                StringBuilder response = new StringBuilder();
	                String inputLine;
	                while ((inputLine = in.readLine()) != null) {
	                    response.append(inputLine);
	                }
	                // response body deliberately not logged
	                return response.toString();
	            }
	        } else {
	            try (BufferedReader err = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"))) {
	                StringBuilder errorResponse = new StringBuilder();
	                String inputLine;
	                while ((inputLine = err.readLine()) != null) {
	                    errorResponse.append(inputLine);
	                }
	                System.out.println("Error response received (" + errorResponse.length() + " chars)");
	            }
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	    return null;
	}
	
	public static String sendPOST(String paymentUrl, String paramter)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {

		try {
			URL obj = new URL(paymentUrl);
			HttpsURLConnection con = null;
			OutputStream os = null;

			TrustManager[] trustAllCerts = getTrrrust();
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return false;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

			con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			// For POST only - START
			con.setDoOutput(true);
			System.out.println("Connecting with Server");

			int osLength = 0;
			osLength = con.getOutputStream().toString().length();
			os = con.getOutputStream();
			System.out.println("osLength is " + osLength);
			System.out.println("Connectionestablished with  server ");
			// POST params are deliberately NOT printed (encrypted payload).
			String POST_PARAMS = paramter;
			os.write(POST_PARAMS.getBytes());
			os.flush();
			os.close();
			// For POST only - END
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);
			if (responseCode == HttpsURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				// Gateway response body is deliberately NOT printed — it holds the
			// payment session reference. Callers log the outcome instead.
				// print result
				return response.toString();

			}
			
			if (responseCode == HttpsURLConnection.HTTP_BAD_REQUEST) {
				// ???? HANDNLE MCDERROR_REC004 •	MCDERROR_REC009 etc 
			}
			
			else {
				System.out.println("POST request not worked");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	public static String sendHttpPOST(String paymentUrl, String paramter)
			throws IOException, NoSuchAlgorithmException, KeyManagementException {

		try {
			URL obj = new URL(paymentUrl);
			HttpURLConnection con = null;
			OutputStream os = null;

			con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			// For POST only - START
			con.setDoOutput(true);
			System.out.println("Connecting with Server");
			int osLength = 0;
			osLength = con.getOutputStream().toString().length();
			os = con.getOutputStream();
			if (osLength == 0) {
				con = (HttpURLConnection) obj.openConnection();
				con.setRequestMethod("POST");
				// For POST only - START
				con.setDoOutput(true);
				System.out.println("Connecting with Server");
				osLength = con.getOutputStream().toString().length();
				os = con.getOutputStream();
			}
			System.out.println("Connectionestablished with  server ");
			// iMcdGatewayDao.persistAxisEasyPayEnquiryReconcilationRequest(paymentReconciableEntity);
			// POST params are deliberately NOT printed (encrypted payload).
			String POST_PARAMS = paramter;
			os.write(POST_PARAMS.getBytes());
			os.flush();
			os.close();
			// For POST only - END
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :: " + responseCode);
			if (responseCode == HttpsURLConnection.HTTP_OK) { // success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				// Gateway response body is deliberately NOT printed — it holds the
			// payment session reference. Callers log the outcome instead.
				// print result
				return response.toString();

			} else {
				System.out.println("POST request not worked");

			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	
	public static TrustManager[] getTrrrust() {
		return new TrustManager[] { 
				new X509ExtendedTrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) {

			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, Socket socket) {

			}

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) {

			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine ssle) {

			}

		} };
	}
}