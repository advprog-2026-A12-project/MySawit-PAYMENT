package id.ac.ui.cs.advprog.mysawitpayment.security;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        if ("/api/v1/topup/callback".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/v1/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            ErrorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        Claims claims = jwtUtil.extractClaims(token);

        if (!hasValidRequiredClaims(claims)) {
            ErrorResponseWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        // Make user info available to controllers
        request.setAttribute("userId", claims.getSubject());
        request.setAttribute("userRole", claims.get("role", String.class));
        request.setAttribute("userEmail", claims.get("email", String.class));
        request.setAttribute("userName", claims.get("name", String.class));

        chain.doFilter(request, response);
    }

    private boolean hasValidRequiredClaims(Claims claims) {
        if (claims == null || claims.getSubject() == null) {
            return false;
        }

        String role = claims.get("role", String.class);
        if (role == null) {
            return false;
        }

        try {
            UUID.fromString(claims.getSubject());
            UserRole.valueOf(role);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
