package com.inventoryAPIs.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.inventoryAPIs.demo.IntegrationTest;
import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.dto.StockTransferRequestDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.entity.MovementType;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.entity.StockLevel;
import com.inventoryAPIs.demo.exception.InsufficientStockException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockLevelRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class StockTransferTest extends IntegrationTest {

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    StockLevelRepository stockLevelRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    private Product product;
    private Location source;
    private Location destination;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminEmail, null, List.of())
        );

        product = productRepository.save(Product.builder()
                .sku(uniqueCode())
                .name("Transfer product")
                .unitPrice(BigDecimal.TEN)
                .costPrice(BigDecimal.ONE)
                .unitOfMeasure("pcs")
                .build());
        source = saveLocation();
        destination = saveLocation();

        stockMovementService.create(new StockMovementRequestDTO(
                product.getId(), source.getId(), MovementType.IN, 10, "starting stock"
        ));
    }


    @AfterEach
    void logOut() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void transferMovesStockBetweenLocations() {

        stockMovementService.transfer(transferOf(4));
        assertThat(quantityAt(source)).isEqualTo(6);
        assertThat(quantityAt(destination)).isEqualTo(4);
    }

    @Test
    void transferMoreThanAvailableChangesNothing(){

        assertThatThrownBy(  ()-> stockMovementService.transfer(transferOf(11))
        ).isInstanceOf(InsufficientStockException.class);

        int s =quantityAt(source);
        int d = quantityAt(destination);

        assertThat(s).isEqualTo(10);
        assertThat(d).isEqualTo(0);
    }

    private StockTransferRequestDTO transferOf(int quantity) {
        return new StockTransferRequestDTO(product.getId(), source.getId(), destination.getId(), quantity, "transfer test");
    }

    private Location saveLocation() {
        return locationRepository.save(Location.builder().code(uniqueCode()).name("Transfer Location").build());

    }

    private String uniqueCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private int quantityAt(Location location) {
        return stockLevelRepository.search(product.getId(), location.getId(), PageRequest.of(0, 1))
                .getContent().stream()
                .findFirst()
                .map(StockLevel::getQuantity)
                .orElse(0);
    }
}
