package com.sparta.msa_exam.product.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class ServerPortInterceptor implements HandlerInterceptor {

    private final Environment environment;

    public ServerPortInterceptor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
        Object handler, ModelAndView modelAndView) {
        // "server.port"로 포트를 가져오기
        String serverPort = environment.getProperty("server.port", "Unknown Port");
        response.setHeader("Server-Port", serverPort);
        System.out.println("Server-Port added: " + serverPort);
    }
}
