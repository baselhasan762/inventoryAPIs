package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.UserRequestDTO;
import com.inventoryAPIs.demo.dto.UserResponseDTO;
import com.inventoryAPIs.demo.dto.UserRoleUpdateDTO;
import com.inventoryAPIs.demo.dto.UserUpdateDTO;
import com.inventoryAPIs.demo.entity.Role;
import com.inventoryAPIs.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserRequestDTO request) {
        return userService.create(request);
    }

    @GetMapping
    public PageResponseDTO<UserResponseDTO> findAll(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return userService.findAll(role, active, pageable);
    }

    @GetMapping("/me")
    public UserResponseDTO me() {
        return userService.findCurrent();
    }

    @GetMapping("/{id}")
    public UserResponseDTO findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}")
    public UserResponseDTO updateProfile(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO request) {
        return userService.updateProfile(id, request);
    }

    @PatchMapping("/{id}/role")
    public UserResponseDTO changeRole(@PathVariable Long id, @Valid @RequestBody UserRoleUpdateDTO request) {
        return userService.changeRole(id, request.getRole());
    }

    @PatchMapping("/{id}/reactivate")
    public UserResponseDTO reactivate(@PathVariable Long id) {
        return userService.reactivate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
