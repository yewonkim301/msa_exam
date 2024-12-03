package com.sparta.msa_exam.product.config;

import com.sparta.msa_exam.product.interceptor.ServerPortInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ServerPortInterceptor serverPortInterceptor;

    public WebConfig(ServerPortInterceptor serverPortInterceptor) {
        this.serverPortInterceptor = serverPortInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(serverPortInterceptor).addPathPatterns("/products/**");
    }
}
