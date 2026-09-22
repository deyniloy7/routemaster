package com.routemaster.orders;

import com.routemaster.common.event.Priority;
import com.routemaster.orders.dto.PlaceOrderRequest;
import com.routemaster.orders.entity.OrderItem;
import com.routemaster.orders.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.util.List;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.routemaster.orders", "com.routemaster.common"})
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(OrderService orderService) {
        return (args ->  {
            PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(
                    "cust_002",
                    "Whitefield",
                    "Koramangala",
                    List.of(new OrderItem("item1", 5, BigDecimal.valueOf(5))),
                    "none",
                    Priority.STANDARD
            );

            orderService.placeOrder(placeOrderRequest);
        });
    }
}
