package id.ac.ui.cs.advprog.mysawitpayment.dto.request.filter;

import id.ac.ui.cs.advprog.mysawitpayment.model.enums.PayrollStatus;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.ReferenceType;
import id.ac.ui.cs.advprog.mysawitpayment.model.enums.UserRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollFilter(
        UUID userId,
        PayrollStatus status,
        UserRole userRole,
        ReferenceType referenceType,
        OffsetDateTime dateFrom,
        OffsetDateTime dateTo
) {
}
