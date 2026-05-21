package id.ac.ui.cs.advprog.mysawitpayment.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class InternalApiKeyFilterTest {

    private static final String INTERNAL_API_KEY = "secret-internal-key";

    private InternalApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new InternalApiKeyFilter(INTERNAL_API_KEY, "/api/v1/internal");
    }

    @Test
    void shouldContinueFilterChainWhenPathIsNotInternalEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/wallets/me");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldReturnUnauthorizedWhenInternalApiKeyIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal/wallets");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(Objects.requireNonNull(response.getContentType()).startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertTrue(response.getContentAsString().contains("Invalid or missing internal API key"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldReturnUnauthorizedWhenInternalApiKeyIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal/wallets");
        request.addHeader("X-Internal-Api-Key", "wrong-key");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(Objects.requireNonNull(response.getContentType()).startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertTrue(response.getContentAsString().contains("Invalid or missing internal API key"));
        assertTrue(response.getContentAsString().contains("\"status\":\"error\""));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        assertTrue(response.getContentAsString().contains("\"timestamp\""));

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChainWhenInternalApiKeyIsValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal/wallets");
        request.addHeader("X-Internal-Api-Key", INTERNAL_API_KEY);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldNotTreatInternalPathWithoutTrailingSlashAsInternalEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/internal");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void shouldUseConfiguredInternalPathPrefix() throws Exception {
        InternalApiKeyFilter customFilter = new InternalApiKeyFilter(
                INTERNAL_API_KEY,
                "/custom/internal"
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/custom/internal/wallets");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        customFilter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing internal API key"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void internalPathMatcherShouldHandleNullPath() {
        InternalPathMatcher matcher = new InternalPathMatcher("/api/v1/internal");

        assertEquals(false, matcher.matches(null));
    }

    @Test
    void internalPathMatcherShouldKeepConfiguredTrailingSlash() {
        InternalPathMatcher matcher = new InternalPathMatcher("/api/v1/internal/");

        assertEquals(true, matcher.matches("/api/v1/internal/wallets"));
    }
}
