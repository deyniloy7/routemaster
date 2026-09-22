package com.routemaster.common.event;

/**
 * Event published when a new order is placed.
 *
 * <p>Published by the Orders service and consumed by the Routing
 * service. Emitted as an asynchronous event rather than a direct
 * synchronous call, since Routing is designed as an AP system and
 * should never have its availability coupled to Orders' ability
 * to reach it.
 *
 * @param priority indicates order delivery priority, includes PRIORITY_UNSPECIFIED too, for when the priority is absent from the incoming message,
 *                 consumer should reject it rather than silently defaulting
 */
public record OrderPlacedEvent(String orderId, String pickupLocation, String dropoffLocation, Priority priority) { }
