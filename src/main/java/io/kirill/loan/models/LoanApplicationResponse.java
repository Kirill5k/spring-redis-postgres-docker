package io.kirill.loan.models;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
public class LoanApplicationResponse {
    private final Long id;
}
