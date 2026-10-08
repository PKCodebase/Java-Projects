package com.mcd.plantation.security;

import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mcd.plantation.exception.AppException;
import com.mcd.plantation.pojo.ErrorCode;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.util.HashUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;


@Component("jwtUtil")
public class JwtUtil {
	
	//private static final Logger logger = LoggerFactory.getLogger("JWT");
	
		@Value("${jwt.signKey}")
		private String SIGN_KEY;
		
		@Value("${jwt.encryptdecryptkey}")
		private String ENCRYPT_DECRYPT_KEY;
		
		/** Access-token lifetime (ms). Default: 1 hour. */
		@Value("${jwt.access-expiry-ms:3600000}")
		private long ACCESS_EXPIRY_MS;
		
		/** Refresh-token lifetime (ms). Default: 7 days. */
		@Value("${jwt.refresh-expiry-ms:604800000}")
		private long REFRESH_EXPIRY_MS;
		
		private SecretKey signingKey;
		
		private SecretKey encrypDecryptKey;
		
		//private String KEY_SALT;

		@PostConstruct
	    public void init() throws NoSuchAlgorithmException {
			//KEY_SALT = GenerateUtil.getUUID();
			//logger.info("JWT KEY SALT :: " + KEY_SALT);
			//byte[] signKey = HashUtil.calculateHashBytes(SIGN_KEY + KEY_SALT, HashUtil.ALGO_SHA512);
			//byte[] encryptDecryptKey = HashUtil.calculateHashBytes(ENCRYPT_DECRYPT_KEY + KEY_SALT, HashUtil.ALGO_SHA512);
			byte[] signKey = HashUtil.calculateHashBytes(SIGN_KEY, HashUtil.ALGO_SHA512);
			byte[] encryptDecryptKey = HashUtil.calculateHashBytes(ENCRYPT_DECRYPT_KEY, HashUtil.ALGO_SHA512);
			signingKey = Keys.hmacShaKeyFor(signKey);
			encrypDecryptKey = Keys.hmacShaKeyFor(encryptDecryptKey);
	    }
		
		public JwtUtil() {
		}
		
		
		
		// VALIDATE AUTH TOKEN OF USER
		public boolean validateToken(String authToken) throws AppException {
			try {
				byte[] jwsBytes = (byte[]) Jwts.parser().decryptWith(encrypDecryptKey).build().parse(authToken).getPayload();
				Jwts.parser().verifyWith(signingKey).build().parse(new ByteArrayInputStream(jwsBytes)).getPayload();
				return true;
			} catch (SignatureException exception) {
				throw new AppException("INVALID TOKEN SIGNATURE : " + exception.getMessage(), 
						exception, 
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("INVALID TOKEN SIGNATURE"));
			} catch (MalformedJwtException exception) {
				throw new AppException("INVALID TOKEN : " + exception.getMessage(), 
						exception, 
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("INVALID TOKEN"));
			} catch (ExpiredJwtException exception) {
				throw new AppException("TOKEN IS EXPIRED : " + exception.getMessage(), 
						exception, 
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("TOKEN IS EXPIRED"));
			} catch (UnsupportedJwtException exception) {
				throw new AppException("TOKEN IS UNSUPPORTED : " + exception.getMessage(), 
						exception, 
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("TOKEN IS UNSUPPORTED"));
			} catch (IllegalArgumentException exception) {
				throw new AppException("TOKEN CLAIMS IS EMPTY : " + exception.getMessage(), 
						exception, 
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("TOKEN CLAIMS IS EMPTY"));
			}
		}
		
		//GET USER TOKEN OBJECT FROM AUTH TOKEN
		public UserToken getUserToken(String authToken) throws AppException {
			byte[] jwsBytes = (byte[]) Jwts.parser()
	                .decryptWith(encrypDecryptKey) // Use the encryption key to decrypt
	                .build()
	                .parse(authToken) // Parses the JWE and returns the byte array content
	                .getPayload();
			Claims claims = (Claims) Jwts.parser()
	                .verifyWith(signingKey)
	                .build()
	                .parse(new ByteArrayInputStream(jwsBytes)) // Use generic parse() as we don't know the final content type yet
	                .getPayload();
			if(claims != null && !claims.isEmpty() && claims.containsKey("loginId")) {
				UserToken userToken = new UserToken(claims.get("userGuid") != null ? claims.get("userGuid").toString() : null,
						claims.get("userProfileGuid") != null ? claims.get("userProfileGuid").toString() : null,
						claims.get("authTypeCode") != null ? claims.get("authTypeCode").toString() : null, 
						claims.get("userSystemCode") != null ? claims.get("userSystemCode").toString() : null, 
						claims.get("userSpecifiedCode") != null ? claims.get("userSpecifiedCode").toString() : null, 
						claims.get("loginId") != null ? claims.get("loginId").toString() : null, 
						claims.get("emailId") != null ? claims.get("emailId").toString() : null, 
						claims.get("mobileNumber") != null ? claims.get("mobileNumber").toString() : null,
						claims.get("userTypeCode") != null ? claims.get("userTypeCode").toString() : null,
						claims.get("roleCode") != null ? claims.get("roleCode").toString() : null, 
						claims.get("sid") != null ? claims.get("sid").toString() : null,
						claims.get("sk") != null ? claims.get("sk").toString() : null);					
				return userToken;
			}
			throw new AppException("TOKEN CLAIMS IS EMPTY", 
					new Throwable(), 
					ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("TOKEN CLAIMS IS EMPTY"));
		}
		
