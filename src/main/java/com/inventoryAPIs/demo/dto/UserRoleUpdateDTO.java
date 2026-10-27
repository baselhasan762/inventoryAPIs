package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRoleUpdateDTO {

    @NotNull
    private Role role;
}
