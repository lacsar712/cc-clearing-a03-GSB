package com.clearing.netting.application;

import com.clearing.netting.domain.exception.DomainException;
import com.clearing.netting.domain.model.ClearingHoliday;
import com.clearing.netting.domain.port.out.ClearingHolidayRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClearingCalendarApplicationService {

    private final ClearingHolidayRepositoryPort holidayRepository;

    public ClearingCalendarApplicationService(ClearingHolidayRepositoryPort holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public List<ClearingHoliday> listHolidays() {
        return holidayRepository.findAllOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public boolean isHoliday(LocalDate date) {
        return date != null && holidayRepository.existsById(date);
    }

    @Transactional
    public ClearingHoliday addHoliday(LocalDate date, String name) {
        if (date == null) {
            throw new DomainException("INVALID_DATE", "holiday date is required");
        }
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new DomainException("INVALID_NAME", "holiday name is required");
        }
        if (holidayRepository.existsById(date)) {
            throw new DomainException("HOLIDAY_EXISTS", "holiday already exists: " + date);
        }
        return holidayRepository.save(ClearingHoliday.of(date, trimmed));
    }

    @Transactional
    public void removeHoliday(LocalDate date) {
        if (date == null) {
            throw new DomainException("INVALID_DATE", "holiday date is required");
        }
        if (!holidayRepository.existsById(date)) {
            throw new DomainException("HOLIDAY_NOT_FOUND", "holiday not found: " + date);
        }
        holidayRepository.deleteById(date);
    }
}
