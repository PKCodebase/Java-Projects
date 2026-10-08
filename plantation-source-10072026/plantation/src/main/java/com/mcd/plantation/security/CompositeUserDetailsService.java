package com.mcd.plantation.security;

import com.mcd.plantation.repository.CitizenRepository;
import com.mcd.plantation.repository.McdOfficialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resolves a login (e-mail) to either a {@code Citizen} or an
 * {@code McdOfficial} so one {@code AuthenticationManager} can authenticate
 * all three roles: R_HORTIC_ADM, R_HORTIC_OFF and CITIZEN.
 */
@Service
@RequiredArgsConstructor
public class CompositeUserDetailsService implements UserDetailsService {

    private final CitizenRepository citizenRepo;
    private final McdOfficialRepository officialRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return citizenRepo.findByEmail(email)
                .map(c -> (UserDetails) c)
                .or(() -> officialRepo.findByEmail(email).map(o -> (UserDetails) o))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
