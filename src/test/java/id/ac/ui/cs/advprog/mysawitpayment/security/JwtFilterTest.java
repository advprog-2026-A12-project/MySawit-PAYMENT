package id.ac.ui.cs.advprog.mysawitpayment.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        when(claims.getSubject()).thenReturn("user-123");
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(claims.get("email", String.class)).thenReturn("admin@test.com");
        when(claims.get("name", String.class)).thenReturn("Admin");

        jwtFilter.doFilter(request, response, chain);

        assertEquals("user-123", request.getAttribute("userId"));
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

        verify(chain, never()).doFilter(request, response);
    }
}