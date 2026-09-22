package com.routemaster.orders.entity;

/**
 * Enum indicating various order statuses
 */
public enum OrderStatus {

    /**
     * The status of other is not present or is missing
     */
    STATUS_UNSPECIFIED,

    /**
     * The order has been placed
     */
    PLACED,

    /**
     * The order has been accepted by the system
     */
    ACCEPTED,

    /**
     * The order has been routed and a driver has been assigned
     */
    ROUTED,

    /**
     * The order is on the way to the customer
     */
    ON_THE_WAY,

    /**
     * The order has been delivered to the customer
     */
    DELIVERED,

    /**
     * The package failed to reach the customer
     */
    DELIVERY_FAILED,

    /**
     * The order has been returned by the customer after receiving
     */
    RETURNED,

    /**
     * The order has been cancelled by the customer
     */
    CANCELLED
}
