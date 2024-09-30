package com.tlback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class TlbackApplication {

    public static void main(final String[] args) {
        SpringApplication.run(TlbackApplication.class, args);
    }

}
