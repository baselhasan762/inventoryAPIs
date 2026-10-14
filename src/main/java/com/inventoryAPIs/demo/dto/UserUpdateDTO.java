package com.inventoryAPIs.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Profile fields a user may change on their own account; null fields are left unchanged. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class UserUpdateDTO {

    private static final String NOT_BLANK = ".*\\S.*";

    @Email
    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 150)
    private String email;

    @Size(min = 8, max = 72)
    private String password;

    @Pattern(regexp = NOT_BLANK, message = "must not be blank")
    @Size(max = 100)
    private String fullName;
}
