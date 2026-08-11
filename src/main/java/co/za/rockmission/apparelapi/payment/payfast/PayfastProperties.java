package co.za.rockmission.apparelapi.payment.payfast;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payfast")
public record PayfastProperties(
        String merchantId,
        String merchantKey,
        String passphrase,
        boolean sandbox,
        String returnUrl,
        String cancelUrl,
        String notifyUrl) {

    public String processUrl() {
        return sandbox ? "https://sandbox.payfast.co.za/eng/process" : "https://www.payfast.co.za/eng/process";
    }

    public String validateUrl() {
        return sandbox
                ? "https://sandbox.payfast.co.za/eng/query/validate"
                : "https://www.payfast.co.za/eng/query/validate";
    }
}
