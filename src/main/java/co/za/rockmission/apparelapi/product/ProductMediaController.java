package co.za.rockmission.apparelapi.product;

import co.za.rockmission.apparelapi.storage.StorageService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class ProductMediaController {

    private final StorageService storageService;

    @GetMapping("/products/{filename:.+}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String filename) {
        StorageService.StoredObject image = storageService.getProductImage(filename);

        MediaType mediaType;
        try {
            mediaType = image.contentType() == null || image.contentType().isBlank()
                    ? MediaType.IMAGE_WEBP
                    : MediaType.parseMediaType(image.contentType());
        } catch (IllegalArgumentException ex) {
            mediaType = MediaType.IMAGE_WEBP;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.X_CONTENT_TYPE_OPTIONS, "nosniff")
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(image.content());
    }
}
