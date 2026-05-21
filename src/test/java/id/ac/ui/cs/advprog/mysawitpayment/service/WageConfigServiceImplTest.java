package id.ac.ui.cs.advprog.mysawitpayment.service;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.CreateWageConfigRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CurrentWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryWageConfigResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ActiveWageConfigNotFoundException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.exception.WageConfigConflictException;
import id.ac.ui.cs.advprog.mysawitpayment.model.WageConfig;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.repository.WageConfigRepository;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.PaymentAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WageConfigServiceImplTest {

    @Mock
    private WageConfigRepository wageConfigRepository;

    @Mock
    private PaymentAuthorizationService authorizationService;

    @InjectMocks
    private WageConfigServiceImpl wageConfigService;

    private UUID adminId;
    private WageConfig activeConfig;
    private OffsetDateTime now;

    private AuthenticatedUser adminRequester() {
        return new AuthenticatedUser(adminId, UserRole.ADMIN);
    }

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        now = OffsetDateTime.now(ZoneOffset.UTC);

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

        CurrentWageConfigResponse response = wageConfigService.getCurrentWageConfig(adminRequester());

        assertNotNull(response);
        assertEquals(activeConfig.getId(), response.getId());
        assertEquals(activeConfig.getUpahBuruhPerKg(), response.getUpahBuruhPerKg());
        assertEquals(activeConfig.getUpahSupirPerKg(), response.getUpahSupirPerKg());
        assertEquals(activeConfig.getUpahMandorPerKg(), response.getUpahMandorPerKg());
        assertEquals("SawitDollar", response.getCurrency());
        assertTrue(response.getIsActive());
        assertNotNull(response.getUpdatedBy());
        assertEquals(adminId, response.getUpdatedBy().getId());
        assertEquals(activeConfig.getEffectiveFrom(), response.getEffectiveFrom());
        assertEquals(activeConfig.getCreatedAt(), response.getCreatedAt());

        verify(authorizationService).requireWageConfigManager(adminRequester());
        verify(wageConfigRepository).findByIsActiveTrue();
    }

    @Test
    void getCurrentWageConfigShouldThrowExceptionWhenNoActiveConfigExists() {
        when(wageConfigRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        assertThrows(ActiveWageConfigNotFoundException.class,
                () -> wageConfigService.getCurrentWageConfig(adminRequester()));

        verify(authorizationService).requireWageConfigManager(adminRequester());
        verify(wageConfigRepository).findByIsActiveTrue();
    }

    @Test
    void getCurrentWageConfigShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        AuthenticatedUser requester = new AuthenticatedUser(UUID.randomUUID(), UserRole.BURUH);
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireWageConfigManager(requester);

        assertThrows(ForbiddenException.class, () -> wageConfigService.getCurrentWageConfig(requester));

        verify(authorizationService).requireWageConfigManager(requester);
        verifyNoInteractions(wageConfigRepository);
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

        OffsetDateTime newTime = OffsetDateTime.now(ZoneOffset.UTC);

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

        when(wageConfigRepository.findActiveForUpdate()).thenReturn(Optional.of(previousConfig));
        when(wageConfigRepository.saveAndFlush(previousConfig)).thenReturn(previousConfig);
        when(wageConfigRepository.save(any(WageConfig.class))).thenReturn(savedNewConfig);

        CreateWageConfigResponse response = wageConfigService.createWageConfig(request, adminRequester());

        assertNotNull(response);
        assertEquals(savedNewConfig.getId(), response.getId());
        assertEquals(new BigDecimal("4.00"), response.getUpahBuruhPerKg());
        assertEquals(new BigDecimal("3.00"), response.getUpahSupirPerKg());
        assertEquals(new BigDecimal("2.00"), response.getUpahMandorPerKg());
        assertEquals("SawitDollar", response.getCurrency());
        assertTrue(response.getIsActive());

        assertNotNull(response.getUpdatedBy());
        assertEquals(adminId, response.getUpdatedBy().getId());

        assertNotNull(response.getPreviousConfig());
        assertEquals(previousConfig.getId(), response.getPreviousConfig().getId());
        assertEquals(previousConfig.getUpahBuruhPerKg(), response.getPreviousConfig().getUpahBuruhPerKg());
        assertEquals(previousConfig.getUpahSupirPerKg(), response.getPreviousConfig().getUpahSupirPerKg());
        assertEquals(previousConfig.getUpahMandorPerKg(), response.getPreviousConfig().getUpahMandorPerKg());
        assertEquals(savedNewConfig.getEffectiveFrom(), response.getPreviousConfig().getDeactivatedAt());

        assertFalse(previousConfig.getIsActive());

        ArgumentCaptor<WageConfig> newConfigCaptor = ArgumentCaptor.forClass(WageConfig.class);
        verify(authorizationService).requireWageConfigManager(adminRequester());
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

        OffsetDateTime newTime = OffsetDateTime.now(ZoneOffset.UTC);

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

        when(wageConfigRepository.findActiveForUpdate()).thenReturn(Optional.empty());
        when(wageConfigRepository.save(any(WageConfig.class))).thenReturn(savedNewConfig);

        CreateWageConfigResponse response = wageConfigService.createWageConfig(request, adminRequester());

        assertNotNull(response);
        assertEquals(savedNewConfig.getId(), response.getId());
        assertEquals(new BigDecimal("4.00"), response.getUpahBuruhPerKg());
        assertEquals(new BigDecimal("3.00"), response.getUpahSupirPerKg());
        assertEquals(new BigDecimal("2.00"), response.getUpahMandorPerKg());
        assertTrue(response.getIsActive());
        assertNull(response.getPreviousConfig());

        verify(authorizationService).requireWageConfigManager(adminRequester());
        verify(wageConfigRepository, never()).saveAndFlush(any(WageConfig.class));
        verify(wageConfigRepository).save(any(WageConfig.class));
    }

    @Test
    void createWageConfigShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        AuthenticatedUser requester = new AuthenticatedUser(UUID.randomUUID(), UserRole.MANDOR);
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireWageConfigManager(requester);

        assertThrows(ForbiddenException.class, () -> wageConfigService.createWageConfig(request, requester));

        verify(authorizationService).requireWageConfigManager(requester);
        verifyNoInteractions(wageConfigRepository);
    }

    @Test
    void createWageConfigShouldUseSerializableTransactionIsolation() throws Exception {
        org.springframework.transaction.annotation.Transactional transactional = WageConfigServiceImpl.class
                .getMethod("createWageConfig", CreateWageConfigRequest.class, AuthenticatedUser.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class);

        assertNotNull(transactional);
        assertEquals(org.springframework.transaction.annotation.Isolation.SERIALIZABLE, transactional.isolation());
    }

    @Test
    void activeConfigLookupShouldUsePessimisticWriteLock() throws Exception {
        org.springframework.data.jpa.repository.Lock lock = WageConfigRepository.class
                .getMethod("findActiveForUpdate")
                .getAnnotation(org.springframework.data.jpa.repository.Lock.class);

        assertNotNull(lock);
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE, lock.value());
    }

    @Test
    void createWageConfigShouldMapUniqueConflictToBusinessConflict() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(new BigDecimal("4.00"));
        request.setUpahSupirPerKg(new BigDecimal("3.00"));
        request.setUpahMandorPerKg(new BigDecimal("2.00"));

        when(wageConfigRepository.findActiveForUpdate()).thenReturn(Optional.empty());
        when(wageConfigRepository.save(any(WageConfig.class)))
                .thenThrow(new DataIntegrityViolationException("active unique conflict"));

        WageConfigConflictException exception = assertThrows(
                WageConfigConflictException.class,
                () -> wageConfigService.createWageConfig(request, adminRequester())
        );

        assertEquals("Active wage config was updated concurrently", exception.getMessage());
    }

    @Test
    void createWageConfigShouldMapLockConflictToBusinessConflict() {
        CreateWageConfigRequest request = new CreateWageConfigRequest();
        request.setUpahBuruhPerKg(new BigDecimal("4.00"));
        request.setUpahSupirPerKg(new BigDecimal("3.00"));
        request.setUpahMandorPerKg(new BigDecimal("2.00"));

        when(wageConfigRepository.findActiveForUpdate())
                .thenThrow(new CannotAcquireLockException("active config locked"));

        WageConfigConflictException exception = assertThrows(
                WageConfigConflictException.class,
                () -> wageConfigService.createWageConfig(request, adminRequester())
        );

        assertEquals("Active wage config was updated concurrently", exception.getMessage());
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

        Page<HistoryWageConfigResponse> responsePage = wageConfigService.getWageConfigHistory(
                adminRequester(),
                pageable
        );

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
        assertEquals(config1.getEffectiveFrom(), first.getEffectiveFrom());

        HistoryWageConfigResponse second = responsePage.getContent().get(1);
        assertEquals(config2.getId(), second.getId());
        assertFalse(second.getIsActive());

        verify(authorizationService).requireWageConfigManager(adminRequester());
        verify(wageConfigRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    void getWageConfigHistoryShouldThrowForbiddenWhenRequesterIsNotAdmin() {
        AuthenticatedUser requester = new AuthenticatedUser(UUID.randomUUID(), UserRole.SUPIR_TRUK);
        Pageable pageable = PageRequest.of(0, 10);
        doThrow(new ForbiddenException())
                .when(authorizationService)
                .requireWageConfigManager(requester);

        assertThrows(ForbiddenException.class, () -> wageConfigService.getWageConfigHistory(requester, pageable));

        verify(authorizationService).requireWageConfigManager(requester);
        verifyNoInteractions(wageConfigRepository);
    }
}
