package id.ac.ui.cs.advprog.mysawitpayment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateWageConfigRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Upah buruh per kg must be greater than 0")
    private BigDecimal upahBuruhPerKg;

    @NotNull
    @DecimalMin(value = "0.01", message = "Upah supir per kg must be greater than 0")
    private BigDecimal upahSupirPerKg;

    @NotNull
    @DecimalMin(value = "0.01", message = "Upah mandor per kg must be greater than 0")
    private BigDecimal upahMandorPerKg;
}
