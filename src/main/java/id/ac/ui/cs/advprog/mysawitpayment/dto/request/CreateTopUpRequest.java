package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateTopUpRequest {

    @NotNull
    @Positive
    @DecimalMax(value = "100000.00")
    private BigDecimal amountSawitDollar;
}
