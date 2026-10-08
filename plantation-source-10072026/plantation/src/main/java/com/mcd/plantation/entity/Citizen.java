package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

// ════════════════════════════════════════════════════════════════
//  CITIZEN  — local credential store for the citizen role
// ════════════════════════════════════════════════════════════════
@Entity
@Table(name = "citizens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Citizen implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "citizen_id")
    private UUID citizenId;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(length = 4)
    private String aadhaarLast4;

    @Column(columnDefinition = "TEXT")
    private String address;

    /** BCrypt hash — never exposed through the API. */
    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private OffsetDateTime registeredAt;

    private OffsetDateTime lastLoginAt;

    // ── UserDetails ───────────────────────────────────────────

    /** Login identifier: the citizen signs in with their e-mail address. */
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CITIZEN"));
    }
}
