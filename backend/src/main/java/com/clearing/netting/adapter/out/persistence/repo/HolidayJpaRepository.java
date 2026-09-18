package com.clearing.netting.adapter.out.persistence.repo;

import com.clearing.netting.adapter.out.persistence.entity.HolidayJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayJpaRepository extends JpaRepository<HolidayJpaEntity, LocalDate> {

    List<HolidayJpaEntity> findAllByOrderByHolidayDateAsc();
}
