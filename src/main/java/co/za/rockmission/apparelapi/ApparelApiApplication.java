package co.za.rockmission.apparelapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ApparelApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApparelApiApplication.class, args);
    }
}
