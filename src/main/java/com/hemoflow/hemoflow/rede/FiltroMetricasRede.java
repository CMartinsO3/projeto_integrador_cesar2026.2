package com.hemoflow.hemoflow.rede;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class FiltroMetricasRede extends OncePerRequestFilter {

    private final MetricasRedeService metricasRedeService;

    public FiltroMetricasRede(MetricasRedeService metricasRedeService) {
        this.metricasRedeService = metricasRedeService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isRecursoEstatico(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = Math.max(0, System.currentTimeMillis() - inicio);
            String rotaNormalizada = extrairRotaNormalizada(request);
            String metodo = request.getMethod();
            int statusCode = response.getStatus();
            metricasRedeService.registrar(metodo, rotaNormalizada, duracaoMs, statusCode);
        }
    }

    private boolean isRecursoEstatico(String uri) {
        if (uri == null) {
            return false;
        }
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".ico")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".svg");
    }

    private String extrairRotaNormalizada(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern instanceof String str && !str.isBlank()) {
            return str;
        }
        return normalizarUriFallback(request.getRequestURI());
    }

    public static String normalizarUriFallback(String uri) {
        if (uri == null || uri.isBlank()) {
            return "/";
        }
        return uri.replaceAll("/\\d+(?=(/|$))", "/{id}")
                .replaceAll("/[0-9a-fA-F-]{36}(?=(/|$))", "/{id}");
    }
}

