package id.ac.ui.cs.advprog.mysawitpayment.dto.request.internal;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class PayrollCreationRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UserRole userRole;

    @NotNull
    private ReferenceType referenceType;

    @NotNull
    private UUID referenceId;

    @NotNull
    @Positive
    private BigDecimal kilogram;
}
