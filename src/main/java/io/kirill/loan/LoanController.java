package io.kirill.loan;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.kirill.customer.CustomerService;
import io.kirill.loan.models.LoanApplicationRequest;
import io.kirill.loan.models.LoanApplicationResponse;
import io.kirill.loan.models.LoanExtensionRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {
    private static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    private final CustomerService customerService;
    private final LoanRiskAnalyst loanRiskAnalyst;
    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(CREATED)
    public LoanApplicationResponse apply(@Validated @RequestBody LoanApplicationRequest application, HttpServletRequest request) {
        loanRiskAnalyst.assess(getIpAddress(request), application.getLoanAmount(), LocalDateTime.now());
        var customer = customerService.get(application.getCustomerId());
        var loan = loanService.create(customer, application.getLoanAmount(), application.getTerm());
        return new LoanApplicationResponse(loan.getId());
    }

    private String getIpAddress(HttpServletRequest request) {
        return Optional.of(request)
                .map(req -> req.getHeader(X_FORWARDED_FOR_HEADER))
                .filter(header -> !header.isBlank())
                .orElseGet(request::getRemoteAddr);
    }

    @PutMapping("/{id}/term")
    @ResponseStatus(NO_CONTENT)
    public void extend(@PathVariable Long id, @Validated @RequestBody LoanExtensionRequest extension) {
        loanService.extend(id, extension.getTerm());
    }
}
