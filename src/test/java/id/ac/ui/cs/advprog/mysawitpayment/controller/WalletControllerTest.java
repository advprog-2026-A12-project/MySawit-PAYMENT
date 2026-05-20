package id.ac.ui.cs.advprog.mysawitpayment.controller;

import id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter.WalletTransactionFilter;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletTransactionResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.WalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.AdminWalletResponse;
import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.security.AuthenticatedUser;
import id.ac.ui.cs.advprog.mysawitpayment.security.JwtFilter;
import id.ac.ui.cs.advprog.mysawitpayment.service.WalletService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = WalletController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void getMyWalletSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        WalletResponse response = new WalletResponse();
        response.setId(UUID.randomUUID());
        response.setUserId(userId);
        response.setBalance(new BigDecimal("1000.00"));
        response.setCurrency("SawitDollar");
        response.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(walletService.getMyWallet(any(AuthenticatedUser.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/me")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.balance").value(1000.00));

        verify(walletService).getMyWallet(any(AuthenticatedUser.class));
    }

    @Test
    void getTransactionsSuccess() throws Exception {
        UUID userId = UUID.randomUUID();

        WalletTransactionResponse tx = new WalletTransactionResponse();
        tx.setId(UUID.randomUUID());
        tx.setTransactionType("CREDIT");
        tx.setAmount(new BigDecimal("500.00"));
        tx.setBalanceBefore(new BigDecimal("500.00"));
        tx.setBalanceAfter(new BigDecimal("1000.00"));
        tx.setReferenceType("PAYROLL");
        tx.setReferenceId(UUID.randomUUID());
        tx.setDescription("Payroll");
        tx.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Page<WalletTransactionResponse> page = new PageImpl<>(
                List.of(tx),
                PageRequest.of(0, 20, Sort.by("createdAt").descending()),
                1
        );

        when(walletService.getMyTransactions(
                any(AuthenticatedUser.class),
                any(WalletTransactionFilter.class),
                any(Pageable.class)
        ))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/wallets/me/transactions")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH")
                        .param("transactionType", "CREDIT")
                        .param("dateFrom", "2026-05-01")
                        .param("dateTo", "2026-05-20")
                        .param("sort", "amount,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20));

        ArgumentCaptor<WalletTransactionFilter> filterCaptor =
                ArgumentCaptor.forClass(WalletTransactionFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(walletService).getMyTransactions(
                any(AuthenticatedUser.class),
                filterCaptor.capture(),
                pageableCaptor.capture()
        );

        assertEquals("CREDIT", filterCaptor.getValue().transactionType().name());
        assertEquals("2026-05-01T00:00Z", filterCaptor.getValue().dateFrom().toString());
        assertEquals("2026-05-21T00:00Z", filterCaptor.getValue().dateTo().toString());
        assertEquals("amount: ASC", pageableCaptor.getValue().getSort().toString());
    }

    @Test
    void getTransactionsInvalidPage() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/me/transactions")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Page must be >= 0"));

        verify(walletService, never()).getMyTransactions(any(AuthenticatedUser.class), any(), any());
    }

    @Test
    void getTransactionsInvalidSize101() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/me/transactions")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));

        verify(walletService, never()).getMyTransactions(any(AuthenticatedUser.class), any(), any());
    }

    @Test
    void getTransactionsInvalidSizeZero() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/me/transactions")
                        .requestAttr("userId", userId.toString())
                        .requestAttr("userRole", "BURUH")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Size must be between 1 and 100"));

        verify(walletService, never()).getMyTransactions(any(AuthenticatedUser.class), any(), any());
    }

    @Test
    void getWalletByUserIdAdmin() throws Exception {
        UUID targetUserId = UUID.randomUUID();

        AdminWalletResponse response = new AdminWalletResponse();
        response.setId(UUID.randomUUID());
        response.setUserId(targetUserId);
        response.setBalance(new BigDecimal("2000.00"));
        response.setCurrency("SawitDollar");
        response.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        response.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        when(walletService.getWalletByUserId(any(AuthenticatedUser.class), eq(targetUserId))).thenReturn(response);

        mockMvc.perform(get("/api/v1/wallets/{userId}", targetUserId)
                        .requestAttr("userId", UUID.randomUUID().toString())
                .requestAttr("userRole", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(targetUserId.toString()))
                .andExpect(jsonPath("$.data.userName").doesNotExist())
                .andExpect(jsonPath("$.data.userRole").doesNotExist());

        verify(walletService).getWalletByUserId(any(AuthenticatedUser.class), eq(targetUserId));
    }

    @Test
    void getWalletByUserIdForbidden() throws Exception {
        UUID targetUserId = UUID.randomUUID();
        when(walletService.getWalletByUserId(any(AuthenticatedUser.class), eq(targetUserId)))
                .thenThrow(new ForbiddenException());

        mockMvc.perform(get("/api/v1/wallets/{userId}", targetUserId)
                        .requestAttr("userId", UUID.randomUUID().toString())
                        .requestAttr("userRole", "BURUH"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Forbidden"));

        verify(walletService).getWalletByUserId(any(AuthenticatedUser.class), eq(targetUserId));
    }
}
