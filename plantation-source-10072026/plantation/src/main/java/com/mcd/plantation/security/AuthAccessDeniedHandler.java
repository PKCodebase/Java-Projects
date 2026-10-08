package com.mcd.plantation.security;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.mcd.plantation.pojo.ApiError;
import com.mcd.plantation.util.JsonUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Writes a JSON 403 directly instead of calling
 * {@code response.sendError(403)}.
 *
 * <p>{@code sendError} triggers a container error dispatch to {@code /error},
 * which re-enters the security filter chain without the original
 * authentication and turns the 403 into a misleading 401. With this handler
 * the status stays what it really is: the token is valid, the role is wrong
 * (e.g. a citizen calling an {@code /mcd/**} staff endpoint).</p>
 */
@Component("authAccessDeniedHandler")
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpStatus.FORBIDDEN.value());

		ApiError apiError = new ApiError(new Date(),
				String.valueOf(HttpStatus.FORBIDDEN.value()),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				request.getRequestURI(),
				"ERR_403",
				"ACCESS DENIED — your account role is not allowed to call this endpoint");

		response.getWriter().write(JsonUtil.convertObjectToJson(apiError));
	}
}
