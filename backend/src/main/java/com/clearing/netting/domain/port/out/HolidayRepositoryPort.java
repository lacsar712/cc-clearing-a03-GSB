package com.clearing.netting.domain.port.out;

import com.clearing.netting.domain.model.Holiday;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepositoryPort {

    Holiday save(Holiday holiday);

    List<Holiday> findAllOrderByDateAsc();

    boolean existsByDate(LocalDate date);

    void deleteByDate(LocalDate date);
}
