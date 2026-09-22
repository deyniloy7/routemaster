package com.routemaster.orders.dto;

import com.routemaster.common.event.Priority;
import com.routemaster.orders.entity.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotBlank(message = "Customer id is required")
    private String customerId;

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @NotBlank(message = "Dropoff location is required")
    private String dropoffLocation;

    @NotEmpty(message = "Items are required")
    private List<OrderItem> items;

    private String specialInstructions;

    private Priority priority;
}
