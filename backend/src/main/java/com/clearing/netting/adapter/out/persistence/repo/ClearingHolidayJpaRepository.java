package com.clearing.netting.adapter.out.persistence.repo;

import com.clearing.netting.adapter.out.persistence.entity.ClearingHolidayJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClearingHolidayJpaRepository extends JpaRepository<ClearingHolidayJpaEntity, LocalDate> {
    List<ClearingHolidayJpaEntity> findAllByOrderByHolidayDateAsc();
}
