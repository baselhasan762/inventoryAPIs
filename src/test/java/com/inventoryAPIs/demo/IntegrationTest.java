package com.inventoryAPIs.demo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@Import(TestContainersConfiguration.class)
@TestPropertySource(properties = {
        "spring.datasource.password=unused",
        "app.admin.password=Test@12345",
        "app.jwt.secret=dGVzdC1vbmx5LXNlY3JldC10ZXN0LW9ubHktc2VjcmV0LXRlc3Qtb25seQ=="
})
@AutoConfigureMockMvc
public abstract class IntegrationTest {
}
