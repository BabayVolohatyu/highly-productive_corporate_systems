package org.example.monitoring.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.stream.Collectors;

@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("anonymous")
    public boolean anonymous(Authentication authentication) {
        return !known(authentication);
    }

    @ModelAttribute("username")
    public String username(Authentication authentication) {
        return known(authentication) ? authentication.getName() : "";
    }

    @ModelAttribute("roles")
    public String roles(Authentication authentication) {
        if (!known(authentication)) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        if (!known(authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean known(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
