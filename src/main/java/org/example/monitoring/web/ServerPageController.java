package org.example.monitoring.web;

import jakarta.validation.Valid;
import org.example.monitoring.server.ConflictException;
import org.example.monitoring.server.InvalidRequestException;
import org.example.monitoring.server.NotFoundException;
import org.example.monitoring.server.ServerRequest;
import org.example.monitoring.server.ServerResponse;
import org.example.monitoring.server.ServerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ServerPageController {

    private final ServerService serverService;

    public ServerPageController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/servers")
    public String list(Model model) {
        model.addAttribute("servers", serverService.findAll());
        return "servers/list";
    }

    @GetMapping("/servers/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("server")) {
            model.addAttribute("server", new ServerRequest("", "", "DEV", "UNKNOWN", ""));
        }
        prepareForm(model, false, null);
        return "servers/form";
    }

    @PostMapping("/servers")
    public String create(@Valid ServerRequest server,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepareForm(model, false, null);
            return "servers/form";
        }
        try {
            serverService.create(server);
        } catch (ConflictException | InvalidRequestException exception) {
            binding.reject("server", exception.getMessage());
            model.addAttribute("error", exception.getMessage());
            prepareForm(model, false, null);
            return "servers/form";
        }
        redirect.addFlashAttribute("message", "Server created");
        return "redirect:/servers";
    }

    @GetMapping("/servers/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ServerResponse existing = serverService.findById(id);
        if (!model.containsAttribute("server")) {
            model.addAttribute("server", new ServerRequest(
                    existing.hostname(),
                    existing.ipAddress(),
                    existing.environment(),
                    existing.status(),
                    existing.description() == null ? "" : existing.description()));
        }
        prepareForm(model, true, id);
        return "servers/form";
    }

    @PostMapping("/servers/{id}")
    public String update(@PathVariable Long id,
                         @Valid ServerRequest server,
                         BindingResult binding,
                         Model model,
                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            prepareForm(model, true, id);
            return "servers/form";
        }
        try {
            serverService.update(id, server);
        } catch (NotFoundException exception) {
            redirect.addFlashAttribute("message", exception.getMessage());
            return "redirect:/servers";
        } catch (ConflictException | InvalidRequestException exception) {
            binding.reject("server", exception.getMessage());
            model.addAttribute("error", exception.getMessage());
            prepareForm(model, true, id);
            return "servers/form";
        }
        redirect.addFlashAttribute("message", "Server updated");
        return "redirect:/servers";
    }

    @PostMapping("/servers/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            serverService.delete(id);
            redirect.addFlashAttribute("message", "Server deleted");
        } catch (NotFoundException exception) {
            redirect.addFlashAttribute("message", exception.getMessage());
        }
        return "redirect:/servers";
    }

    private void prepareForm(Model model, boolean editing, Long serverId) {
        model.addAttribute("options", serverService.formOptions());
        model.addAttribute("editing", editing);
        model.addAttribute("serverId", serverId);
    }
}
