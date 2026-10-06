package org.example.monitoring.server;

import jakarta.validation.Valid;
import org.example.monitoring.web.CreateServer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping
    public List<ServerResponse> list() {
        return serverService.findAll();
    }

    @GetMapping("/{id}")
    public ServerResponse get(@PathVariable Long id) {
        return serverService.findById(id);
    }

    @GetMapping("/{id}/unmetered")
    public ServerResponse getUnmetered(@PathVariable Long id) {
        return serverService.findByIdUnmetered(id);
    }

    @GetMapping("/{id}/masked")
    public ServerResponse getMasked(@PathVariable Long id) {
        return serverService.findByIdMasked(id);
    }

    @CreateServer
    public ServerResponse create(@Valid @RequestBody ServerRequest request) {
        return serverService.create(request);
    }

    @PutMapping("/{id}")
    public ServerResponse update(@PathVariable Long id, @Valid @RequestBody ServerRequest request) {
        return serverService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        serverService.delete(id);
    }
}
