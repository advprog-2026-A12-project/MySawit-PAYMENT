package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WageConfigService {

    CurrentWageConfigResponse getCurrentWageConfig();

    CreateWageConfigResponse createWageConfig(CreateWageConfigRequest request, UUID adminId);

    Page<HistoryWageConfigResponse> getWageConfigHistory(Pageable pageable);

    WageConfig getActiveWageConfig();
}