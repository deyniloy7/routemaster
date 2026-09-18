package com.routemaster.orders.entity;

public enum Channel {
    SOURCE_UNSPECIFIED,
    MOBILE_APP,
    WEBSITE,
    DESKTOP_APP,
    PHONE_CALL,

    /**
     * The order has been received from a third party channel/source
     */
    PARTNER_INTEGRATION
}
