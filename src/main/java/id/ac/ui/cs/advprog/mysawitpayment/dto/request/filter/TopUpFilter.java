package id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;

public record TopUpFilter(PaymentTransactionStatus status) {
}
