package com.routemaster.orders.entity;

/**
 * The enum specifying order sources
 */
public enum Source {

    /**
     * The source of the order is missing/absent
     */
    SOURCE_UNSPECIFIED,

    /**
     * The order has been placed using the mobile app
     */
    MOBILE_APP,

    /**
     * The order was placed on the website
     */
    WEBSITE,

    /**
     * The order was placed on the desktop app
     */
    DESKTOP_APP,

    /**
     * The order was phoned-in
     */
    PHONE_CALL,

    /**
     * The order has been received from a third party channel/source
     */
    PARTNER_INTEGRATION
}
