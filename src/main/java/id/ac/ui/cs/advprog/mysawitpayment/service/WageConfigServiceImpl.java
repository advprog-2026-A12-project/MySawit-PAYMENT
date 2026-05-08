package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.PreviousWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.UpdatedByResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ActiveWageConfigNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WageConfigRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WageConfigServiceImpl implements WageConfigService {

    private final WageConfigRepository wageConfigRepository;
    private final PaymentAuthorizationService authorizationService;

    @Override
    public WageConfig getActiveWageConfig() {
        return wageConfigRepository.findByIsActiveTrue()
                .orElseThrow(ActiveWageConfigNotFoundException::new);
    }

    @Override
    public CurrentWageConfigResponse getCurrentWageConfig(AuthenticatedUser requester) {
        authorizationService.requireWageConfigManager(requester);
        WageConfig wageConfig = getActiveWageConfig();
        return mapToCurrentWageConfigResponse(wageConfig);
    }

    @Override
    @Transactional
    public CreateWageConfigResponse createWageConfig(CreateWageConfigRequest request, AuthenticatedUser requester) {
        authorizationService.requireWageConfigManager(requester);
        WageConfig previousActiveConfig = wageConfigRepository.findByIsActiveTrue().orElse(null);

        if (previousActiveConfig != null) {
            previousActiveConfig.deactivate();
            wageConfigRepository.saveAndFlush(previousActiveConfig);
        }

        WageConfig newConfig = WageConfig.builder()
                .upahBuruhPerKg(request.getUpahBuruhPerKg())
                .upahSupirPerKg(request.getUpahSupirPerKg())
                .upahMandorPerKg(request.getUpahMandorPerKg())
                .updatedBy(requester.id())
                .build();

        WageConfig savedConfig = wageConfigRepository.save(newConfig);

        return mapToCreateWageConfigResponse(savedConfig, previousActiveConfig);
    }

    @Override
    public Page<HistoryWageConfigResponse> getWageConfigHistory(AuthenticatedUser requester, Pageable pageable) {
        authorizationService.requireWageConfigManager(requester);
        return wageConfigRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToHistoryWageConfigResponse);
    }

    private CurrentWageConfigResponse mapToCurrentWageConfigResponse(WageConfig wageConfig) {
        UpdatedByResponse updatedBy = mapToUpdatedByResponse(wageConfig.getUpdatedBy());

        return CurrentWageConfigResponse.builder()
                .id(wageConfig.getId())
                .upahBuruhPerKg(wageConfig.getUpahBuruhPerKg())
                .upahSupirPerKg(wageConfig.getUpahSupirPerKg())
                .upahMandorPerKg(wageConfig.getUpahMandorPerKg())
                .currency("SawitDollar")
                .isActive(wageConfig.getIsActive())
                .updatedBy(updatedBy)
                .effectiveFrom(wageConfig.getEffectiveFrom())
                .createdAt(wageConfig.getCreatedAt())
                .build();
    }

    private CreateWageConfigResponse mapToCreateWageConfigResponse(
            WageConfig currentConfig,
            WageConfig previousConfig
    ) {
        UpdatedByResponse updatedBy = mapToUpdatedByResponse(currentConfig.getUpdatedBy());

        PreviousWageConfigResponse previousWageConfigResponse = null;
        if (previousConfig != null) {
            previousWageConfigResponse = PreviousWageConfigResponse.builder()
                    .id(previousConfig.getId())
                    .upahBuruhPerKg(previousConfig.getUpahBuruhPerKg())
                    .upahSupirPerKg(previousConfig.getUpahSupirPerKg())
                    .upahMandorPerKg(previousConfig.getUpahMandorPerKg())
                    .deactivatedAt(currentConfig.getEffectiveFrom())
                    .build();
        }

        return CreateWageConfigResponse.builder()
                .id(currentConfig.getId())
                .upahBuruhPerKg(currentConfig.getUpahBuruhPerKg())
                .upahSupirPerKg(currentConfig.getUpahSupirPerKg())
                .upahMandorPerKg(currentConfig.getUpahMandorPerKg())
                .currency("SawitDollar")
                .isActive(currentConfig.getIsActive())
                .previousConfig(previousWageConfigResponse)
                .updatedBy(updatedBy)
                .effectiveFrom(currentConfig.getEffectiveFrom())
                .createdAt(currentConfig.getCreatedAt())
                .build();
    }

    private HistoryWageConfigResponse mapToHistoryWageConfigResponse(WageConfig wageConfig) {
        UpdatedByResponse updatedBy = mapToUpdatedByResponse(wageConfig.getUpdatedBy());

        return HistoryWageConfigResponse.builder()
                .id(wageConfig.getId())
                .upahBuruhPerKg(wageConfig.getUpahBuruhPerKg())
                .upahSupirPerKg(wageConfig.getUpahSupirPerKg())
                .upahMandorPerKg(wageConfig.getUpahMandorPerKg())
                .isActive(wageConfig.getIsActive())
                .updatedBy(updatedBy)
                .effectiveFrom(wageConfig.getEffectiveFrom())
                .build();
    }

    private UpdatedByResponse mapToUpdatedByResponse(UUID updatedById) {
        // TODO: Ambil nama admin
        return UpdatedByResponse.builder()
                .id(updatedById)
                .name(null)
                .build();
    }
}
