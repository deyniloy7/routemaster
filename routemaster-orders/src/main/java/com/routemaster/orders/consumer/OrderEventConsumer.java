package com.routemaster.orders.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.routemaster.common.constants.KafkaTopics.Orders.ORDER_PLACED;

@Component
public class OrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(topics = ORDER_PLACED, groupId = "orders-service-group")
    public void onOrderPlaced(String payload) {
        log.info("Received order placed event: {}", payload);
    }
}
