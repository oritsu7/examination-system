package com.ansh.examsystem.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String redirectUrl = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .findFirst()
                .map(role -> switch (role) {
                    case "ROLE_ADMIN" -> "/admin";
                    case "ROLE_TEACHER" -> "/teacher";
                    case "ROLE_STUDENT" -> "/student";
                    default -> "/";
                })
                .orElse("/");

        response.sendRedirect(redirectUrl);
    }
}