package io.kirill.loan.models;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class LoanExtensionRequest {

    @NotNull
    @Min(2)
    @Max(52)
    private final Integer term;
}
