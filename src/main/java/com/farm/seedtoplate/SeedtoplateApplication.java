package com.farm.seedtoplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.farm.seedtoplate.config.RazorpayProperties;

@SpringBootApplication
@EnableConfigurationProperties(RazorpayProperties.class)
public class SeedtoplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeedtoplateApplication.class, args);
    }
}
