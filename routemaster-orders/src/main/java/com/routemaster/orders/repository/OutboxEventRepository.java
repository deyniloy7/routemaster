package com.routemaster.orders.repository;

import com.routemaster.orders.entity.OutboxEvent;
import com.routemaster.orders.entity.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByPublishStatusIn(List<PublishStatus> publishStatuses);
}
