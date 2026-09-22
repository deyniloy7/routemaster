package com.routemaster.common.constants;

public class KafkaTopics {
    private KafkaTopics() {};

    public static final class Orders {
        private Orders () {};

        public static final String ORDER_PLACED = "order-events";
    }

}
