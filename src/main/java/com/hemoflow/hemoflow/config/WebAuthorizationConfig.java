package com.hemoflow.hemoflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAuthorizationConfig implements WebMvcConfigurer {

    private final AutorizacaoOperacionalInterceptor autorizacaoInterceptor;

    public WebAuthorizationConfig(AutorizacaoOperacionalInterceptor autorizacaoInterceptor) {
        this.autorizacaoInterceptor = autorizacaoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(autorizacaoInterceptor);
    }
}
