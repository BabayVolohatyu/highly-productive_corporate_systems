package org.example.monitoring.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserAdviceTest {

    private final CurrentUserAdvice advice = new CurrentUserAdvice();

    @Test
    void anonymous_nullAuthentication_marksAnonymousAndEmptyUsername() {
        // Act
        boolean anonymous = advice.anonymous(null);
        String username = advice.username(null);
        String roles = advice.roles(null);
        boolean isAdmin = advice.isAdmin(null);

        // Assert
        assertThat(anonymous).isTrue();
        assertThat(username).isEmpty();
        assertThat(roles).isEmpty();
        assertThat(isAdmin).isFalse();
    }

    @Test
    void anonymous_anonymousToken_marksAnonymous() {
        // Arrange
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        // Act / Assert
        assertThat(advice.anonymous(authentication)).isTrue();
        assertThat(advice.username(authentication)).isEmpty();
    }

    @Test
    void knownUser_returnsUsernameRolesAndAdminFlag() {
        // Arrange
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "alice",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_OPERATOR")));

        // Act
        boolean anonymous = advice.anonymous(authentication);
        String username = advice.username(authentication);
        String roles = advice.roles(authentication);
        boolean isAdmin = advice.isAdmin(authentication);

        // Assert
        assertThat(anonymous).isFalse();
        assertThat(username).isEqualTo("alice");
        assertThat(roles).contains("ROLE_ADMIN").contains("ROLE_OPERATOR");
        assertThat(isAdmin).isTrue();
    }
}
