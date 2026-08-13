package co.za.rockmission.apparelapi.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer(AppProperties appProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedOrigins = Arrays.stream(appProperties.frontendUrl().split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .toArray(String[]::new);

                registry.addMapping("/api/**")
                    .allowedOriginPatterns(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Location");
            }
        };
    }
}
