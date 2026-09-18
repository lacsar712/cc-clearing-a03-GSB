package com.clearing.netting.application;

import com.clearing.netting.domain.exception.DomainException;
import com.clearing.netting.domain.model.NettingRun;
import com.clearing.netting.domain.port.out.HolidayRepositoryPort;
import com.clearing.netting.domain.port.out.MemberRepositoryPort;
import com.clearing.netting.domain.port.out.NetPositionRepositoryPort;
import com.clearing.netting.domain.port.out.NettingRunRepositoryPort;
import com.clearing.netting.domain.port.out.ObligationRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NettingHolidayGuardTest {

    private final LocalDate holiday = LocalDate.of(2026, 10, 1);

    private NettingRunStatusService passthroughStatusService() {
        NettingRunStatusService statusService = mock(NettingRunStatusService.class);
        when(statusService.saveInNewTx(any())).thenAnswer(inv -> inv.getArgument(0));
        return statusService;
    }

    @Test
    void blocksNettingWhenSettleDateIsHoliday() {
        HolidayRepositoryPort holidayRepo = mock(HolidayRepositoryPort.class);
        ObligationRepositoryPort obligationRepo = mock(ObligationRepositoryPort.class);
        when(holidayRepo.existsByDate(holiday)).thenReturn(true);

        NettingApplicationService service = new NettingApplicationService(
                mock(NettingRunRepositoryPort.class),
                obligationRepo,
                mock(MemberRepositoryPort.class),
                mock(NetPositionRepositoryPort.class),
                passthroughStatusService(),
                new HolidayApplicationService(holidayRepo));

        DomainException ex = assertThrows(DomainException.class, () -> service.execute(holiday, "USD"));
        assertEquals("HOLIDAY_BLOCKED", ex.getCode());

        // Guard fires before any run/obligation persistence
        verifyNoInteractions(obligationRepo);
    }

    @Test
    void allowsNettingOnBusinessDay() {
        HolidayRepositoryPort holidayRepo = mock(HolidayRepositoryPort.class);
        ObligationRepositoryPort obligationRepo = mock(ObligationRepositoryPort.class);
        when(holidayRepo.existsByDate(any())).thenReturn(false);
        when(obligationRepo.findOpenBySettleDateAndCurrency(any(), any())).thenReturn(java.util.List.of());

        NettingRunRepositoryPort runRepo = mock(NettingRunRepositoryPort.class);
        NettingRunStatusService statusService = passthroughStatusService();
        NettingApplicationService service = new NettingApplicationService(
                runRepo,
                obligationRepo,
                mock(MemberRepositoryPort.class),
                mock(NetPositionRepositoryPort.class),
                statusService,
                new HolidayApplicationService(holidayRepo));

        // Business day passes the holiday guard; with no OPEN obligations it then
        // fails inside the netting domain service with NO_OBLIGATIONS — and that
        // business failure is recorded as a FAILED run (RUNNING save + FAILED save).
        DomainException ex = assertThrows(DomainException.class, () -> service.execute(holiday, "USD"));
        assertEquals("NO_OBLIGATIONS", ex.getCode());
        verify(statusService, org.mockito.Mockito.times(2)).saveInNewTx(any(NettingRun.class));
    }

    @Test
    void rejectsDuplicateHoliday() {
        HolidayRepositoryPort holidayRepo = mock(HolidayRepositoryPort.class);
        when(holidayRepo.existsByDate(holiday)).thenReturn(true);
        HolidayApplicationService service = new HolidayApplicationService(holidayRepo);

        DomainException ex = assertThrows(DomainException.class,
                () -> service.addHoliday(holiday, "国庆节"));
        assertEquals("HOLIDAY_EXISTS", ex.getCode());
        verify(holidayRepo, never()).save(any());
    }
}
