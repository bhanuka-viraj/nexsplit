package com.nexsplit.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserDto {
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    private String username;

    @Pattern(regexp = "^\\d{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNumber;
}
