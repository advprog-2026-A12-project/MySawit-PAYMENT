package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;
    private OffsetDateTime timestamp;

}