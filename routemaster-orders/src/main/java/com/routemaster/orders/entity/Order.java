package com.routemaster.orders.entity;

import com.routemaster.common.event.Priority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Represents the Order entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
public class Order {

    /**
     * The unique identifier of the order
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * The unique identifier of the customer
     */
    @Column(nullable = false)
    private String customerId;

    /**
     * The location to pick the order from
     */
    @Column(nullable = false)
    private String pickupLocation;

    /**
     * The location to drop the order to
     */
    @Column(nullable = false)
    private String dropoffLocation;

    /**
     * The base fee applicable for the order
     */
    @Column(nullable = false)
    private BigDecimal baseFee;

    /**
     * The delivery fee applicable for the order
     */
    @Column(nullable = false)
    private BigDecimal deliveryFee;

    /**
     * The payment status of the order
     */
    @Column(nullable = false)
    private boolean paid;

    /**
     * The current status of the order
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * The priority level of the order
     */
    @Enumerated(EnumType.STRING)
    private Priority priority;

    /**
     * The source where the order was placed
     */
    @Enumerated(EnumType.STRING)
    private Source source;

    /**
     * The estimated time that might be required to deliver the order
     */
    @Column(nullable = false)
    private LocalDateTime estimatedDeliveryTime;

    /**
     * Any additional instructions/requests made for the order
     */
    private String specialInstructions;

    /**
     * The unique id of the driver assigned for delivery
     */
    private String driverId;

    /**
     * The time when the order was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The time when the order was updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * The list of items in the order
     */
    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderItem> items;

    /**
     * Set creation time when the order is created
     * Set update time for the first time as well
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Set update time every time the order gets updated
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * The total cost of all the items in the order
     */
    public BigDecimal costOfItems() {
        return items.stream().map(OrderItem::subTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the final delivery fee by applying a sequence of optional
     * business rules (e.g. surge pricing, promotional discounts) to the
     * order's base fee.
     *
     * <p>Always starts from {@code baseFee}, never from the current
     * {@code deliveryFee} value, so that calling this method more than
     * once never double-applies a rule that was already factored in.
     *
     * @param rules an ordered list of adjustments, each taking the running
     *              fee and this order and returning the adjusted fee;
     *              passed in rather than stored statically so different
     *              rule sets can be substituted in tests
     */
    public BigDecimal calculateDeliveryFee(List<BiFunction<BigDecimal, Order, BigDecimal>> rules) {
        BigDecimal currentFee = this.baseFee;
        for (BiFunction<BigDecimal, Order, BigDecimal> rule : rules) {
            currentFee = rule.apply(currentFee, this);
        }
        return currentFee;

    }

    /**
     * The order's grand total — items plus delivery fee.
     *
     * <p>Returns empty if {@code deliveryFee} is null. This should not
     * happen in practice, since the fee-estimation step always sets it
     * at creation, but nullable=false is only a database-level guarantee —
     * a null could still exist on an in-memory Order before that step runs.
     *
     * @return the total cost, or empty if the delivery fee isn't set yet
     */
    public Optional<BigDecimal> totalCost() {
        return Optional.ofNullable(deliveryFee).map(fee -> costOfItems().add(fee));
    }
}
