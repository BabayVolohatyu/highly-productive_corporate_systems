package org.example.monitoring.security;

import com.nimbusds.jwt.SignedJWT;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return fromRealmAccess(jwt.getClaim("realm_access"));
    }

    public Collection<GrantedAuthority> fromAccessToken(String tokenValue) {
        try {
            Object realmAccess = SignedJWT.parse(tokenValue).getJWTClaimsSet().getClaim("realm_access");
            return fromRealmAccess(realmAccess);
        } catch (ParseException exception) {
            return List.of();
        }
    }

    public Collection<GrantedAuthority> fromRealmAccess(Object realmAccess) {
        if (!(realmAccess instanceof Map<?, ?> claims)) {
            return List.of();
        }
        Object roles = claims.get("roles");
        if (!(roles instanceof Collection<?> roleNames)) {
            return List.of();
        }
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (Object role : roleNames) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return authorities;
    }
}
