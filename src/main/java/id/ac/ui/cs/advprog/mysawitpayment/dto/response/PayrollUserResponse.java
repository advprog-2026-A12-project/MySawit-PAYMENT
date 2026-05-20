package id.ac.ui.cs.advprog.mysawitpayment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PayrollUserResponse {
    private UUID id;

    private String role;
}
