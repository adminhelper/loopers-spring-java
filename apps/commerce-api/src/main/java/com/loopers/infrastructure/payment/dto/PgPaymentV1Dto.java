package com.loopers.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Collections;
import java.util.List;

public class PgPaymentV1Dto {

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

    public record Request(
            @NotBlank(message = "주문 ID는 필수입니다")
            String orderId,
            @NotNull(message = "카드 타입은 필수입니다")
            PgPaymentV1Dto.CardType cardType,
            @NotBlank(message = "카드 번호는 필수입니다")
            String cardNo,
            @NotNull(message = "결제 금액은 필수입니다")
            @Positive(message = "결제 금액은 0보다 커야 합니다")
            Long amount,
            @NotBlank(message = "콜백 URL은 필수입니다")
            String callbackUrl
    ) {
        public String getMaskedCardNo() {
            if (cardNo == null || cardNo.length() < 4) {
                return "****";
            }
            return "**** **** **** " + cardNo.substring(cardNo.length() - 4);
        }
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
        public static Metadata success() {
            return new Metadata(Result.SUCCESS, null, null);
        }

        public static Metadata fail(String message) {
            return new Metadata(Result.FAIL, "PG_ERROR", message);
        }

        public enum Result {
            SUCCESS, FAIL
        }
    }
}
