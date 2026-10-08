package com.mcd.plantation.security;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.mcd.plantation.pojo.ApiError;
import com.mcd.plantation.util.JsonUtil;

import java.io.IOException;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component("authEntryPoint")
public class AuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, 
			AuthenticationException authException) throws IOException, ServletException {
		HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
		httpServletResponse.setContentType("application/json;charset=UTF-8");
		httpServletResponse.setStatus(httpStatus.value());
		String authenticateHeader = "Bearer";
		httpServletResponse.addHeader("WWW-Authenticate", authenticateHeader);
		ApiError apiError = new ApiError(new Date(),
				String.valueOf(httpStatus.value()), 
				httpStatus.getReasonPhrase(), 
				httpServletRequest.getRequestURI(), 
				"ERR_401",
				"UNAUTHORIZED ACCESSS");
		httpServletResponse.getWriter().write(JsonUtil.convertObjectToJson(apiError));
	}

}