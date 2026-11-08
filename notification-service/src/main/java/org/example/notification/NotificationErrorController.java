package org.example.notification;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NotificationErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        int status = status(request);
        model.addAttribute("status", status);
        model.addAttribute("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        model.addAttribute("message", status == HttpStatus.NOT_FOUND.value()
                ? "That path is not part of the notification service."
                : "The notification service could not complete the request.");
        return "error";
    }

    private static int status(HttpServletRequest request) {
        Object value = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (value instanceof Integer code) {
            return code;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
