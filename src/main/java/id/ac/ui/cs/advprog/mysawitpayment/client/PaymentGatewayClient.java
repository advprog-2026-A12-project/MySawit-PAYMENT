package id.ac.ui.cs.advprog.mysawitpayment.client;

import id.ac.ui.cs.advprog.mysawitpayment.dto.result.CreateInvoiceResult;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayClient {
    CreateInvoiceResult createTopupInvoice(UUID transactionId, UUID adminId, BigDecimal amountIdr);
}