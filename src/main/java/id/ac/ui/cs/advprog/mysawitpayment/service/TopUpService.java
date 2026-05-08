package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateTopUpRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TopUpService {

    CreateTopUpResponse createTopUp(CreateTopUpRequest request, AuthenticatedUser requester);

    Page<HistoryTopUpResponse> getMyTopUps(AuthenticatedUser requester, Pageable pageable);

    TopUpDetailResponse getTopUpDetail(UUID id, AuthenticatedUser requester);

    void handleXenditCallback(String callbackToken, XenditCallbackRequest request);
}
