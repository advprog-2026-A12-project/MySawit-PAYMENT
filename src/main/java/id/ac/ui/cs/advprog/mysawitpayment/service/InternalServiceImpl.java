package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.PayrollCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal.WalletCreationRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.PayrollCreationResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.internal.WalletCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalServiceImpl implements InternalService {

    private final WalletService walletService;
    private final PayrollService payrollService;

    @Override
    public WalletCreationResponse createWallet(WalletCreationRequest request) {
        return walletService.createWallet(request);
    }

    @Override
    public PayrollCreationResponse createPayroll(PayrollCreationRequest request) {
        return payrollService.createPayroll(request);
    }
}
