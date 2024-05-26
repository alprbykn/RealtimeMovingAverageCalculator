package com.example.realtimemovingavaragecalculator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${windowSize}")
    private Integer windowSize;

    public Integer getWindowSize() {
        return windowSize;
    }
}
