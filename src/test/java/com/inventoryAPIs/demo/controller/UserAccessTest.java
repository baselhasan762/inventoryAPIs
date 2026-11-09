package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.IntegrationTest;
import com.inventoryAPIs.demo.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import com.inventoryAPIs.demo.repository.UserRepository ;
import com.inventoryAPIs.demo.entity.Role;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;

public class UserAccessTest extends IntegrationTest {
    private static final String STAFF_PASSWORD ="Staff@12345";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void staffCannotListUsers() throws Exception {
        String token = accessToken(signUpStaff(), STAFF_PASSWORD);

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }


    @Test
    void adminCanListUsers() throws Exception {
        String token = accessToken(adminEmail, adminPassword);

        mockMvc.perform(get("/api/users").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void signUpCannotChooseAdminRole() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "fullName": "Sneaky", "role": "ADMIN"}
                                """.formatted(uniqueEmail(), STAFF_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lastAdminCannotBeDeactivated() throws Exception {
        assertThat(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).isEqualTo(1);
        String token = accessToken(adminEmail, adminPassword);

        mockMvc.perform(delete("/api/users/{id}", currentUserId(token))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict());
    }

    @Test
    void lastAdminCannotBeDemoted() throws Exception {
        assertThat(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).isEqualTo(1);
        String token = accessToken(adminEmail, adminPassword);

        mockMvc.perform(patch("/api/users/{id}/role", currentUserId(token))
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role": "STAFF"}
                                """))
                .andExpect(status().isConflict());
    }

    private String signUpStaff() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s", "fullName": "Test Staff"}
                                """.formatted(email, STAFF_PASSWORD)))
                .andExpect(status().isCreated());
        return email;
    }

    private String accessToken(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private int currentUserId(String token) throws Exception {
        String body = mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail() {
        return "staff-" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }
}
