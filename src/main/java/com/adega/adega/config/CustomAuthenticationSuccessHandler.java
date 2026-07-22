package com.adega.adega.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isEmployee = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        boolean isClient = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));

        if (isAdmin || isEmployee) {
            response.sendRedirect("/admin/home");
            return;
        }

        if(isClient) {
            response.sendRedirect("/");
            return;
        }

        response.sendRedirect("/");
    }
}
