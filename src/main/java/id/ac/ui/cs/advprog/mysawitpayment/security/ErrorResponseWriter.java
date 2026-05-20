package id.ac.ui.cs.advprog.mysawitpayment.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import id.ac.ui.cs.advprog.mysawitpayment.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.OffsetDateTime;

final class ErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ErrorResponseWriter() {
    }

    static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = ApiResponse.<Object>builder()
                .status("error")
                .message(message)
                .data(null)
                .timestamp(OffsetDateTime.now())
                .build();

        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(body));
    }
}
