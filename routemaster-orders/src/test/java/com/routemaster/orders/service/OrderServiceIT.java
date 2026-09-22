package com.routemaster.orders.service;

import com.routemaster.common.event.Priority;
import com.routemaster.orders.dto.PlaceOrderRequest;
import com.routemaster.orders.entity.OrderItem;
import com.routemaster.orders.repository.OrderRepository;
import com.routemaster.orders.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
public class OrderServiceIT {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @MockBean
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void placeOrder_shouldFail_whenAnyTransactionFails() {
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                "cust_001",
                "Whitefield",
                "Domlur",
                List.of(
                        new OrderItem("bags", 2, BigDecimal.valueOf(2.50)),
                        new OrderItem("shoes", 5, BigDecimal.valueOf(3))
                ),
                null,
                Priority.STANDARD
        );

        when(outboxEventRepository.save(any())).thenThrow(new RuntimeException("Simulated Outbox failure"));

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(placeOrderRequest));
        assertThat(orderRepository.count()).isZero();
    }
}
