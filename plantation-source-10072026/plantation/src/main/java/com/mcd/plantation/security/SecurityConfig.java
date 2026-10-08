package com.mcd.plantation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// ════════════════════════════════════════════════════════════════
//  SECURITY CONFIGURATION
// ════════════════════════════════════════════════════════════════
@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private AuthenticationEntryPoint authEntryPoint;
	
	@Autowired
	private AuthAccessDeniedHandler authAccessDeniedHandler;
	
	@Bean
	AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}
	
	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex
                // no / invalid token            → 401 (JSON)
                .authenticationEntryPoint(authEntryPoint)
                // valid token, wrong role       → 403 (JSON, written directly
                //                                 so the status is not turned
                //                                 into a 401 by the error
                //                                 dispatch)
                .accessDeniedHandler(authAccessDeniedHandler))
        .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitUrls()).permitAll()
                // Staff-only surface: Horticulture Admin (R_HORTIC_ADM),
                // Horticulture Officer (R_HORTIC_OFF) and Supervisor.
                // Citizens get 403 here even with a valid token.
                .requestMatchers("/mcd/**").hasAnyRole("R_HORTIC_ADM", "R_HORTIC_OFF", "SUPERVISOR")
                // Everything else just needs a valid, authenticated token.
                .requestMatchers("/**").hasRole("USER")
                .anyRequest().denyAll()
        )
        .addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

    return http.build();
	}
	
	private String[] permitUrls() {
		Set<String> urls = new HashSet<String>();
		// Public read endpoints
		urls.add("/parks");
		urls.add("/parks/**");
		urls.add("/trees");
		urls.add("/trees/**");
		urls.add("/slots/**");
		urls.add("/certificates/verify/**");
		urls.add("/zones");
		urls.add("/zones/**");
		urls.add("/occasions");
		urls.add("/occasions/**");
		urls.add("/uploads/**");
		urls.add("/v3/api-docs/**");
		urls.add("/swagger-ui/**");
		urls.add("/swagger-ui.html");
		urls.add("/actuator/health");
		urls.add("/payment/webhook");
		// Auth endpoints (login / register / OTP / forgot-password / OAuth2)
		urls.add("/auth");
		urls.add("/auth/**");

		return urls.toArray(new String[urls.size()]);
	}

	/**
	 * BCrypt for every stored password (citizens + officials).
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	/**
	 * Backs the DaoAuthenticationProvider used by the /auth login endpoints.
	 * The CompositeUserDetailsService bean resolves the login e-mail to either
	 * a Citizen (ROLE_CITIZEN) or an McdOfficial (ROLE_R_HORTIC_ADM /
	 * ROLE_R_HORTIC_OFF / ROLE_SUPERVISOR).
	 */
	@Bean
	AuthenticationManager authManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}

	/**
	 * CORS for the Angular dev server. Allowed origins come from the
	 * CORS_ALLOWED_ORIGINS env var (comma-separated). Origins are public values,
	 * never secrets — the default covers the local Angular dev server only.
	 */
	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		String allowed = System.getenv().getOrDefault(
				"CORS_ALLOWED_ORIGINS", "http://localhost:4200,http://127.0.0.1:4200");
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(Arrays.asList(allowed.split("\\s*,\\s*")));
		cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
	
	/*
	 * private final AuthenticationEntryPoint authEntryPoint;
	 * 
	 * private final JwtAuthFilter jwtAuthFilter;
	 * 
	 * private static final String[] PUBLIC_GET = { "/parks", "/parks/**", "/trees",
	 * "/trees/**", "/slots/**", "/certificates/verify/**", "/zones", "/zones/**",
	 * "/occasions", "/occasions/**", "/uploads/**", "/v3/api-docs/**",
	 * "/swagger-ui/**", "/swagger-ui.html", "/actuator/health" };
	 * 
	 * private static final String[] PUBLIC_POST = { "/auth/citizen/register",
	 * "/auth/citizen/login", "/auth/official/login", "/auth/refresh",
	 * "/payment/webhook" // Razorpay webhook — no auth header };
	 * 
	 * @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws
	 * Exception { return http .csrf(AbstractHttpConfigurer::disable)
	 * .sessionManagement(s ->
	 * s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	 * .authorizeHttpRequests(auth -> auth .requestMatchers(HttpMethod.GET,
	 * PUBLIC_GET).permitAll() .requestMatchers(HttpMethod.POST,
	 * PUBLIC_POST).permitAll()
	 * .requestMatchers("/mcd/**").hasAnyRole("HORTICULTURE_OFFICER","SUPERVISOR",
	 * "ADMIN") .anyRequest().authenticated() ) .addFilterBefore(jwtAuthFilter,
	 * UsernamePasswordAuthenticationFilter.class) .build(); }
	 * 
	 * @Bean public PasswordEncoder passwordEncoder() { return new
	 * BCryptPasswordEncoder(12); }
	 * 
	 * @Bean public AuthenticationManager authManager(AuthenticationConfiguration
	 * cfg) throws Exception { return cfg.getAuthenticationManager(); }
	 */
}


/*
 * //════════════════════════════════════════════════════════════════ //JWT
 * FILTER //════════════════════════════════════════════════════════════════
 * 
 * @Component @RequiredArgsConstructor @Slf4j class JwtAuthFilter extends
 * OncePerRequestFilter {
 * 
 * private final JwtUtil jwtUtil; private final UserDetailsService
 * userDetailsService;
 * 
 * @Override protected void doFilterInternal(HttpServletRequest req,
 * HttpServletResponse res, FilterChain chain) throws ServletException,
 * IOException { String header = req.getHeader("Authorization"); if (header ==
 * null || !header.startsWith("Bearer ")) { chain.doFilter(req, res); return; }
 * 
 * String token = header.substring(7); try { String username =
 * jwtUtil.extractUsername(token); if (username != null &&
 * SecurityContextHolder.getContext().getAuthentication() == null) { UserDetails
 * user = userDetailsService.loadUserByUsername(username); if
 * (jwtUtil.isValid(token, user)) { var auth = new
 * UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
 * auth.setDetails(new org.springframework.security.web.authentication
 * .WebAuthenticationDetailsSource().buildDetails(req));
 * SecurityContextHolder.getContext().setAuthentication(auth); } } } catch
 * (Exception e) { log.warn("JWT filter error: {}", e.getMessage()); }
 * chain.doFilter(req, res); } }
 */
