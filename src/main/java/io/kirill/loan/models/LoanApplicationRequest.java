package io.kirill.loan.models;

import java.math.BigDecimal;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class LoanApplicationRequest {

    @NotNull
    private final Long customerId;

    @Min(50)
    @Max(500)
    @NotNull
    private final BigDecimal loanAmount;

    @NotNull
    @Min(1)
    @Max(52)
    private final Integer term;
}
