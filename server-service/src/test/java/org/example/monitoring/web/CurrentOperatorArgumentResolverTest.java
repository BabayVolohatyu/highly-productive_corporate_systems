package org.example.monitoring.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

import java.lang.reflect.Method;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class CurrentOperatorArgumentResolverTest {

    private final CurrentOperatorArgumentResolver resolver = new CurrentOperatorArgumentResolver();

    @BeforeEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void supportsParameter_withAnnotationAndString_returnsTrue() throws Exception {
        MethodParameter parameter = annotatedOperatorParameter();

        assertThat(resolver.supportsParameter(parameter)).isTrue();
    }

    @Test
    void supportsParameter_withoutAnnotation_returnsFalse() throws Exception {
        Method method = SampleController.class.getDeclaredMethod("plain", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        assertThat(resolver.supportsParameter(parameter)).isFalse();
    }

    @Test
    void resolveArgument_jwtPreferredUsername_returnsAlice() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "alice")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        Object resolved = resolver.resolveArgument(
                annotatedOperatorParameter(), null, null, null);

        assertThat(resolved).isEqualTo("alice");
    }

    private MethodParameter annotatedOperatorParameter() throws Exception {
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        return parameter;
    }

    static class SampleController {
        void sample(@CurrentOperator String operator) {
        }

        void plain(String operator) {
        }
    }
}
