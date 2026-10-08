package com.mcd.plantation.entity;

import com.mcd.plantation.enums.OfficialRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
// ════════════════════════════════════════════════════════════════
//  MCD OFFICIAL  — local credential store for the two staff roles
//  (R_HORTIC_ADM = Horticulture Admin, R_HORTIC_OFF = Horticulture
//  Officer, SUPERVISOR = Supervisor)
// ════════════════════════════════════════════════════════════════
@Entity
@Table(name = "mcd_officials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class McdOfficial implements UserDetails {

    @Id
    @Column(name = "official_id")
    private String officialId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String employeeId;

    /**
     * Zone this officer belongs to ({@code mcd_officials.zone_id}). Used to
     * resolve the officer's zone scope when the external master-data service
     * is unreachable.
     */
    @Column(name = "zone_id")
    private UUID zoneId;

    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfficialRole role = OfficialRole.R_HORTIC_OFF;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    /** BCrypt hash — never exposed through the API. */
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime createdAt;

    // ── UserDetails ───────────────────────────────────────────

    /** Login identifier: officials sign in with their e-mail address. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Grants {@code ROLE_<role>} so Spring Security can enforce
     * {@code hasRole('R_HORTIC_ADM')} / {@code hasRole('R_HORTIC_OFF')} etc.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
