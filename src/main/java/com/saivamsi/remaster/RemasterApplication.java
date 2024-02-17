package com.saivamsi.remaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemasterApplication.class, args);
    }

}
