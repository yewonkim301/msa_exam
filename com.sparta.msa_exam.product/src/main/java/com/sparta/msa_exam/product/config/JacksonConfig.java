package com.sparta.msa_exam.product.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Jackson2ObjectMapperBuilder를 통해 ObjectMapper 생성
        return Jackson2ObjectMapperBuilder
            .json()
            .modulesToInstall(new JavaTimeModule())  // Java 8 시간 처리 모듈 등록
            .build();
    }
}
