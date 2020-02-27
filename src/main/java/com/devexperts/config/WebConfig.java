package com.devexperts.config;

import com.devexperts.converter.AccountDtoToAccountConverter;
import com.devexperts.converter.AccountToDtoConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new AccountToDtoConverter());
        registry.addConverter(new AccountDtoToAccountConverter());
    }
}

