package com.mcd.plantation.dto.request;

import jakarta.validation.constraints.*;

/**
 * The Google Identity Services ID token handed to us by the browser.
 *
 * <p>The token is a bearer credential: it is forwarded to Google for
 * verification and is never written to a log, a database row or an error
 * message.</p>
 */
public record GoogleCredentialRequest(
    @NotBlank String credential
) {}
