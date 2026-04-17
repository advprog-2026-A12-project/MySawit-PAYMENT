package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    CreateTopUpResponse createTopUp(CreateTopUpRequest request, UUID adminId);

    Page<HistoryTopUpResponse> getMyTopUps(UUID adminId, Pageable pageable);

    TopUpDetailResponse getTopUpDetail(UUID id, UUID adminId);
}
