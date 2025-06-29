package com.tlback.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TlbackApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TlbackApplication.class, args);
    }

}
