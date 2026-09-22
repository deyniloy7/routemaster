package com.routemaster.orders.service;

import com.routemaster.orders.entity.OutboxEvent;
import com.routemaster.orders.entity.PublishStatus;
import com.routemaster.orders.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.routemaster.common.constants.KafkaTopics.Orders.ORDER_PLACED;

@Service
@RequiredArgsConstructor
public class OutboxEventService {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository
                    .findByPublishStatusIn(List.of(PublishStatus.WAITING, PublishStatus.FAILED));

        for (OutboxEvent event: events) {
            if (event.getRetryCount() >= 5) {
                event.setPublishStatus(PublishStatus.EXHAUSTED);
                outboxEventRepository.save(event);
                continue;
            }
            kafkaTemplate.send(ORDER_PLACED, event.getPayload())
                    .whenComplete((result, exception) -> {
                        if(exception == null) {
                            event.setPublishStatus(PublishStatus.PUBLISHED);
                        } else {
                            event.setPublishStatus(PublishStatus.FAILED);
                            event.setRetryCount(event.getRetryCount() + 1);
                        }
                        outboxEventRepository.save(event);
                    });

        }
    }
}
