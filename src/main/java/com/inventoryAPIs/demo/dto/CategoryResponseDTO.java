package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponseDTO {

    private final Long id;
    private final String name;
    private final String description;

    public static CategoryResponseDTO from(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
