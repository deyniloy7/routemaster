package com.routemaster.orders.entity;

/**
 * Enum representing various stages of OrderPlacedEvent publishing
 */
public enum PublishStatus {
    /**
     * The status of the publishing is unknown
     */
    STATUS_UNSPECIFIED,

    /**
     * The event is waiting to be published
     */
    WAITING,

    /**
     * The event has been successfully published
     */
    PUBLISHED,

    /**
     * The publishing of the event failed
     */
    FAILED,

    /**
     * The publishing of the event failed even after the maximum number of retries
     * Needs human intervention
     */
    EXHAUSTED
}
