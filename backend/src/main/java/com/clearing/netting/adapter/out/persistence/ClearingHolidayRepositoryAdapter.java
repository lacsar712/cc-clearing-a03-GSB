package com.clearing.netting.adapter.out.persistence;

import com.clearing.netting.adapter.out.persistence.repo.ClearingHolidayJpaRepository;
import com.clearing.netting.domain.model.ClearingHoliday;
import com.clearing.netting.domain.port.out.ClearingHolidayRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClearingHolidayRepositoryAdapter implements ClearingHolidayRepositoryPort {

    private final ClearingHolidayJpaRepository repository;

    public ClearingHolidayRepositoryAdapter(ClearingHolidayJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClearingHoliday save(ClearingHoliday holiday) {
        return PersistenceMapper.toDomain(repository.save(PersistenceMapper.toEntity(holiday)));
    }

    @Override
    public Optional<ClearingHoliday> findById(LocalDate holidayDate) {
        return repository.findById(holidayDate).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<ClearingHoliday> findAllOrderByDateAsc() {
        return repository.findAllByOrderByHolidayDateAsc().stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(LocalDate holidayDate) {
        return repository.existsById(holidayDate);
    }

    @Override
    public void deleteById(LocalDate holidayDate) {
        repository.deleteById(holidayDate);
    }
}
