package com.mcd.plantation.dto.response;

/**
 * Public Google sign-in configuration, served anonymously so the UI can tell
 * "not configured" apart from "broken".
 *
 * <p>{@code clientId} is the public OAuth2 client id (never a secret — the
 * client secret, if the flow ever needs one, stays on the server). When Google
 * sign-in is disabled it is an empty string.</p>
 */
public record OauthClientConfigResponse(
    boolean enabled,
    String clientId
) {}
