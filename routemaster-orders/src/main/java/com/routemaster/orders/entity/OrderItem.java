package com.routemaster.orders.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem: represents an item in the Order.
 * Embeddable, since it doesn't exist outside an order
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderItem {

    /**
     * The name of the item
     */
    private String itemName;

    /**
     * The quantity of the items in the order
     */
    private int quantity;

    /**
     * The price per unit of the item
     */
    private BigDecimal rate;

    /**
     * Method to calculate the total cost of the items
     * on demand rather than storing it separately. This avoids
     * staleness errors
     * @return the total cost of the item, computed as rate multiplied by quantity
     */
    public BigDecimal subTotal() {
        return rate.multiply(BigDecimal.valueOf(quantity));
    }
}
