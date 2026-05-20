package id.ac.ui.cs.advprog.mysawitpayment.security;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(1)
public class InternalApiKeyFilter implements Filter {

    private static final String INTERNAL_PATH_PREFIX = "/api/v1/internal/";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final String internalApiKey;

    public InternalApiKeyFilter(@Value("${internal.api-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        if (!path.startsWith(INTERNAL_PATH_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String providedApiKey = request.getHeader(INTERNAL_API_KEY_HEADER);

        if (providedApiKey == null || !isValidApiKey(providedApiKey)) {
            ErrorResponseWriter.write(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or missing internal API key"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isValidApiKey(String providedApiKey) {
        byte[] expected = internalApiKey.getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedApiKey.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(expected, provided);
    }
}
