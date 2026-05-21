package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticatedUserTest {

    @Test
    void fromShouldBuildAuthenticatedUserWhenClaimsAreValid() {
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", userId.toString());
        request.setAttribute("userRole", "ADMIN");

        AuthenticatedUser user = AuthenticatedUser.from(request);

        assertEquals(userId, user.id());
        assertEquals(UserRole.ADMIN, user.role());
    }

    @Test
    void fromShouldThrowBadRequestWhenClaimsAreMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AuthenticatedUser.from(request)
        );

        assertEquals("Authenticated user claims are required", exception.getMessage());
    }

    @Test
    void fromShouldThrowBadRequestWhenClaimsAreMalformed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", "not-a-uuid");
        request.setAttribute("userRole", "ADMIN");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AuthenticatedUser.from(request)
        );

        assertEquals("Authenticated user claims are invalid", exception.getMessage());
    }

    @Test
    void fromShouldThrowBadRequestWhenRoleClaimIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", UUID.randomUUID().toString());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> AuthenticatedUser.from(request)
        );

        assertEquals("Authenticated user claims are required", exception.getMessage());
    }
}
