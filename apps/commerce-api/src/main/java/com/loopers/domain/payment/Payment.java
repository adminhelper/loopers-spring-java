package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "payment")
@Getter
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String orderReference;

    @Column(nullable = false)
    private String cardType;

    @Column(nullable = false)
    private String cardNo;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column
    private String transactionKey;

    @Column
    private String reason;

    protected Payment() {}

    private Payment(Long orderId, String userId, String orderReference, String cardType, String cardNo, Long amount, PaymentStatus status) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderReference = orderReference;
        this.cardType = cardType;
        this.cardNo = cardNo;
        this.amount = amount;
        this.status = status;
    }

    public static Payment pending(Long orderId, String userId, String orderReference, String cardType, String cardNo, Long amount) {
        return new Payment(orderId, userId, orderReference, cardType, cardNo, amount, PaymentStatus.PENDING);
    }

    public void updateStatus(PaymentStatus status, String transactionKey, String reason) {
        this.status = status;
        this.transactionKey = transactionKey;
        this.reason = reason;
    }
}
