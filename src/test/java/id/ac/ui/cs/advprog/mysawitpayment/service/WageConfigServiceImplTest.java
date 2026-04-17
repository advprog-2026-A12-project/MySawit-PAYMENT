package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ActiveWageConfigNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WageConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WageConfigServiceImplTest {

    @Mock
    private WageConfigRepository wageConfigRepository;

    @InjectMocks
    private WageConfigServiceImpl wageConfigService;

    private UUID adminId;
    private WageConfig activeConfig;
    private OffsetDateTime now;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        now = OffsetDateTime.now();

        activeConfig = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .isActive(true)
                .updatedBy(adminId)
                .effectiveFrom(now)
                .createdAt(now)
                .build();
    }

    @Test
    void getCurrentWageConfigShouldReturnCurrentResponseWhenActiveConfigExists() {
        when(wageConfigRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeConfig));

        CurrentWageConfigResponse response = wageConfigService.getCurrentWageConfig();

        assertNotNull(response);
        assertEquals(activeConfig.getId(), response.getId());
        assertEquals(activeConfig.getUpahBuruhPerKg(), response.getUpahBuruhPerKg());
        assertEquals(activeConfig.getUpahSupirPerKg(), response.getUpahSupirPerKg());
        assertEquals(activeConfig.getUpahMandorPerKg(), response.getUpahMandorPerKg());
        assertEquals("SawitDollar", response.getCurrency());
        assertTrue(response.getIsActive());
        assertNotNull(response.getUpdatedBy());
        assertEquals(adminId, response.getUpdatedBy().getId());
        assertNull(response.getUpdatedBy().getName());
        assertEquals(activeConfig.getEffectiveFrom(), response.getEffectiveFrom());
        assertEquals(activeConfig.getCreatedAt(), response.getCreatedAt());

        verify(wageConfigRepository).findByIsActiveTrue();
    }

    @Test
    void getCurrentWageConfigShouldThrowExceptionWhenNoActiveConfigExists() {
        when(wageConfigRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        assertThrows(ActiveWageConfigNotFoundException.class,
                () -> wageConfigService.getCurrentWageConfig());

        verify(wageConfigRepository).findByIsActiveTrue();
    }

    @Test
    void createWageConfigShouldDeactivatePreviousAndCreateNewConfigWhenActiveConfigExists() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(new BigDecimal("4.00"));
        request.setUpahSupirPerKg(new BigDecimal("3.00"));
        request.setUpahMandorPerKg(new BigDecimal("2.00"));

        WageConfig previousConfig = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .isActive(true)
                .updatedBy(UUID.randomUUID())
                .effectiveFrom(now.minusDays(1))
                .createdAt(now.minusDays(1))
                .build();

        OffsetDateTime newTime = OffsetDateTime.now();

        WageConfig savedNewConfig = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(request.getUpahBuruhPerKg())
                .upahSupirPerKg(request.getUpahSupirPerKg())
                .upahMandorPerKg(request.getUpahMandorPerKg())
                .isActive(true)
                .updatedBy(adminId)
                .effectiveFrom(newTime)
                .createdAt(newTime)
                .build();

        when(wageConfigRepository.findByIsActiveTrue()).thenReturn(Optional.of(previousConfig));
        when(wageConfigRepository.saveAndFlush(previousConfig)).thenReturn(previousConfig);
        when(wageConfigRepository.save(any(WageConfig.class))).thenReturn(savedNewConfig);

        CreateWageConfigResponse response = wageConfigService.createWageConfig(request, adminId);

        assertNotNull(response);
        assertEquals(savedNewConfig.getId(), response.getId());
        assertEquals(new BigDecimal("4.00"), response.getUpahBuruhPerKg());
        assertEquals(new BigDecimal("3.00"), response.getUpahSupirPerKg());
        assertEquals(new BigDecimal("2.00"), response.getUpahMandorPerKg());
        assertEquals("SawitDollar", response.getCurrency());
        assertTrue(response.getIsActive());

        assertNotNull(response.getUpdatedBy());
        assertEquals(adminId, response.getUpdatedBy().getId());
        assertNull(response.getUpdatedBy().getName());

        assertNotNull(response.getPreviousConfig());
        assertEquals(previousConfig.getId(), response.getPreviousConfig().getId());
        assertEquals(previousConfig.getUpahBuruhPerKg(), response.getPreviousConfig().getUpahBuruhPerKg());
        assertEquals(previousConfig.getUpahSupirPerKg(), response.getPreviousConfig().getUpahSupirPerKg());
        assertEquals(previousConfig.getUpahMandorPerKg(), response.getPreviousConfig().getUpahMandorPerKg());
        assertEquals(savedNewConfig.getEffectiveFrom(), response.getPreviousConfig().getDeactivatedAt());

        assertFalse(previousConfig.getIsActive());

        ArgumentCaptor<WageConfig> newConfigCaptor = ArgumentCaptor.forClass(WageConfig.class);
        verify(wageConfigRepository).saveAndFlush(previousConfig);
        verify(wageConfigRepository).save(newConfigCaptor.capture());

        WageConfig capturedNewConfig = newConfigCaptor.getValue();
        assertEquals(request.getUpahBuruhPerKg(), capturedNewConfig.getUpahBuruhPerKg());
        assertEquals(request.getUpahSupirPerKg(), capturedNewConfig.getUpahSupirPerKg());
        assertEquals(request.getUpahMandorPerKg(), capturedNewConfig.getUpahMandorPerKg());
        assertEquals(adminId, capturedNewConfig.getUpdatedBy());
    }

    @Test
    void createWageConfigShouldCreateNewConfigWithoutPreviousConfigWhenNoActiveConfigExists() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(new BigDecimal("4.00"));
        request.setUpahSupirPerKg(new BigDecimal("3.00"));
        request.setUpahMandorPerKg(new BigDecimal("2.00"));

        OffsetDateTime newTime = OffsetDateTime.now();

        WageConfig savedNewConfig = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(request.getUpahBuruhPerKg())
                .upahSupirPerKg(request.getUpahSupirPerKg())
                .upahMandorPerKg(request.getUpahMandorPerKg())
                .isActive(true)
                .updatedBy(adminId)
                .effectiveFrom(newTime)
                .createdAt(newTime)
                .build();

        when(wageConfigRepository.findByIsActiveTrue()).thenReturn(Optional.empty());
        when(wageConfigRepository.save(any(WageConfig.class))).thenReturn(savedNewConfig);

        CreateWageConfigResponse response = wageConfigService.createWageConfig(request, adminId);

        assertNotNull(response);
        assertEquals(savedNewConfig.getId(), response.getId());
        assertEquals(new BigDecimal("4.00"), response.getUpahBuruhPerKg());
        assertEquals(new BigDecimal("3.00"), response.getUpahSupirPerKg());
        assertEquals(new BigDecimal("2.00"), response.getUpahMandorPerKg());
        assertTrue(response.getIsActive());
        assertNull(response.getPreviousConfig());

        verify(wageConfigRepository, never()).saveAndFlush(any(WageConfig.class));
        verify(wageConfigRepository).save(any(WageConfig.class));
    }

    @Test
    void getWageConfigHistoryShouldReturnMappedHistoryResponses() {
        Pageable pageable = PageRequest.of(0, 10);

        WageConfig config1 = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("4.00"))
                .upahSupirPerKg(new BigDecimal("3.00"))
                .upahMandorPerKg(new BigDecimal("2.00"))
                .isActive(true)
                .updatedBy(UUID.randomUUID())
                .effectiveFrom(now)
                .createdAt(now)
                .build();

        WageConfig config2 = WageConfig.builder()
                .id(UUID.randomUUID())
                .upahBuruhPerKg(new BigDecimal("3.00"))
                .upahSupirPerKg(new BigDecimal("2.00"))
                .upahMandorPerKg(new BigDecimal("1.50"))
                .isActive(false)
                .updatedBy(UUID.randomUUID())
                .effectiveFrom(now.minusDays(1))
                .createdAt(now.minusDays(1))
                .build();

        Page<WageConfig> page = new PageImpl<>(List.of(config1, config2), pageable, 2);
        when(wageConfigRepository.findAllByOrderByCreatedAtDesc(pageable)).thenReturn(page);

        Page<HistoryWageConfigResponse> responsePage = wageConfigService.getWageConfigHistory(pageable);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getTotalElements());
        assertEquals(2, responsePage.getContent().size());

        HistoryWageConfigResponse first = responsePage.getContent().getFirst();
        assertEquals(config1.getId(), first.getId());
        assertEquals(config1.getUpahBuruhPerKg(), first.getUpahBuruhPerKg());
        assertEquals(config1.getUpahSupirPerKg(), first.getUpahSupirPerKg());
        assertEquals(config1.getUpahMandorPerKg(), first.getUpahMandorPerKg());
        assertTrue(first.getIsActive());
        assertEquals(config1.getUpdatedBy(), first.getUpdatedBy().getId());
        assertNull(first.getUpdatedBy().getName());
        assertEquals(config1.getEffectiveFrom(), first.getEffectiveFrom());

        HistoryWageConfigResponse second = responsePage.getContent().get(1);
        assertEquals(config2.getId(), second.getId());
        assertFalse(second.getIsActive());

        verify(wageConfigRepository).findAllByOrderByCreatedAtDesc(pageable);
    }
}