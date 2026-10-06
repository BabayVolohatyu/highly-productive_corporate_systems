package org.example.monitoring.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class ServerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerService serverService;

    @Test
    void create_validBody_returns201() throws Exception {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        ServerResponse created = new ServerResponse(1L, "edge-1", "10.0.0.15", "PROD", "UP", "edge", now, now);
        when(serverService.create(any(ServerRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "edge-1",
                                  "ipAddress": "10.0.0.15",
                                  "environment": "PROD",
                                  "status": "UP",
                                  "description": "edge proxy"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hostname").value("edge-1"));
    }

    @Test
    void create_prodWithUnknown_returns400WithExplicitMessage() throws Exception {
        mockMvc.perform(post("/api/servers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "hostname": "bad-prod",
                                  "ipAddress": "10.0.0.99",
                                  "environment": "PROD",
                                  "status": "UNKNOWN",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsStringIgnoringCase("production")))
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsStringIgnoringCase("UNKNOWN")));
    }
}
