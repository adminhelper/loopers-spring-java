package com.loopers.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

public class PgPaymentV1Dto {

    public record Request(
            String orderId,
            PgPaymentV1Dto.CardType cardType,
            String cardNo,
            Long amount,
            String callbackUrl
    ) {
    }

    public record Response(
            String transactionKey,
            PgPaymentV1Dto.TransactionStatus status,
            String reason
    ) {
    }

    public record OrderResponse(
            String orderId,
            List<TransactionRecord> transactions
    ) {
        public OrderResponse(String orderId) {
            this(orderId, Collections.emptyList());
        }
    }

    public record TransactionRecord(
            String transactionKey,
            PgPaymentV1Dto.TransactionStatus status,
            String reason
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResponse<T>(
            Metadata meta,
            T data
    ) {
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(Metadata.success(), data);
        }

        public static <T> ApiResponse<T> fail(String message, T data) {
            return new ApiResponse<>(Metadata.fail(message), data);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metadata(
            Result result,
            String errorCode,
            String message
    ) {
        public enum Result {
            SUCCESS, FAIL
        }

        public static Metadata success() {
            return new Metadata(Result.SUCCESS, null, null);
        }

        public static Metadata fail(String message) {
            return new Metadata(Result.FAIL, "PG_ERROR", message);
        }
    }

    public enum CardType {
        SAMSUNG,
        KB,
        HYUNDAI
    }

    public enum TransactionStatus {
        SUCCESS,
        FAILED,
        PENDING
    }
}
