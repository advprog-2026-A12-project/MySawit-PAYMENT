package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public record AuthenticatedUser(UUID id, UserRole role) {

    public static AuthenticatedUser from(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        if (userId == null || userRole == null) {
            throw new IllegalArgumentException("Authenticated user claims are required");
        }

        try {
            return new AuthenticatedUser(UUID.fromString(userId), UserRole.valueOf(userRole));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Authenticated user claims are invalid", exception);
        }
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }
}
