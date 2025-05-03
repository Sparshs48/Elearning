// src/main/java/com/example/demo/config/MailConfig.java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        // returns a do-nothing sender; you won’t actually send mail
        return new JavaMailSenderImpl();
    }
}
