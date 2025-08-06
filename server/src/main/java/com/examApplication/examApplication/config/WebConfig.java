package com.examApplication.examApplication.config;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${certificate.output.directory}")
    private String outputDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourcePath = "file:" + outputDirectory + "/";
        System.out.println("✅ Mapping '/certificates/**' to: " + resourcePath);

        registry.addResourceHandler("/certificates/**")
                .addResourceLocations(resourcePath);
    }
}


