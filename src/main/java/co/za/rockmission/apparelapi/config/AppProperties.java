package co.za.rockmission.apparelapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String frontendUrl, String jwtSecret, long jwtExpiryHours) {
}
