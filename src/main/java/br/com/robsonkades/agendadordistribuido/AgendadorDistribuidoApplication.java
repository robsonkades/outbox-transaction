package br.com.robsonkades.agendadordistribuido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgendadorDistribuidoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendadorDistribuidoApplication.class, args);
    }
}
