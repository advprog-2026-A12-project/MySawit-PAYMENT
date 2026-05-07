package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;

public interface InternalService {
    WalletCreationResponse createWallet(WalletCreationRequest request);

    PayrollCreationResponse createPayroll(PayrollCreationRequest request);
}
