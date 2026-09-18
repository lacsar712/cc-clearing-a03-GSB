package com.clearing.netting.application;

import com.clearing.netting.domain.exception.DomainException;
import com.clearing.netting.domain.model.Holiday;
import com.clearing.netting.domain.port.out.HolidayRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayApplicationService {

    private final HolidayRepositoryPort holidayRepository;

    public HolidayApplicationService(HolidayRepositoryPort holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Transactional(readOnly = true)
    public List<Holiday> listHolidays() {
        return holidayRepository.findAllOrderByDateAsc();
    }

    @Transactional(readOnly = true)
    public boolean isHoliday(LocalDate date) {
        return date != null && holidayRepository.existsByDate(date);
    }

    @Transactional
    public Holiday addHoliday(LocalDate date, String name) {
        if (date == null) {
            throw new DomainException("INVALID_DATE", "holiday date is required");
        }
        if (holidayRepository.existsByDate(date)) {
            throw new DomainException("HOLIDAY_EXISTS", "holiday already exists for " + date);
        }
        return holidayRepository.save(Holiday.of(date, name));
    }

    @Transactional
    public void removeHoliday(LocalDate date) {
        if (date == null) {
            throw new DomainException("INVALID_DATE", "holiday date is required");
        }
        if (!holidayRepository.existsByDate(date)) {
            throw new DomainException("HOLIDAY_NOT_FOUND", "holiday not found: " + date);
        }
        holidayRepository.deleteByDate(date);
    }
}
