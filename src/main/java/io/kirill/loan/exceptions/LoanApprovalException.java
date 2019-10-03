package io.kirill.loan.exceptions;

import io.kirill.common.exceptions.ApiErrorException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

public class LoanApprovalException extends ApiErrorException {
    public LoanApprovalException(String ipAddress, int attempts, int hoursPeriod) {
        super(HttpStatus.FORBIDDEN, String.format("denied. %d attempts within %d hours from %s", attempts, hoursPeriod, ipAddress));
    }

    public LoanApprovalException(BigDecimal loanAmount) {
        super(HttpStatus.FORBIDDEN, String.format("denied. request for %d during unsafe hours", loanAmount.intValue()));
    }
}
