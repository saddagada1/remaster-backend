package com.saivamsi.remaster.configuration;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfiguration {
    @Value("${RESEND_KEY}")
    private String resendKey;

    @Bean
    public Resend resendClient() {
        return new Resend(resendKey);
    }
}
