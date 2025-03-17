package com.thms.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;

public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        if (roles.contains("ROLE_ADMIN")) {
            // Redirect admin to dashboard
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_MANAGER")) {
            // Redirect managers to manager dashboard (if exists)
            response.sendRedirect("/admin/dashboard");
        } else {
            // Default redirect for regular users
            response.sendRedirect("/");
        }
    }
}