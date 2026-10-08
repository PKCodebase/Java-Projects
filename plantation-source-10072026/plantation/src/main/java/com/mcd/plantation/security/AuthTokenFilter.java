package com.mcd.plantation.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.mcd.plantation.exception.AppException;
import com.mcd.plantation.pojo.ApiError;
import com.mcd.plantation.pojo.ErrorCode;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.util.JsonUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, 
			HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
		try {
			String jwt = parseJwt(httpServletRequest);
			if (jwt != null && jwtUtil.validateToken(jwt)) {
				// A token without the "loginId" claim (e.g. a refresh token
				// replayed as a bearer token) is rejected here with 401.
				UserToken userToken = jwtUtil.getUserToken(jwt);

				List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
				// ROLE_USER = "somebody is logged in" (generic gate) plus the
				// role that was put into the token at login time, so the three
				// portal roles are really enforced:
				//   ROLE_R_HORTIC_ADM — Horticulture Admin
				//   ROLE_R_HORTIC_OFF — Horticulture Officer / ROLE_SUPERVISOR
				//   ROLE_CITIZEN      — Citizen
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				String roleCode = userToken.getRoleCode();
				if (roleCode != null && !roleCode.isBlank()) {
					authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode.trim()));
				}

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userToken, 
						jwt, authorities);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			filterChain.doFilter(httpServletRequest, httpServletResponse);
		} catch (Exception exception) {
			HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;
			httpServletResponse.setContentType("application/json;charset=UTF-8");
			httpServletResponse.setStatus(httpStatus.value());
			String authenticateHeader = "Bearer";
			httpServletResponse.addHeader("WWW-Authenticate", authenticateHeader);
			ApiError apiError = null;
			if(exception instanceof AppException) {
				AppException appException = (AppException)exception;
				ErrorCode errorCode = appException.getErrorCode();
				apiError = new ApiError(appException.getExceptionDateTime(),
										String.valueOf(httpStatus.value()), 
										httpStatus.getReasonPhrase(), 
										httpServletRequest.getRequestURI(), 
										errorCode != null ? errorCode.getCode() : null,
										errorCode != null ? errorCode.getMessage() : null);
			}else {
				apiError = new ApiError(String.valueOf(httpStatus.value()), 
						httpStatus.getReasonPhrase(), 
						httpServletRequest.getRequestURI(), 
						null,
						exception.getMessage());
			}
			httpServletResponse.getWriter().write(JsonUtil.convertObjectToJson(apiError));
		}
		
	}

	private String parseJwt(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");
		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}
	
}