		/** Lifetime of an access token, in milliseconds. */
		public long getExpiryMs() {
			return ACCESS_EXPIRY_MS;
		}
		
		/**
		 * Issues an access token: the claims are JWS-signed with the signing key
		 * and the signed JWT is then JWE-encrypted with the encryption key — the
		 * exact format consumed by validateToken() and getUserToken().
		 */
		public String generateAccessToken(UserToken userToken) {
			Date issuedAt = new Date();
			Date expiry = new Date(System.currentTimeMillis() + ACCESS_EXPIRY_MS);
			String signedJwt = Jwts.builder()
					.claims(generateClaims(userToken))
					.issuedAt(issuedAt)
					.expiration(expiry)
					.signWith(signingKey)
					.compact();
			return encrypt(signedJwt);
		}
		
		/**
		 * Issues a refresh token. It deliberately carries NO "loginId" claim, so
		 * getUserToken() rejects it — a refresh token can never be replayed as an
		 * access token. The login travels in the standard "sub" claim instead.
		 */
		public String generateRefreshToken(UserToken userToken) {
			Map<String, Object> claims = new HashMap<>();
			claims.put("tokenType", "refresh");
			claims.put("roleCode", userToken.getRoleCode());
			claims.put("userTypeCode", userToken.getUserTypeCode());
			claims.put("userSystemCode", userToken.getUserSystemCode());
			String signedJwt = Jwts.builder()
					.claims(claims)
					.subject(userToken.getLoginId())
					.issuedAt(new Date())
					.expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRY_MS))
					.signWith(signingKey)
					.compact();
			return encrypt(signedJwt);
		}
		
		/**
		 * Validates a refresh token and returns its subject (the login e-mail).
		 * Throws AppException when the token is not a refresh token.
		 */
		public String getRefreshLoginId(String refreshToken) throws AppException {
			Claims claims = parseClaims(refreshToken);
			if (!"refresh".equals(claims.get("tokenType", String.class)) || claims.getSubject() == null) {
				throw new AppException("INVALID REFRESH TOKEN",
						new Throwable(),
						ErrorCode.TOKEN_VALIDATION_ERROR.changeMessage("INVALID REFRESH TOKEN"));
			}
			return claims.getSubject();
		}
		
		// ── internals ───────────────────────────────────────────────────
		
		private String encrypt(String signedJwt) {
			return Jwts.builder()
					.content(signedJwt.getBytes(java.nio.charset.StandardCharsets.UTF_8))
					.encryptWith(encrypDecryptKey, Jwts.ENC.A256CBC_HS512)
					.compact();
		}
		
		private Claims parseClaims(String authToken) throws AppException {
			byte[] jwsBytes = (byte[]) Jwts.parser()
					.decryptWith(encrypDecryptKey)
					.build()
					.parse(authToken)
					.getPayload();
			return (Claims) Jwts.parser()
					.verifyWith(signingKey)
					.build()
					.parse(new ByteArrayInputStream(jwsBytes))
					.getPayload();
		}
		
		private Map<String, Object> generateClaims(UserToken userToken) {
			Map<String, Object> claims = new HashMap<>();
			claims.put("userGuid", userToken.getUserGuid());
			claims.put("userProfileGuid", userToken.getUserProfileGuid());
			claims.put("userTypeCode", userToken.getUserTypeCode());
			claims.put("authTypeCode", userToken.getAuthTypeCode());
			claims.put("userSystemCode", userToken.getUserSystemCode());
			claims.put("userSpecifiedCode", userToken.getUserSpecifiedCode());
			claims.put("loginId", userToken.getLoginId());
			claims.put("emailId", userToken.getEmailId());
			claims.put("mobileNumber", userToken.getMobileNumber());
			claims.put("roleCode", userToken.getRoleCode());
			claims.put("sid", userToken.getSid());
			claims.put("sk", userToken.getSk());
			return claims;
		}
		
}

/*
@Component @Slf4j
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expiryMs;
    private final long refreshExpiryMs;

    public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiry-ms}") long expiryMs,
        @Value("${jwt.refresh-expiry-ms}") long refreshExpiryMs) {
        this.signingKey     = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs       = expiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    public String generateToken(UserDetails user, UUID userId, String role) {
        return Jwts.builder()
            .claims(Map.of("role", role, "userId", userId.toString()))
            .subject(user.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
            .subject(username)
            .claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
            .signWith(signingKey)
            .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build()
            .parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return extractClaims(token).getSubject(); }
    public long   getExpiryMs()                 { return expiryMs; }

    public boolean isValid(String token, UserDetails user) {
        try {
            return extractUsername(token).equals(user.getUsername())
                && !extractClaims(token).getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}*/
