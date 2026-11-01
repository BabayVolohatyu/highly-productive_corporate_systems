package org.example.monitoring.web;

import org.example.monitoring.server.ConflictException;
import org.example.monitoring.server.InvalidRequestException;
import org.example.monitoring.server.NotFoundException;
import org.example.monitoring.server.ServerFormOptions;
import org.example.monitoring.server.ServerRequest;
import org.example.monitoring.server.ServerResponse;
import org.example.monitoring.server.ServerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerPageControllerTest {

    @Mock
    private ServerService serverService;

    @InjectMocks
    private ServerPageController controller;

    @Mock
    private BindingResult bindingResult;

    @Test
    void list_addsServersToModel() {
        // Arrange
        ServerResponse row = sampleResponse(1L);
        when(serverService.findAll()).thenReturn(List.of(row));
        ConcurrentModel model = new ConcurrentModel();

        // Act
        String view = controller.list(model);

        // Assert
        assertThat(view).isEqualTo("servers/list");
        assertThat(model.getAttribute("servers")).isEqualTo(List.of(row));
    }

    @Test
    void createForm_withoutExistingModel_addsDefaults() {
        // Arrange
        when(serverService.formOptions()).thenReturn(new ServerFormOptions(List.of(), List.of()));
        ConcurrentModel model = new ConcurrentModel();

        // Act
        String view = controller.createForm(model);

        // Assert
        assertThat(view).isEqualTo("servers/form");
        ServerRequest server = (ServerRequest) model.getAttribute("server");
        assertThat(server.environment()).isEqualTo("DEV");
        assertThat(server.status()).isEqualTo("UNKNOWN");
    }

    @Test
    void create_bindingErrors_returnsFormView() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(true);
        when(serverService.formOptions()).thenReturn(new ServerFormOptions(List.of(), List.of()));
        ConcurrentModel model = new ConcurrentModel();
        ServerRequest request = new ServerRequest("", "", "DEV", "UP", "");

        // Act
        String view = controller.create(request, bindingResult, model, new RedirectAttributesModelMap());

        // Assert
        assertThat(view).isEqualTo("servers/form");
        verify(serverService, never()).create(any());
    }

    @Test
    void create_conflictException_returnsFormWithError() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(serverService.formOptions()).thenReturn(new ServerFormOptions(List.of(), List.of()));
        ServerRequest request = new ServerRequest("dup", "1.1.1.1", "DEV", "UP", "");
        doThrow(new ConflictException("Hostname already exists")).when(serverService).create(request);
        ConcurrentModel model = new ConcurrentModel();

        // Act
        String view = controller.create(request, bindingResult, model, new RedirectAttributesModelMap());

        // Assert
        assertThat(view).isEqualTo("servers/form");
        assertThat(model.getAttribute("error")).isEqualTo("Hostname already exists");
    }

    @Test
    void create_success_redirectsToList() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        ServerRequest request = new ServerRequest("new", "1.1.1.1", "DEV", "UP", "");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = controller.create(request, bindingResult, new ConcurrentModel(), redirect);

        // Assert
        assertThat(view).isEqualTo("redirect:/servers");
        verify(serverService).create(request);
        assertThat(flash(redirect).get("message")).isEqualTo("Server created");
    }

    @Test
    void update_notFound_redirectsWithMessage() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        ServerRequest request = new ServerRequest("h", "1.1.1.1", "DEV", "UP", "");
        doThrow(new NotFoundException("Server 9 was not found")).when(serverService).update(9L, request);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = controller.update(9L, request, bindingResult, new ConcurrentModel(), redirect);

        // Assert
        assertThat(view).isEqualTo("redirect:/servers");
        assertThat(flash(redirect).get("message")).isEqualTo("Server 9 was not found");
    }

    @Test
    void update_invalidRequest_returnsFormWithError() {
        // Arrange
        when(bindingResult.hasErrors()).thenReturn(false);
        when(serverService.formOptions()).thenReturn(new ServerFormOptions(List.of(), List.of()));
        ServerRequest request = new ServerRequest("h", " ", "DEV", "UP", "");
        doThrow(new InvalidRequestException("ip_address is required")).when(serverService).update(2L, request);
        ConcurrentModel model = new ConcurrentModel();

        // Act
        String view = controller.update(2L, request, bindingResult, model, new RedirectAttributesModelMap());

        // Assert
        assertThat(view).isEqualTo("servers/form");
        assertThat(model.getAttribute("error")).isEqualTo("ip_address is required");
    }

    @Test
    void delete_success_setsFlashMessage() {
        // Arrange
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = controller.delete(3L, redirect);

        // Assert
        assertThat(view).isEqualTo("redirect:/servers");
        verify(serverService).delete(3L);
        assertThat(flash(redirect).get("message")).isEqualTo("Server deleted");
    }

    @Test
    void delete_notFound_setsFlashMessage() {
        // Arrange
        doThrow(new NotFoundException("Server 404 was not found")).when(serverService).delete(404L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        // Act
        String view = controller.delete(404L, redirect);

        // Assert
        assertThat(view).isEqualTo("redirect:/servers");
        assertThat(flash(redirect).get("message")).isEqualTo("Server 404 was not found");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> flash(RedirectAttributes redirect) {
        return (Map<String, ?>) redirect.getFlashAttributes();
    }

    private static ServerResponse sampleResponse(long id) {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return new ServerResponse(id, "host", "1.1.1.1", "DEV", "UP", "", now, now);
    }
}
