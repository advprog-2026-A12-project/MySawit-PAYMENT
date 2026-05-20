package id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.TransactionType;

import java.time.OffsetDateTime;

public record WalletTransactionFilter(
        TransactionType transactionType,
        OffsetDateTime dateFrom,
        OffsetDateTime dateTo
) {
}
