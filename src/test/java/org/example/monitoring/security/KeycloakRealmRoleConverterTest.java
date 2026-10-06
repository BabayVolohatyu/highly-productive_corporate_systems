package org.example.monitoring.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void fromRealmAccess_validMap_returnsPrefixedDistinctRoles() {
        // Arrange
        Map<String, Object> realmAccess = Map.of("roles", List.of("ADMIN", "ADMIN", "OPERATOR"));

        // Act
        Collection<GrantedAuthority> authorities = converter.fromRealmAccess(realmAccess);

        // Assert
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN", "ROLE_OPERATOR");
    }

    @Test
    void fromRealmAccess_notAMap_returnsEmptyList() {
        // Act
        Collection<GrantedAuthority> authorities = converter.fromRealmAccess("not-a-map");

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void fromRealmAccess_missingRolesCollection_returnsEmptyList() {
        // Act
        Collection<GrantedAuthority> authorities = converter.fromRealmAccess(Map.of("other", List.of("X")));

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void fromAccessToken_malformedToken_returnsEmptyList() {
        // Act
        Collection<GrantedAuthority> authorities = converter.fromAccessToken("not.a.jwt");

        // Assert
        assertThat(authorities).isEmpty();
    }

    @Test
    void convert_jwtWithRealmAccess_returnsAuthorities() {
        // Arrange
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("OPERATOR")))
                .build();

        // Act
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Assert
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_OPERATOR");
    }
}
