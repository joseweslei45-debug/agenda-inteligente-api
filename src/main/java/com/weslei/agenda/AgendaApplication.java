package com.weslei.agenda;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendaApplication.class, args);
    }

    /** Relógio injetável: permite testar regras de tempo de forma determinística. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
