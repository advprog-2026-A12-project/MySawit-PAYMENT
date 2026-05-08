package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentAuthorizationService {

    public boolean canAdmin(AuthenticatedUser user) {
        return user != null && user.isAdmin();
    }

    public boolean canViewOwnPayrolls(AuthenticatedUser user) {
        return user != null && !user.isAdmin();
    }

    public boolean canViewPayroll(AuthenticatedUser user, UUID payrollOwnerId) {
        if (user == null) {
            return false;
        }

        return user.isAdmin() || user.id().equals(payrollOwnerId);
    }

    public boolean canViewTopUp(AuthenticatedUser user, UUID topUpAdminId) {
        if (user == null || !user.isAdmin()) {
            return false;
        }

        return user.id().equals(topUpAdminId);
    }

    public void requireAdmin(AuthenticatedUser user) {
        forbidUnless(canAdmin(user));
    }

    public void requirePayrollRecipient(AuthenticatedUser user) {
        forbidUnless(canViewOwnPayrolls(user));
    }

    public void requirePayrollViewer(AuthenticatedUser user, UUID payrollOwnerId) {
        forbidUnless(canViewPayroll(user, payrollOwnerId));
    }

    public void requireTopUpOwner(AuthenticatedUser user, UUID topUpAdminId) {
        forbidUnless(canViewTopUp(user, topUpAdminId));
    }

    private void forbidUnless(boolean allowed) {
        if (!allowed) {
            throw new ForbiddenException();
        }
    }
}