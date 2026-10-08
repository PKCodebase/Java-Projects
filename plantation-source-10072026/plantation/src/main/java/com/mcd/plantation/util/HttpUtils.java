package com.mcd.plantation.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpUtils {

	private static final String[] IP_HEADERS = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"
	};
	
	private HttpUtils() {}
	
	public static String getRequestIP(HttpServletRequest request) {
        for (String header: IP_HEADERS) {
        	String value = request.getHeader(header);
            if (value == null || value.isEmpty()) {
                continue;
            }
            String[] parts = value.split("\\s*,\\s*");
            return parts[0];
        }
        return request.getRemoteAddr();
    }
	
	public static HttpStatusCode httpStatusCode(int code) {
		switch (code) {
		case 200: {
			return HttpStatus.OK;
		}
		case 301: {
			return HttpStatus.MOVED_PERMANENTLY;
		}
		case 302: {
			return HttpStatus.FOUND;
		}
		case 304: {
			return HttpStatus.NOT_MODIFIED;
		}
		case 400: {
			return HttpStatus.BAD_REQUEST;
		}
		case 401: {
			return HttpStatus.UNAUTHORIZED;
		}
		case 403: {
			return HttpStatus.FORBIDDEN;
		}
		case 404: {
			return HttpStatus.NOT_FOUND;
		}
		case 405: {
			return HttpStatus.METHOD_NOT_ALLOWED;
		}
		case 406: {
			return HttpStatus.NOT_ACCEPTABLE;
		}
		case 415: {
			return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
		}
		case 500: {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		case 501: {
			return HttpStatus.NOT_IMPLEMENTED;
		}
		case 503: {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		default:
			return HttpStatus.BAD_GATEWAY;
		}
	}
}