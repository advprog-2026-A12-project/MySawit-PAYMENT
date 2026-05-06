package id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class WalletCreationRequest {

    @NotNull
    private UUID userId;
}
