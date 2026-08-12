package co.za.rockmission.apparelapi.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    private UUID id = UUID.randomUUID();

    private String firstName;

    private String lastName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public void touch() {
        this.updatedAt = Instant.now();
    }
}
