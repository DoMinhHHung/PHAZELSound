package iuh.fit.se.phazelsound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PhazelSoundApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhazelSoundApplication.class, args);
    }

}
