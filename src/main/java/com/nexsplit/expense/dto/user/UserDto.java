package com.nexsplit.expense.dto.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nexsplit.expense.model.User.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto { // use @Valid annotation in controller parameter
    private String id;

    @NotBlank(message = "User name is mandatory")
    private String name;

    @NotBlank(message = "User email is mandatory")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Size(min = 10, max = 10)
    private int contactNumber;

    @NotBlank
    private Role role;
}
