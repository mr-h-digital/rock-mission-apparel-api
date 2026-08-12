package co.za.rockmission.apparelapi.auth.dto;

import co.za.rockmission.apparelapi.auth.AppUser;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String province,
        String postalCode,
        String country) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddressLine1(),
                user.getAddressLine2(),
                user.getCity(),
                user.getProvince(),
                user.getPostalCode(),
                user.getCountry()
        );
    }
}
