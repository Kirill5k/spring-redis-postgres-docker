package io.kirill.loan;

import static java.util.Optional.ofNullable;

import io.kirill.loan.exceptions.LoanApprovalException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoanRiskAnalyst {
    private static final String HASH_KEY = "ip-address";

    private static final int MAX_LOAN_AMOUNT = 500;
    private static final int MAX_APPLICATIONS_PER_IP = 3;
    private static final int HOURS_COOLDOWN_TIME = 24;
    private static final LocalTime UNSAFE_TIME_PERIOD_START = LocalTime.MIDNIGHT;
    private static final LocalTime UNSAFE_TIME_PERIOD_END = LocalTime.of(6, 0, 0);

    private final HashOperations<String, String, LinkedList<LocalDateTime>> ipRegistry;

    public LoanRiskAnalyst(RedisTemplate<String, LinkedList<LocalDateTime>> redisTemplate) {
        ipRegistry = redisTemplate.opsForHash();
    }

    public void assess(String ipAddress, BigDecimal loanAmount, LocalDateTime requestDateTime) {
        verifyTimeAndAmount(loanAmount, requestDateTime);
        verifyIpAddress(ipAddress, requestDateTime);
    }

    private void verifyTimeAndAmount(BigDecimal loanAmount, LocalDateTime requestDateTime) {
        var time = requestDateTime.toLocalTime();
        if (loanAmount.intValue() == MAX_LOAN_AMOUNT && time.isAfter(UNSAFE_TIME_PERIOD_START) && time.isBefore(UNSAFE_TIME_PERIOD_END)) {
            throw new LoanApprovalException(loanAmount);
        }
    }

    private void verifyIpAddress(String ipAddress, LocalDateTime requestDateTime) {
        var requestDates = ofNullable(ipRegistry.get(HASH_KEY, ipAddress)).orElseGet(LinkedList::new);
        requestDates.addLast(requestDateTime);
        if (requestDates.size() == MAX_APPLICATIONS_PER_IP) {
            var earliestDate = requestDates.removeFirst();
            ipRegistry.put(HASH_KEY, ipAddress, requestDates);
            var hoursBetweenRequests = ChronoUnit.HOURS.between(earliestDate, requestDateTime);
            if (hoursBetweenRequests < HOURS_COOLDOWN_TIME) {
                throw new LoanApprovalException(ipAddress, MAX_APPLICATIONS_PER_IP, HOURS_COOLDOWN_TIME);
            }
        } else {
            ipRegistry.put(HASH_KEY, ipAddress, requestDates);
        }
    }
}
