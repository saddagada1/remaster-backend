package com.saivamsi.remaster.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Service
@RequiredArgsConstructor
public class ResendService {

    private final Resend resendClient;
    private final ResourceLoader resourceLoader;
    @Value("${CLIENT_DOMAIN}")
    private String clientDomain;

    public void sendVerificationEmail(String recipient, String greeting, String token) {
        Resource resource = resourceLoader.getResource("classpath:/templates/" + "verify-email.html");
        GlobalException readError = new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalError.builder().subject("email").subject("could not load email template").build());
        String template;
        if (resource.exists()) {
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                template = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw readError;
            }
        } else {
            throw readError;
        }
        template = template.replace("${GREETING}", greeting);
        template = template.replace("${VERIFY_LINK}", clientDomain + "/verify?token=" + token);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from("Remaster <onboarding@saivamsi.ca>")
                .to(recipient)
                .subject("Verify Email")
                .html(template)
                .build();
        try {
            resendClient.emails().send(options);
        } catch (ResendException e) {
           throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalError.builder().subject("email").subject("could not send verification email").build());
        }
    }

    public void sendForgotPasswordEmail(String recipient, String token) {
        Resource resource = resourceLoader.getResource("classpath:/templates/" + "forgot-password-email.html");
        GlobalException readError = new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalError.builder().subject("email").subject("could not load email template").build());
        String template;
        if (resource.exists()) {
            try (Reader reader = new InputStreamReader(resource.getInputStream())) {
                template = FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw readError;
            }
        } else {
            throw readError;
        }
        template = template.replace("${FORGOT_PASSWORD_LINK}", clientDomain + "/forgot-password/reset?token=" + token);

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from("Remaster <onboarding@saivamsi.ca>")
                .to(recipient)
                .subject("Forgot Password")
                .html(template)
                .build();
        try {
            resendClient.emails().send(options);
        } catch (ResendException e) {
            throw new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR, GlobalError.builder().subject("email").subject("could not send forgot password email").build());
        }
    }
}
