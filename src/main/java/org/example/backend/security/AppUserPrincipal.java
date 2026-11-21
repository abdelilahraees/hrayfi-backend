package org.example.backend.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.backend.user.entity.AppUser;
import org.example.backend.user.entity.Artisan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter Spring Security autour de notre AppUser.
 * Expose l'id user via {@link #getId()} pour que les contrôleurs puissent
 * récupérer l'utilisateur courant sans refaire un lookup en base.
 */
@RequiredArgsConstructor
@Getter
public class AppUserPrincipal implements UserDetails {

    private final AppUser user;

    public Long getId() {
        return user.getId();
    }

    /** Un artisan non validé (en attente, refusé ou suspendu) ne peut ni se connecter ni utiliser un ancien jeton. */
    public boolean isActif() {
        return !(user instanceof Artisan artisan) || artisan.isValide();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String getPassword() { return user.getPassword(); }
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
