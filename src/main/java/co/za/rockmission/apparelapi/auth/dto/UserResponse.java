package co.za.rockmission.apparelapi.auth.dto;

import co.za.rockmission.apparelapi.auth.AppUser;
import java.util.UUID;

public record UserResponse(UUID id, String firstName, String lastName, String email) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
