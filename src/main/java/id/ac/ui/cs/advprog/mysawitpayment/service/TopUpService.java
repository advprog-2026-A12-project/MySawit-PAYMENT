package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TopUpService {

    CreateTopUpResponse createTopUp(CreateTopUpRequest request, UUID adminId);

    Page<HistoryTopUpResponse> getMyTopUps(UUID adminId, Pageable pageable);

    TopUpDetailResponse getTopUpDetail(UUID id, UUID adminId);

    void handleXenditCallback(String callbackToken, XenditCallbackRequest request);
}
