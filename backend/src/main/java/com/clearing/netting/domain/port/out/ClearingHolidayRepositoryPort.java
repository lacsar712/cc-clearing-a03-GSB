package com.clearing.netting.domain.port.out;

import com.clearing.netting.domain.model.ClearingHoliday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClearingHolidayRepositoryPort {
    ClearingHoliday save(ClearingHoliday holiday);

    Optional<ClearingHoliday> findById(LocalDate holidayDate);

    List<ClearingHoliday> findAllOrderByDateAsc();

    boolean existsById(LocalDate holidayDate);

    void deleteById(LocalDate holidayDate);
}
