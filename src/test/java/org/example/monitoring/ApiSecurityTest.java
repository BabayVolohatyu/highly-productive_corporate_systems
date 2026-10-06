package org.example.monitoring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiSecurityTest.StubIdentity.class)
@Testcontainers
class ApiSecurityTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedApiRequestIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/servers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice", roles = "ADMIN")
    void adminCanCreateReadUpdateAndDelete() throws Exception {
        String created = mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serverJson("admin-host", "10.0.0.8", "PROD", "UP", "edge")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostname").value("admin-host"))
                .andExpect(jsonPath("$.environment").value("PROD"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/servers/" + id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ipAddress").value("10.0.0.8"));

        mockMvc.perform(put("/api/servers/" + id.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serverJson("admin-host", "10.0.0.9", "STAGE", "MAINTENANCE", "patched")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"))
                .andExpect(jsonPath("$.ipAddress").value("10.0.0.9"));

        mockMvc.perform(get("/api/admin/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverCount").isNumber());

        mockMvc.perform(delete("/api/servers/" + id.longValue()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "bob", roles = "OPERATOR")
    void operatorIsForbiddenOnAdminPathAndOnDelete() throws Exception {
        String created = mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(serverJson("ops-host", "10.1.0.4", "DEV", "UNKNOWN", "")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Number id = com.jayway.jsonpath.JsonPath.read(created, "$.id");

        mockMvc.perform(get("/api/admin/overview"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/servers/" + id.longValue()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/servers/" + id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hostname").value("ops-host"));
    }

    private static String serverJson(String hostname, String ip, String environment, String status, String description) {
        return """
                {"hostname":"%s","ipAddress":"%s","environment":"%s","status":"%s","description":"%s"}
                """.formatted(hostname, ip, environment, status, description);
    }

    @TestConfiguration
    static class StubIdentity {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                    .clientId("monitoring-ui")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid")
                    .authorizationUri("http://127.0.0.1:9/oauth/authorize")
                    .tokenUri("http://127.0.0.1:9/oauth/token")
                    .jwkSetUri("http://127.0.0.1:9/oauth/jwks")
                    .userInfoUri("http://127.0.0.1:9/oauth/userinfo")
                    .userNameAttributeName("sub")
                    .build();
            return new InMemoryClientRegistrationRepository(registration);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test")
                    .build();
        }
    }
}
