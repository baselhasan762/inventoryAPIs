package com.inventoryAPIs.demo.repository;

import com.inventoryAPIs.demo.entity.Role;
import com.inventoryAPIs.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndActiveTrue(Role role);

    /** A null filter matches everything, so each one is optional. */
    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:active IS NULL OR u.active = :active)
            """)
    Page<User> search(@Param("role") Role role, @Param("active") Boolean active, Pageable pageable);
}
