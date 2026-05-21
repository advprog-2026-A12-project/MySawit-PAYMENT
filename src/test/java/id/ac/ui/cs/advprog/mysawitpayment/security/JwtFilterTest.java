package id.ac.ui.cs.advprog.mysawitpayment.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;

class JwtFilterTest {

    private JwtUtil jwtUtil;
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        jwtFilter = new JwtFilter(jwtUtil);
    }

    @Test
    void testMissingToken() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing token"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidToken() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalidtoken");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        when(jwtUtil.isValid("invalidtoken")).thenReturn(false);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid token"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testValidToken() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer validtoken");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);

        when(jwtUtil.isValid("validtoken")).thenReturn(true);
        when(jwtUtil.extractClaims("validtoken")).thenReturn(claims);

        String userId = UUID.randomUUID().toString();

        when(claims.getSubject()).thenReturn(userId);
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(claims.get("email", String.class)).thenReturn("admin@test.com");
        when(claims.get("name", String.class)).thenReturn("Admin");

        jwtFilter.doFilter(request, response, chain);

        assertEquals(userId, request.getAttribute("userId"));
        assertEquals("ADMIN", request.getAttribute("userRole"));
        assertEquals("admin@test.com", request.getAttribute("userEmail"));
        assertEquals("Admin", request.getAttribute("userName"));

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void testAuthorizationHeaderWithoutBearer() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123"); // bukan Bearer

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing token"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidTokenWhenSubjectClaimIsMalformed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer malformedclaims");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);
        when(jwtUtil.isValid("malformedclaims")).thenReturn(true);
        when(jwtUtil.extractClaims("malformedclaims")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("not-a-uuid");
        when(claims.get("role", String.class)).thenReturn("ADMIN");

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid token"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testInvalidTokenWhenRoleClaimIsMalformed() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer badrole");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        Claims claims = mock(Claims.class);
        when(jwtUtil.isValid("badrole")).thenReturn(true);
        when(jwtUtil.extractClaims("badrole")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(UUID.randomUUID().toString());
        when(claims.get("role", String.class)).thenReturn("NOT_A_ROLE");

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid token"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void testShouldBypassJwtValidationForTopupCallback() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/topup/callback");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).isValid(anyString());
        verify(jwtUtil, never()).extractClaims(anyString());
    }

    @Test
    void testShouldBypassJwtValidationForCorsPreflightOptionsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("OPTIONS");
        request.setRequestURI("/api/v1/wallets/me");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "GET");
        request.addHeader("Access-Control-Request-Headers", "Authorization");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).isValid(anyString());
        verify(jwtUtil, never()).extractClaims(anyString());
    }

    @Test
    void testShouldBypassJwtValidationForInternalEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal/wallets");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).isValid(anyString());
        verify(jwtUtil, never()).extractClaims(anyString());
    }

    @Test
    void testShouldNotBypassJwtValidationForInternalPathWithoutTrailingSlash() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing token"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }
}
