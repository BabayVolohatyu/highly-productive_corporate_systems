package org.example.monitoring.server;

import org.example.monitoring.aop.CallMeter;
import org.example.monitoring.aop.OperationTimings;
import org.example.monitoring.config.WebMvcConfig;
import org.example.monitoring.web.CurrentOperatorArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOverviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({WebMvcConfig.class, CurrentOperatorArgumentResolver.class})
class AdminOverviewControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerService serverService;

    @MockitoBean
    private CallMeter callMeter;

    @MockitoBean
    private OperationTimings operationTimings;

    @Test
    @WithMockUser(username = "alice", roles = "ADMIN")
    void overview_resolvesCurrentOperatorAndServerCount() throws Exception {
        when(serverService.countServers()).thenReturn(5L);
        when(callMeter.getFindByIdEntries()).thenReturn(2L);
        when(operationTimings.getLastListMillis()).thenReturn(11L);

        mockMvc.perform(get("/api/admin/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverCount").value(5))
                .andExpect(jsonPath("$.requestedBy").value("alice"))
                .andExpect(jsonPath("$.findByIdEntries").value(2))
                .andExpect(jsonPath("$.lastListMillis").value(11));
    }
}
