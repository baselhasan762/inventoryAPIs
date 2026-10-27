package com.inventoryAPIs.demo.service;

import com.inventoryAPIs.demo.dto.PageResponseDTO;
import com.inventoryAPIs.demo.dto.UserRequestDTO;
import com.inventoryAPIs.demo.dto.UserResponseDTO;
import com.inventoryAPIs.demo.dto.UserUpdateDTO;
import com.inventoryAPIs.demo.entity.Role;
import com.inventoryAPIs.demo.entity.User;
import com.inventoryAPIs.demo.exception.BusinessRuleException;
import com.inventoryAPIs.demo.exception.DuplicateResourceException;
import com.inventoryAPIs.demo.exception.ResourceNotFoundException;
import com.inventoryAPIs.demo.repository.UserRepository;
import com.inventoryAPIs.demo.security.CurrentUserProvider;
import com.inventoryAPIs.demo.security.UserSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSecurity userSecurity;
    private final CurrentUserProvider currentUserProvider;

    public UserResponseDTO create(UserRequestDTO request) {
        ensureEmailAvailable(request.getEmail());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(resolveRole(request.getRole()))
                .build();

        return UserResponseDTO.from(userRepository.save(user));
    }

    /** Admin listing; {@code role} and {@code active} are optional filters, null meaning "any". */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> findAll(Role role, Boolean active, Pageable pageable) {
        return PageResponseDTO.from(userRepository.search(role, active, pageable), UserResponseDTO::from);
    }

    /** Any logged-in user may view a profile; only changing one is restricted. */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return UserResponseDTO.from(findUser(id));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findCurrent() {
        return UserResponseDTO.from(currentUserProvider.get());
    }

    @PreAuthorize("@userSecurity.isSelf(#id, authentication)")
    public UserResponseDTO updateProfile(Long id, UserUpdateDTO request) {
        User user = findUser(id);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            ensureEmailAvailable(request.getEmail());
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponseDTO.from(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO changeRole(Long id, Role role) {
        User user = findUser(id);

        if (user.getRole() == Role.ADMIN && role != Role.ADMIN) {
            ensureNotLastAdmin(user);
        }
        user.setRole(role);

        return UserResponseDTO.from(user);
    }

    /** Undoes a soft delete; reactivating an already active user is a no-op. */
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO reactivate(Long id) {
        User user = findUser(id);
        user.setActive(true);
        return UserResponseDTO.from(user);
    }

    /** Soft delete: stock movements keep a foreign key to their creator, so the row is deactivated instead. */
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isSelf(#id, authentication)")
    public void delete(Long id) {
        User user = findUser(id);
        ensureNotLastAdmin(user);
        user.setActive(false);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Role resolveRole(Role requested) {
        if (requested == null) {
            return Role.STAFF;
        }
        if (requested != Role.STAFF && !userSecurity.isCurrentUserAdmin()) {
            throw new AccessDeniedException("Only admins can assign roles");
        }
        return requested;
    }

    /** Refuses changes that would leave nobody able to manage users, locations and categories. */
    private void ensureNotLastAdmin(User user) {
        if (user.getRole() == Role.ADMIN && user.isActive()
                && userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new BusinessRuleException("Cannot remove the last active admin");
        }
    }

    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already in use: " + email);
        }
    }
}
