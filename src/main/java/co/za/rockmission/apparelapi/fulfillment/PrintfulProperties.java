package co.za.rockmission.apparelapi.fulfillment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "printful")
public record PrintfulProperties(boolean enabled, String apiKey, String apiBaseUrl) {
}
