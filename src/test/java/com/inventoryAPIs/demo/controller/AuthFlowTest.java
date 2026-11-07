package com.inventoryAPIs.demo.controller;

import com.inventoryAPIs.demo.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends IntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Test
    void loginWithCorrectPasswordReturnsTokens() throws
            Exception{
        login(adminPassword)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        login("wrong-password").andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenWorksOnlyOnce() throws Exception {
        String token = refreshTokenFrom(login(adminPassword));

        refresh(token).andExpect(status().isOk());

        refresh(token).andExpect(status().isUnauthorized());
    }

    @Test
    void reusingARefreshTokenRevokesEveryToken() throws Exception {
        String stolen = refreshTokenFrom(login(adminPassword));
        String current = refreshTokenFrom(refresh(stolen));

        refresh(stolen).andExpect(status().isUnauthorized());

        refresh(current).andExpect(status().isUnauthorized());
    }

    private ResultActions login(String password) throws
            Exception{
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password": "%s"}
                        """.formatted(adminEmail,password)));

    }

    private ResultActions refresh(String token) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(token)));
    }

    private String refreshTokenFrom(ResultActions response ) throws Exception
    {
        String body = response.andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body,"$.refreshToken");
    }




}
