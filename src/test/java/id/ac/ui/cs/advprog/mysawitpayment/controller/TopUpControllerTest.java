package id.ac.ui.cs.advprog.mysawitpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.XenditCallbackRequest;
import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.TopUpFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.CreateTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.HistoryTopUpResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.TopUpDetailResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PaymentTransactionStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.service.TopUpService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class TopUpControllerTest {

    private TopUpService topUpService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        topUpService = mock(TopUpService.class);
        TopUpController controller = new TopUpController(topUpService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createTopUpShouldReturnSuccessWhenAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        CreateTopUpResponse response = CreateTopUpResponse.builder()
                .id(transactionId)
                .amountSawitDollar(new BigDecimal("10.00"))
                .amountIdr(new BigDecimal("100000.00"))
                .exchangeRate("1 SD = Rp 10,000")
                .paymentGateway("XENDIT")
                .status(PaymentTransactionStatus.PENDING)
                .paymentUrl("https://pay.xendit.co/invoice")
                .expiresAt(now.plusHours(1))
                .createdAt(now)
                .build();

        AuthenticatedUser admin = new AuthenticatedUser(adminId, UserRole.ADMIN);

        when(topUpService.createTopUp(any(), eq(admin))).thenReturn(response);

        String requestBody = """
                {
                  "amountSawitDollar": 10.00
                }
                """;

        mockMvc.perform(post("/api/v1/topup")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", adminId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Top-up created successfully"))
                .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.data.amountSawitDollar").value(10.00))
                .andExpect(jsonPath("$.data.amountIdr").value(100000.00))
                .andExpect(jsonPath("$.data.exchangeRate").value("1 SD = Rp 10,000"))
                .andExpect(jsonPath("$.data.paymentGateway").value("XENDIT"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.paymentUrl").value("https://pay.xendit.co/invoice"));

        verify(topUpService).createTopUp(any(), eq(admin));
    }

    @Test
    void createTopUpShouldRejectMissingAmount() throws Exception {
        mockMvc.perform(post("/api/v1/topup")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(topUpService, never()).createTopUp(any(), any());
    }

    @Test
    void createTopUpShouldRejectNonPositiveAmount() throws Exception {
        String requestBody = """
                {
                  "amountSawitDollar": 0
                }
                """;

        mockMvc.perform(post("/api/v1/topup")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(topUpService, never()).createTopUp(any(), any());
    }

    @Test
    void createTopUpShouldRejectAmountAboveMaximum() throws Exception {
        String requestBody = """
                {
                  "amountSawitDollar": 100000.01
                }
                """;

        mockMvc.perform(post("/api/v1/topup")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(topUpService, never()).createTopUp(any(), any());
    }

    @Test
    void getMyTopUpsShouldReturnSuccessWhenAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        HistoryTopUpResponse item = HistoryTopUpResponse.builder()
                .id(UUID.randomUUID())
                .amountSawitDollar(new BigDecimal("15.00"))
                .amountIdr(new BigDecimal("150000.00"))
                .paymentGateway("XENDIT")
                .status("SUCCESS")
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();

        Page<HistoryTopUpResponse> page = new PageImpl<>(
                List.of(item),
                PageRequest.of(0, 20, Sort.by("createdAt").descending()),
                1
        );

        AuthenticatedUser admin = new AuthenticatedUser(adminId, UserRole.ADMIN);

        when(topUpService.getMyTopUps(eq(admin), any(TopUpFilter.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/topup")
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", adminId.toString())
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "SUCCESS")
                        .param("sort", "amountSawitDollar,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Top-up history retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].id").value(item.getId().toString()))
                .andExpect(jsonPath("$.data.content[0].amountSawitDollar").value(15.00))
                .andExpect(jsonPath("$.data.content[0].amountIdr").value(150000.00))
                .andExpect(jsonPath("$.data.content[0].paymentGateway").value("XENDIT"))
                .andExpect(jsonPath("$.data.content[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));

        ArgumentCaptor<TopUpFilter> filterCaptor = ArgumentCaptor.forClass(TopUpFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(topUpService).getMyTopUps(eq(admin), filterCaptor.capture(), pageableCaptor.capture());
        assertEquals(PaymentTransactionStatus.SUCCESS, filterCaptor.getValue().status());
        assertEquals("amountSawitDollar: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getTopUpDetailShouldReturnSuccessWhenAdmin() throws Exception {
        UUID adminId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        OffsetDateTime updatedAt = OffsetDateTime.now(ZoneOffset.UTC);

        TopUpDetailResponse response = TopUpDetailResponse.builder()
                .id(transactionId)
                .amountSawitDollar(new BigDecimal("20.00"))
                .amountIdr(new BigDecimal("200000.00"))
                .exchangeRate("1 SD = Rp 10,000")
                .paymentGateway("XENDIT")
                .gatewayReferenceId("inv-123")
                .status(PaymentTransactionStatus.SUCCESS)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AuthenticatedUser admin = new AuthenticatedUser(adminId, UserRole.ADMIN);

        when(topUpService.getTopUpDetail(transactionId, admin)).thenReturn(response);

        mockMvc.perform(get("/api/v1/topup/{topupId}", transactionId)
                        .requestAttr("userRole", "ADMIN")
                        .requestAttr("userId", adminId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Top-up detail retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.data.amountSawitDollar").value(20.00))
                .andExpect(jsonPath("$.data.amountIdr").value(200000.00))
                .andExpect(jsonPath("$.data.exchangeRate").value("1 SD = Rp 10,000"))
                .andExpect(jsonPath("$.data.paymentGateway").value("XENDIT"))
                .andExpect(jsonPath("$.data.gatewayReferenceId").value("inv-123"))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        verify(topUpService).getTopUpDetail(transactionId, admin);
    }

    @Test
    void createTopUpForbidden() {
        when(topUpService.createTopUp(any(), any()))
                .thenThrow(new ForbiddenException());

        String requestBody = """
            {
              "amountSawitDollar": 10.00
            }
            """;

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(post("/api/v1/topup")
                        .requestAttr("userRole", "BURUH")
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        assertEquals("Forbidden", ex.getCause().getMessage());

        verify(topUpService).createTopUp(any(), any());
    }

    @Test
    void getMyTopUpsForbidden() {
        when(topUpService.getMyTopUps(any(), any(), any()))
                .thenThrow(new ForbiddenException());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/topup")
                        .requestAttr("userRole", "MANDOR")
                        .requestAttr("userId", UUID.randomUUID().toString()))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        assertEquals("Forbidden", ex.getCause().getMessage());

        verify(topUpService).getMyTopUps(any(), any(), any());
    }

    @Test
    void getTopUpDetailForbidden() {
        UUID topupId = UUID.randomUUID();
        when(topUpService.getTopUpDetail(eq(topupId), any()))
                .thenThrow(new ForbiddenException());

        ServletException ex = assertThrows(ServletException.class, () ->
                mockMvc.perform(get("/api/v1/topup/{topupId}", topupId)
                        .requestAttr("userRole", "SUPIR_TRUK")
                        .requestAttr("userId", UUID.randomUUID().toString()))
        );

        assertNotNull(ex.getCause());
        assertEquals(ForbiddenException.class, ex.getCause().getClass());
        assertEquals("Forbidden", ex.getCause().getMessage());

        verify(topUpService).getTopUpDetail(eq(topupId), any());
    }

    @Test
    void handleXenditCallbackShouldReturnSuccess() throws Exception {
        XenditCallbackRequest request = new XenditCallbackRequest();
        request.setId("inv-123");
        request.setExternalId(UUID.randomUUID().toString());
        request.setStatus("PAID");
        request.setAmount(new BigDecimal("100000.00"));
        request.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(post("/api/v1/topup/callback")
                        .header("x-callback-token", "callback-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.findAndRegisterModules().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(topUpService).handleXenditCallback(eq("callback-token"), any(XenditCallbackRequest.class));
    }
}
