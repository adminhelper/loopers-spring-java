package com.loopers.infrastructure.dataplatform;

import com.loopers.domain.order.Order;
import com.loopers.domain.payment.Payment;
import org.springframework.stereotype.Component;

@Component
public class OrderDataPlatformClientImpl implements OrderDataPlatformClient {

    @Override
    public void send(Order order, Payment payment) {
    }
}

