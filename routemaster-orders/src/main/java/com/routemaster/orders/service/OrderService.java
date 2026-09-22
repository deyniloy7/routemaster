package com.routemaster.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.routemaster.common.event.OrderPlacedEvent;
import com.routemaster.common.exception.RouteMasterException;
import com.routemaster.orders.dto.PlaceOrderRequest;
import com.routemaster.orders.entity.Order;
import com.routemaster.orders.entity.OrderStatus;
import com.routemaster.orders.entity.OutboxEvent;
import com.routemaster.orders.repository.OrderRepository;
import com.routemaster.orders.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order placeOrder(PlaceOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .items(List.copyOf(request.getItems()))
                .priority(request.getPriority())
                .baseFee(calculatePlaceholderBaseFee(request.getPickupLocation(), request.getDropoffLocation()))
                .paid(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(OrderStatus.PLACED)
                .specialInstructions(request.getSpecialInstructions())
                .build();

        order.setDeliveryFee(order.calculateDeliveryFee(List.of()));
        order.setEstimatedDeliveryTime(calculatePlaceholderEstimatedDeliveryTime());

        Order savedOrder = orderRepository.save(order);

        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getPickupLocation(),
                savedOrder.getDropoffLocation(),
                savedOrder.getPriority()
        );

        OutboxEvent outboxEvent;

        try {
            outboxEvent = OutboxEvent.builder()
                    .payload(objectMapper.writeValueAsString(orderPlacedEvent))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RouteMasterException("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        outboxEventRepository.save(outboxEvent);
        
        return savedOrder;
    }

    // TODO: this needs to replaced
    private BigDecimal calculatePlaceholderBaseFee(String pickupLocation, String dropoffLocation) {
        return BigDecimal.valueOf(50);
    }

    // TODO: this needs to replaced
    private LocalDateTime calculatePlaceholderEstimatedDeliveryTime() {
        return LocalDateTime.now().plusMinutes(25);
    }
}
