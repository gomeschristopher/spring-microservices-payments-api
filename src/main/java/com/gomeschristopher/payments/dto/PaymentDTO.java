package com.gomeschristopher.payments.dto;

import com.gomeschristopher.payments.model.Payment;
import com.gomeschristopher.payments.model.Status;

import java.math.BigDecimal;

public record PaymentDTO(
         Long id,
         BigDecimal value,
         String name,
         String number,
         String expiration,
         String code,
         Status status,
         Long orderId,
         Long paymentMethodId
) {
    public PaymentDTO(Payment payment) {
        this(
                payment.getId(),
                payment.getValue(),
                payment.getName(),
                payment.getNumber(),
                payment.getExpiration(),
                payment.getCode(),
                payment.getStatus(),
                payment.getOrderId(),
                payment.getPaymentMethodId()
        );
    }
}
