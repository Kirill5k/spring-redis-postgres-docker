package io.kirill.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.kirill.loan.exceptions.LoanApprovalException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

@RunWith(MockitoJUnitRunner.class)
public class LoanRiskAnalystTest {
    private static final String HASH_KEY = "ip-address";

    private LoanRiskAnalyst loanRiskAnalyst;

    private String ipAddress = "192.168.0.1";

    @Mock
    private RedisTemplate<String, LinkedList<LocalDateTime>> redisTemplate;

    @Mock
    private HashOperations ipRegistry;

    @Before
    public void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(ipRegistry);
        loanRiskAnalyst = new LoanRiskAnalyst(redisTemplate);
    }

    @Test
    public void assessWhenBorrowingMaxAmountDuringNight() {
        assertThat(catchThrowable(() -> loanRiskAnalyst.assess(ipAddress, BigDecimal.valueOf(500), LocalDateTime.of(LocalDate.now(), LocalTime.of(1, 1)))))
                .isInstanceOf(LoanApprovalException.class)
                .hasMessage("denied. request for 500 during unsafe hours");
    }

    @Test
    public void assessWhenMaking3ApplicationsWithin24Hours() {
        var currentRequestDates = List.of(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)), LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)));
        when(ipRegistry.get(HASH_KEY, ipAddress)).thenReturn(new LinkedList<>(currentRequestDates));

        assertThat(catchThrowable(() -> loanRiskAnalyst.assess(ipAddress, BigDecimal.valueOf(400), LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59)))))
                .isInstanceOf(LoanApprovalException.class)
                .hasMessage("denied. 3 attempts within 24 hours from 192.168.0.1");

        verify(ipRegistry).get(HASH_KEY, ipAddress);
        verify(ipRegistry).put(HASH_KEY, ipAddress, new LinkedList<>(List.of(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)), LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59)))));
    }

    @Test
    public void assessWhenMaking3ApplicationsNotWithin24Hours() {
        var currentRequestDates = List.of(LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(22, 0)), LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)));
        when(ipRegistry.get(HASH_KEY, ipAddress)).thenReturn(new LinkedList<>(currentRequestDates));

        assertThat(catchThrowable(() -> loanRiskAnalyst.assess(ipAddress, BigDecimal.valueOf(400), LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 1)))))
                .doesNotThrowAnyException();

        verify(ipRegistry).get(HASH_KEY, ipAddress);
        verify(ipRegistry).put(HASH_KEY, ipAddress, new LinkedList<>(List.of(LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)), LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 1)))));
    }

    @Test
    public void savesIpaddressInCache() {
        when(ipRegistry.get(HASH_KEY, ipAddress)).thenReturn(null);

        loanRiskAnalyst.assess(ipAddress, BigDecimal.valueOf(400), LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 1)));

        verify(ipRegistry).get(HASH_KEY, ipAddress);
        verify(ipRegistry).put(HASH_KEY, ipAddress, new LinkedList<>(List.of(LocalDateTime.of(LocalDate.now(), LocalTime.of(22, 1)))));
    }
}