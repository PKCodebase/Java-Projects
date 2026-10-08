package com.mcd.plantation.service.impl;

import com.mcd.plantation.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Verifies the Google Identity Services ID token ("credential") that the
 * browser receives after the user picks a Google account.
 *
 * <ul>
 *   <li>The token is sent to Google's token-info endpoint over HTTPS; Google
 *       validates the signature and the expiry, this class validates that the
 *       token was minted for <b>our</b> client id (audience) and that the
 *       account e-mail is verified.</li>
 *   <li>The token itself is a bearer credential: it is never stored, never
 *       logged and never echoed into an exception message — Google puts the
 *       whole token in the request URI, so only status codes (not
 *       {@code ex.getMessage()}, which contains the URI) are ever logged.</li>
 *   <li>Disabled by default: with {@code GOOGLE_CLIENT_ID} unset
 *       ({@code oauth2.google-client-id} empty) nothing is called at all.</li>
 * </ul>
 */
@Component
@Slf4j
public class GoogleTokenVerifier {

    /** Google's public ID-token verification endpoint (no key/secret involved). */
    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    private static final int TIMEOUT_MS = 5000;

    /** Public OAuth2 client id; empty ⇒ Google sign-in is not configured. */
    private final String clientId;
    private final RestTemplate rest;

    public GoogleTokenVerifier(@Value("${oauth2.google-client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.rest = new RestTemplate(factory);
    }

    public boolean isEnabled() {
        return !clientId.isEmpty();
    }

    /** The public client id (safe to return to anonymous callers). */
    public String clientId() {
        return clientId;
    }

    /** Verified profile — derived from claims Google has signed. */
    public record Profile(String sub, String email, String name) {}

    /**
     * Verifies the ID token and returns the profile it belongs to.
     *
     * @throws UnauthorizedException when the token cannot be verified — the
     *         message is always safe to show to the user and contains no token
     */
    public Profile verify(String idToken) {
        String uri = UriComponentsBuilder.fromHttpUrl(TOKEN_INFO_URL)
                .queryParam("id_token", idToken)
                .build()
                .toUriString();

        Map<?, ?> claims;
        try {
            claims = rest.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, Map.class).getBody();
        } catch (HttpStatusCodeException ex) {
            //4xx/5xx from Google → invalid, expired or tampered token.
            //Never log ex.getMessage(): it contains the request URI, i.e. the token.
            log.warn("Google ID token rejected by Google (HTTP {})", ex.getStatusCode().value());
            throw new UnauthorizedException(
                    "Google sign-in failed: the sign-in token is invalid or has expired. Please try again.");
        } catch (Exception ex) {
            log.warn("Google token verification unavailable: {}", ex.getClass().getSimpleName());
            throw new UnauthorizedException(
                    "Google sign-in is temporarily unavailable. Please try again later.");
        }

        if (claims == null || claims.isEmpty()) {
            throw new UnauthorizedException("Google sign-in failed: no details were returned by Google.");
        }

        //Audience: the token must have been issued for OUR client id.
        if (!clientId.equals(str(claims.get("aud")))) {
            log.warn("Google ID token audience does not match the configured client id");
            throw new UnauthorizedException(
                    "Google sign-in failed: this sign-in token was not issued for this application.");
        }

        String email = str(claims.get("email"));
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("Google sign-in failed: the Google account has no e-mail address.");
        }
        if (!"true".equalsIgnoreCase(str(claims.get("email_verified")))) {
            throw new UnauthorizedException(
                    "Google sign-in failed: the e-mail address of this Google account is not verified.");
        }

        //Defence in depth — Google already checked the signature and expiry.
        String exp = str(claims.get("exp"));
        if (exp != null) {
            try {
                if (Long.parseLong(exp) * 1000L < System.currentTimeMillis()) {
                    throw new UnauthorizedException(
                            "Google sign-in failed: the sign-in token has expired. Please try again.");
                }
            } catch (NumberFormatException ignore) {
                //Unparsable exp is not fatal — the signature was already validated by Google.
            }
        }

        return new Profile(str(claims.get("sub")), email, str(claims.get("name")));
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
