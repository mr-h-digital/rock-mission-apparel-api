package co.za.rockmission.apparelapi.fulfillment;

import co.za.rockmission.apparelapi.order.Order;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Submits a paid order to Printful for print-on-demand fulfillment.
 *
 * <p>Disabled by default ({@code printful.enabled=false}) until a real Printful account exists
 * and the catalog's products are mapped to Printful sync variant IDs - without that mapping
 * there is nothing correct to submit, so this deliberately only logs until it's configured.
 */
@Service
@RequiredArgsConstructor
public class PrintfulService {

    private static final Logger log = LoggerFactory.getLogger(PrintfulService.class);

    private final PrintfulProperties properties;
    private final RestClient.Builder restClientBuilder;

    public void submitOrder(Order order) {
        if (!properties.enabled()) {
            log.info(
                    "Printful fulfillment disabled - order {} marked paid but not auto-submitted for fulfillment. "
                            + "Set printful.enabled=true once a Printful account + product mapping exists.",
                    order.getId());
            return;
        }

        // Printful's Order Create API expects each line item's Printful sync variant ID, which
        // requires mapping this catalog's product/size/color combinations to Printful's catalog
        // once the real garments are set up there - that mapping doesn't exist yet, so this is
        // intentionally left as the integration point rather than a guessed implementation.
        log.warn(
                "Printful is enabled but order-to-sync-variant mapping is not implemented yet; "
                        + "order {} was NOT submitted for fulfillment. Implement the mapping in "
                        + "PrintfulService before relying on this in production.",
                order.getId());
    }
}
