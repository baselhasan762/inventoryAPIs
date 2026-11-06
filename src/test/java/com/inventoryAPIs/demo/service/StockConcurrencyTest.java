package com.inventoryAPIs.demo.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.inventoryAPIs.demo.IntegrationTest;
import com.inventoryAPIs.demo.dto.StockMovementRequestDTO;
import com.inventoryAPIs.demo.entity.Location;
import com.inventoryAPIs.demo.entity.MovementType;
import com.inventoryAPIs.demo.entity.Product;
import com.inventoryAPIs.demo.exception.InsufficientStockException;
import com.inventoryAPIs.demo.repository.LocationRepository;
import com.inventoryAPIs.demo.repository.ProductRepository;
import com.inventoryAPIs.demo.repository.StockLevelRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class StockConcurrencyTest extends IntegrationTest {
    private static final int STARTING_STOCK =10;
    private static final int BUYERS =20;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private StockLevelRepository stockLevelRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @AfterEach
    void logOut(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void concurrentStockOutsNeverOversell() throws Exception{
        Product product = productRepository.save(Product.builder()
                .sku("CONCUURRENT-1")
                .name("Concurrency product")
                .unitPrice(BigDecimal.TEN)
                .costPrice(BigDecimal.ONE)
                .unitOfMeasure("pcs")
                .build());
        Location location = locationRepository.save(Location.builder()
                .code("CONCURRENT-1")
                .name("Concurrency location")
                .build());
        logInAsAdmin();
        stockMovementService.create(movement(product,location,MovementType.IN,STARTING_STOCK));

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        try(        ExecutorService executor = Executors.newFixedThreadPool(BUYERS);){
            for(int i =0;i<BUYERS ;i++){
                executor.submit(()->{
                    logInAsAdmin();
                    startSignal.await();
                    try{
                        stockMovementService.create(movement(product,location,MovementType.OUT,1));
                        succeeded.incrementAndGet();

                    }
                    catch (InsufficientStockException e){
                        rejected.incrementAndGet();
                    }
                    return null;
                });
            }
            startSignal.countDown();
        }
        assertThat(succeeded.get()).isEqualTo(STARTING_STOCK);
        assertThat(rejected.get()).isEqualTo(BUYERS - STARTING_STOCK);
        assertThat(currentQuantity(product, location)).isZero();
    }

    private void logInAsAdmin(){
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminEmail,null,List.of())
        );
    }

    private StockMovementRequestDTO movement(Product product,Location location,MovementType type , int quantity){
        return new StockMovementRequestDTO(product.getId(),location.getId(),type,quantity,"concurrency test");
    }


    private int currentQuantity(Product product, Location location) {
        return stockLevelRepository.search(product.getId(), location.getId(), PageRequest.of(0, 1))
                .getContent().getFirst().getQuantity();
    }
}


