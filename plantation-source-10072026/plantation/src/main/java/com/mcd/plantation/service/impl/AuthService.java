package com.mcd.plantation.service.impl;

import com.mcd.plantation.dto.request.CitizenRegisterRequest;
import com.mcd.plantation.dto.request.LoginRequest;
import com.mcd.plantation.dto.response.AuthResponse;
import com.mcd.plantation.dto.response.UserSummary;
import com.mcd.plantation.entity.Citizen;
import com.mcd.plantation.entity.McdOfficial;
import com.mcd.plantation.enums.UserTypeCode;
import com.mcd.plantation.exception.AppException;
import com.mcd.plantation.exception.DuplicateResourceException;
import com.mcd.plantation.exception.UnauthorizedException;
import com.mcd.plantation.pojo.UserToken;
import com.mcd.plantation.repository.CitizenRepository;
import com.mcd.plantation.repository.McdOfficialRepository;
import com.mcd.plantation.security.CompositeUserDetailsService;
import com.mcd.plantation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Issues JWTs for all three portal roles.
 *
 * <ul>
 *   <li>{@code POST /auth/citizen/register} → ROLE_CITIZEN</li>
 *   <li>{@code POST /auth/citizen/login}    → ROLE_CITIZEN</li>
 *   <li>{@code POST /auth/official/login}   → ROLE_R_HORTIC_ADM /
 *       ROLE_R_HORTIC_OFF / ROLE_SUPERVISOR</li>
 *   <li>{@code POST /auth/oauth2/google}    → ROLE_CITIZEN (verified Google
 *       ID token, only when GOOGLE_CLIENT_ID is configured)</li>
 *   <li>{@code POST /auth/refresh}          → new access + refresh pair</li>
 * </ul>
 *
 * The token claims match what {@link JwtUtil#getUserToken(String)} and the
 * {@code AuthTokenFilter} expect, so the role written here is the role Spring
 * Security enforces on every request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String CITIZEN_ROLE = "CITIZEN";

    private final CitizenRepository citizenRepo;
    private final McdOfficialRepository officialRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final CompositeUserDetailsService userDetailsService;

    // ── Registration ────────────────────────────────────────────────

    @Transactional
    public AuthResponse registerCitizen(CitizenRegisterRequest req) {
        if (citizenRepo.existsByEmail(req.email()) || officialRepo.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        Citizen citizen = Citizen.builder()
                .fullName(req.fullName())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .aadhaarLast4(req.aadhaarLast4())
                .address(req.address())
                .isActive(true)
                .build();

        citizenRepo.save(citizen);
        log.info("Citizen registered: {}", citizen.getCitizenId());
        return buildAuthResponse(citizen);
    }

    // ── Login ───────────────────────────────────────────────────────

    @Transactional
    public AuthResponse loginCitizen(LoginRequest req) {
        UserDetails user = authenticate(req);
        if (!(user instanceof Citizen citizen)) {
            throw new UnauthorizedException("This email is not registered as a citizen account");
        }
        citizen.setLastLoginAt(OffsetDateTime.now());
        citizenRepo.save(citizen);
        return buildAuthResponse(citizen);
    }

    public AuthResponse loginOfficial(LoginRequest req) {
        UserDetails user = authenticate(req);
        if (!(user instanceof McdOfficial official)) {
            throw new UnauthorizedException("This email is not registered as an official account");
        }
        if (!official.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        return buildAuthResponse(official);
    }

    // ── Google sign-in ──────────────────────────────────────────────

    /**
     * Issues a JWT for a Google account whose ID token has already been
     * verified by {@link GoogleTokenVerifier}.
     *
     * <p>Google sign-in only ever creates/opens <b>citizen</b> accounts: staff
     * accounts are provisioned by an administrator, so an official e-mail is
     * rejected here rather than silently given a citizen session. A brand-new
     * Google account gets a random, unusable local password hash — it exists
     * only to satisfy the NOT NULL column; nothing user-chosen is invented and
     * the password path stays closed.</p>
     */
    @Transactional
    public AuthResponse loginWithGoogle(GoogleTokenVerifier.Profile profile) {
        if (officialRepo.existsByEmail(profile.email())) {
            throw new UnauthorizedException(
                    "This e-mail address belongs to an MCD staff account. Please sign in with your password.");
        }

        Citizen citizen = citizenRepo.findByEmail(profile.email()).orElseGet(() -> createGoogleCitizen(profile));
        if (!citizen.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        citizen.setLastLoginAt(OffsetDateTime.now());
        citizenRepo.save(citizen);
        return buildAuthResponse(citizen);
    }

    private Citizen createGoogleCitizen(GoogleTokenVerifier.Profile profile) {
        String name = (profile.name() == null || profile.name().isBlank())
                ? profile.email().substring(0, profile.email().indexOf('@'))
                : profile.name().trim();

        Citizen citizen = Citizen.builder()
                .fullName(name)
                .email(profile.email())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .isActive(true)
                .build();
        citizenRepo.save(citizen);
        log.info("Citizen created via Google sign-in: {}", citizen.getCitizenId());
        return citizen;
    }

    // ── Refresh ─────────────────────────────────────────────────────

    /**
     * Exchanges a refresh token for a fresh access/refresh pair. The account is
     * re-read from the database, so deactivated accounts cannot refresh.
     */
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is required");
        }

        String loginId;
        try {
            loginId = jwtUtil.getRefreshLoginId(refreshToken);
        } catch (AppException | IllegalArgumentException ex) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(loginId);
        } catch (UsernameNotFoundException ex) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is deactivated");
        }
        return buildAuthResponse(user);
    }

    // ── helpers ─────────────────────────────────────────────────────

    private UserDetails authenticate(LoginRequest req) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        return (UserDetails) authentication.getPrincipal();
    }

    private AuthResponse buildAuthResponse(UserDetails user) {
        UserToken userToken = toUserToken(user);
        String accessToken = jwtUtil.generateAccessToken(userToken);
        String refreshToken = jwtUtil.generateRefreshToken(userToken);

        String id;
        String fullName;
        if (user instanceof Citizen citizen) {
            id = citizen.getCitizenId().toString();
            fullName = citizen.getFullName();
        } else {
            McdOfficial official = (McdOfficial) user;
            id = official.getOfficialId();
            fullName = official.getName();
        }

        UserSummary summary = new UserSummary(id, fullName, user.getUsername(), userToken.getRoleCode());
        return AuthResponse.of(accessToken, refreshToken, jwtUtil.getExpiryMs(), summary);
    }

    /**
     * Maps a principal onto the claim set every downstream service reads:
     *
     * <ul>
     *   <li>{@code loginId}       – the e-mail the user signed in with</li>
     *   <li>{@code userSystemCode} – citizen UUID for citizens (bookings),
     *       employee id for officials (park assignments / plantation
     *       records)</li>
     *   <li>{@code userTypeCode}  – UT_CTZ / UT_EMP</li>
     *   <li>{@code roleCode}      – CITIZEN / R_HORTIC_ADM / R_HORTIC_OFF /
     *       SUPERVISOR</li>
     * </ul>
     */
    private UserToken toUserToken(UserDetails user) {
        if (user instanceof Citizen citizen) {
            String citizenId = citizen.getCitizenId().toString();
            return new UserToken(
                    citizenId,                                  // userGuid
                    citizenId,                                  // userProfileGuid
                    "PWD",                                      // authTypeCode
                    citizenId,                                  // userSystemCode
                    null,                                       // userSpecifiedCode
                    citizen.getEmail(),                         // loginId
                    citizen.getEmail(),                         // emailId
                    citizen.getPhone(),                         // mobileNumber
                    UserTypeCode.UT_CTZ.name(),                 // userTypeCode
                    CITIZEN_ROLE);                              // roleCode
        }

        McdOfficial official = (McdOfficial) user;
        return new UserToken(
                official.getOfficialId(),                       // userGuid
                official.getOfficialId(),                       // userProfileGuid
                "PWD",                                          // authTypeCode
                official.getEmployeeId(),                       // userSystemCode
                null,                                           // userSpecifiedCode
                official.getEmail(),                            // loginId
                official.getEmail(),                            // emailId
                official.getPhone(),                            // mobileNumber
                UserTypeCode.UT_EMP.name(),                     // userTypeCode
                official.getRole().name());                     // roleCode
    }
}
