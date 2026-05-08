package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WageConfigService {

    CurrentWageConfigResponse getCurrentWageConfig(AuthenticatedUser requester);

    CreateWageConfigResponse createWageConfig(CreateWageConfigRequest request, AuthenticatedUser requester);

    Page<HistoryWageConfigResponse> getWageConfigHistory(AuthenticatedUser requester, Pageable pageable);

    WageConfig getActiveWageConfig();
}
