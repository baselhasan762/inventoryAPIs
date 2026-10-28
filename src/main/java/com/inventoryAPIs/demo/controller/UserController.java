package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.UserRequestDTO;
import com.inventoryAPIs.demo.dto.UserResponseDTO;
import com.inventoryAPIs.demo.dto.UserRoleUpdateDTO;
import com.inventoryAPIs.demo.dto.UserUpdateDTO;
import com.inventoryAPIs.demo.entity.Role;
import com.inventoryAPIs.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Sign-up, profiles and user administration")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Sign up", description = "Open to everyone, so testers can create an account. New users are STAFF; only an admin may pick another role.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    public UserResponseDTO create(@Valid @RequestBody UserRequestDTO request) {
        return userService.create(request);
    }

    @Operation(summary = "List users", description = "Admin only. Filter by role and active status.")
    @GetMapping
    public PageResponseDTO<UserResponseDTO> findAll(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return userService.findAll(role, active, pageable);
    }

    @Operation(summary = "Get the logged-in user")
    @GetMapping("/me")
    public UserResponseDTO me() {
        return userService.findCurrent();
    }

    @Operation(summary = "Get a user", description = "Any logged-in user can view a profile.")
    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "Update your profile", description = "Only the account's own user. Only the fields you send are changed.")
    @PatchMapping("/{id}")
    public UserResponseDTO updateProfile(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO request) {
        return userService.updateProfile(id, request);
    }

    @Operation(summary = "Change a user's role", description = "Admin only. The last active admin can't be demoted.")
    @PatchMapping("/{id}/role")
    public UserResponseDTO changeRole(@PathVariable Long id, @Valid @RequestBody UserRoleUpdateDTO request) {
        return userService.changeRole(id, request.getRole());
    }

    @Operation(summary = "Reactivate a user", description = "Admin only.")
    @PatchMapping("/{id}/reactivate")
    public UserResponseDTO reactivate(@PathVariable Long id) {
        return userService.reactivate(id);
    }

    @Operation(summary = "Deactivate a user", description = "Admin or the user themselves. The last active admin can't be removed. Can be undone with /reactivate.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
