package com.hemoflow.hemoflow.config;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AutorizacaoOperacionalInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        Set<String> allowedRoles = requiredRoles(path, method);
        if (allowedRoles.isEmpty()) {
            return true;
        }

        Object roleAttribute = request.getSession(false) == null
                ? null
                : request.getSession(false).getAttribute("role");
        String role = roleAttribute instanceof String value ? value : null;
        if (role != null && allowedRoles.contains(role)) {
            if (path.startsWith("/api/") && !Set.of("GET", "HEAD", "OPTIONS").contains(method)
                    && !isSameOrigin(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"erro\":\"Origem da requisição não autorizada\"}");
                return false;
            }
            return true;
        }

        if (path.startsWith("/api/")) {
            response.setStatus(role == null
                    ? HttpServletResponse.SC_UNAUTHORIZED
                    : HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(role == null
                    ? "{\"erro\":\"Autenticação necessária\"}"
                    : "{\"erro\":\"Perfil sem permissão para esta operação\"}");
        } else if (role == null) {
            String loginPath = path.equals("/motorista")
                    ? "/login-motorista"
                    : path.equals("/requisicoes-hospital")
                            ? "/login-hospital"
                            : "/login-admin";
            response.sendRedirect(loginPath);
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    private Set<String> requiredRoles(String path, String method) {
        if (path.equals("/cadastro-operacional") || path.equals("/api/v1/auth/cadastro-operacional")) {
            return Set.of();
        }
        if (path.matches("/api/v1/auth/usuarios/\\d+/aprovar")) {
            return Set.of("ADMIN");
        }
        if (path.matches("/api/v1/bolsas/hospital/\\d+/estoque-mock")) {
            return Set.of();
        }
        if (path.equals("/requisicoes-hospital")) {
            return Set.of("HOSPITAL", "ADMIN");
        }
        if (path.equals("/motorista")) {
            return Set.of("MOTORISTA", "ADMIN");
        }
        if (path.equals("/dashboard-admin") || path.equals("/contas-operacionais")
                || path.equals("/bolsas-painel") || path.equals("/monitoramento")
                || path.startsWith("/api/v1/bolsas")
                || path.startsWith("/api/v1/nos-rede")
                || path.startsWith("/api/v1/monitoramento")
                || path.equals("/api/v1/auth/usuarios")) {
            return Set.of("ADMIN");
        }
        if (path.equals("/api/v1/requisicoes") || path.matches("/api/v1/requisicoes/\\d+")) {
            if (path.equals("/api/v1/requisicoes")
                    && ("GET".equals(method) || "POST".equals(method))
                    || path.matches("/api/v1/requisicoes/\\d+") && "GET".equals(method)) {
                return Set.of("HOSPITAL", "ADMIN");
            }
            return Set.of("ADMIN");
        }
        if (path.startsWith("/api/v1/entregas")) {
            if (path.matches("/api/v1/entregas/\\d+/motorista")) {
                return Set.of("ADMIN");
            }
            return Set.of("MOTORISTA", "ADMIN");
        }
        return Set.of();
    }

    private boolean isSameOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null) {
            String referer = request.getHeader("Referer");
            if (referer == null) {
                return true;
            }
            origin = referer;
        }
        try {
            URI source = URI.create(origin);
            String host = request.getHeader("Host");
            return source.getScheme() != null
                    && source.getScheme().equalsIgnoreCase(request.getScheme())
                    && source.getAuthority() != null
                    && host != null
                    && source.getAuthority().equalsIgnoreCase(host);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
