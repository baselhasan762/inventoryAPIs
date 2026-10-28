package com.inventoryAPIs.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "InventoryAPI", version = "1.0", description = "inventory api project that handles product managment and stock movement between different locations."),
        security = @SecurityRequirement(name = "bearerAUTH"))
@SecurityScheme(name = "bearerAUTH", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    private static final List<String> TAG_ORDER = List.of(
            "Authentication", "Users", "Products", "Categories", "Locations", "Stock levels", "Stock movements");

    @Bean
    public OpenApiCustomizer tagOrderCustomizer() {
        return openApi -> openApi.setTags(openApi.getTags().stream()
                .sorted(Comparator.comparingInt(tag -> this.positionOf(tag)))
                .toList());
    }

    private int positionOf(Tag tag) {
        int position = TAG_ORDER.indexOf(tag.getName());
        return position == -1 ? TAG_ORDER.size() : position;
    }
}
