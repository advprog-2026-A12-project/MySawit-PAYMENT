package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentAuthorizationServiceTest {

    private final PaymentAuthorizationService authorizationService = new PaymentAuthorizationService();

    @Test
    void requireAdminShouldAllowAdminOnly() {
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser worker = new AuthenticatedUser(UUID.randomUUID(), UserRole.BURUH);

        assertDoesNotThrow(() -> authorizationService.requireAdmin(admin));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireAdmin(worker));
    }

    @Test
    void requirePayrollRecipientShouldRejectAdmin() {
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser mandor = new AuthenticatedUser(UUID.randomUUID(), UserRole.MANDOR);

        assertDoesNotThrow(() -> authorizationService.requirePayrollRecipient(mandor));
        assertThrows(ForbiddenException.class, () -> authorizationService.requirePayrollRecipient(admin));
    }

    @Test
    void canViewPayrollShouldAllowAdminOrOwnerOnly() {
        UUID ownerId = UUID.randomUUID();
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser owner = new AuthenticatedUser(ownerId, UserRole.BURUH);
        AuthenticatedUser otherUser = new AuthenticatedUser(UUID.randomUUID(), UserRole.SUPIR_TRUK);

        assertTrue(authorizationService.canViewPayroll(admin, ownerId));
        assertTrue(authorizationService.canViewPayroll(owner, ownerId));
        assertFalse(authorizationService.canViewPayroll(otherUser, ownerId));
        assertFalse(authorizationService.canViewPayroll(null, ownerId));
    }

    @Test
    void requirePayrollViewerShouldAllowAdminOrOwnerOnly() {
        UUID ownerId = UUID.randomUUID();
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser otherUser = new AuthenticatedUser(UUID.randomUUID(), UserRole.BURUH);

        assertDoesNotThrow(() -> authorizationService.requirePayrollViewer(admin, ownerId));
        assertThrows(ForbiddenException.class, () -> authorizationService.requirePayrollViewer(otherUser, ownerId));
    }

    @Test
    void requireTopUpOwnerShouldAllowOwningAdminOnly() {
        UUID ownerAdminId = UUID.randomUUID();
        AuthenticatedUser ownerAdmin = new AuthenticatedUser(ownerAdminId, UserRole.ADMIN);
        AuthenticatedUser otherAdmin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser worker = new AuthenticatedUser(ownerAdminId, UserRole.BURUH);

        assertDoesNotThrow(() -> authorizationService.requireTopUpOwner(ownerAdmin, ownerAdminId));
        assertFalse(authorizationService.canViewTopUp(null, ownerAdminId));
        assertThrows(
                ForbiddenException.class,
                () -> authorizationService.requireTopUpOwner(otherAdmin, ownerAdminId)
        );
        assertThrows(
                ForbiddenException.class,
                () -> authorizationService.requireTopUpOwner(worker, ownerAdminId)
        );
    }

    @Test
    void requireOwnWalletAccessShouldAllowAnyAuthenticatedUser() {
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser worker = new AuthenticatedUser(UUID.randomUUID(), UserRole.BURUH);

        assertDoesNotThrow(() -> authorizationService.requireOwnWalletAccess(admin));
        assertDoesNotThrow(() -> authorizationService.requireOwnWalletAccess(worker));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireOwnWalletAccess(null));
    }

    @Test
    void requireAdminWalletViewerShouldAllowAdminOnly() {
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser mandor = new AuthenticatedUser(UUID.randomUUID(), UserRole.MANDOR);

        assertDoesNotThrow(() -> authorizationService.requireAdminWalletViewer(admin));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireAdminWalletViewer(mandor));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireAdminWalletViewer(null));
    }

    @Test
    void requireWageConfigManagerShouldAllowAdminOnly() {
        AuthenticatedUser admin = new AuthenticatedUser(UUID.randomUUID(), UserRole.ADMIN);
        AuthenticatedUser driver = new AuthenticatedUser(UUID.randomUUID(), UserRole.SUPIR_TRUK);

        assertDoesNotThrow(() -> authorizationService.requireWageConfigManager(admin));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireWageConfigManager(driver));
        assertThrows(ForbiddenException.class, () -> authorizationService.requireWageConfigManager(null));
    }
}
