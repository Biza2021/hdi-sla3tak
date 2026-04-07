package com.hdisla3tak.app;

import com.hdisla3tak.app.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class HdiSla3takApplication {

    public static void main(String[] args) {
        SpringApplication.run(HdiSla3takApplication.class, args);
    }
}
