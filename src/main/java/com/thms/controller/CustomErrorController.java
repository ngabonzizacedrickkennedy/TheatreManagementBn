package com.thms.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorPage = "error/error";  // Default error page
        String errorMessage = "An unexpected error occurred";
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorPage = "error/404";
                errorMessage = "Page not found";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorPage = "error/500";
                errorMessage = "Internal server error";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorPage = "error/403";
                errorMessage = "Access denied";
            }
            
            // Add status code to model
            model.addAttribute("statusCode", statusCode);
        }
        
        // Add error message to model
        model.addAttribute("errorMessage", errorMessage);
        
        // You can also add the original exception message if available
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (exception != null) {
            model.addAttribute("exceptionMessage", ((Throwable) exception).getMessage());
        }
        
        return errorPage;
    }
}