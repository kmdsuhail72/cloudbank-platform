package com.cloudbank.notification.email;

public interface EmailDeliveryTransport {

    void send(
            EmailDeliveryMessage message
    );
}
