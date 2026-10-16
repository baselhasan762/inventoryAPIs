package com.inventoryAPIs.demo.dto;

import com.inventoryAPIs.demo.entity.Location;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationResponseDTO {

    private final Long id;
    private final String name;
    private final String code;
    private final String address;

    public static LocationResponseDTO from(Location location) {
        return LocationResponseDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .code(location.getCode())
                .address(location.getAddress())
                .build();
    }
}
