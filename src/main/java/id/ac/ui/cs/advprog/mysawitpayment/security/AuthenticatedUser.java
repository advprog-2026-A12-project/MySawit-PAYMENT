package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public record AuthenticatedUser(UUID id, UserRole role) {

    public static AuthenticatedUser from(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        return new AuthenticatedUser(UUID.fromString(userId), UserRole.valueOf(userRole));
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }
}
