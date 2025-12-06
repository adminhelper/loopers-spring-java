package com.loopers.application.order;

public record OrderPaymentCommand(
        String cardType,
        String cardNo
) {}
