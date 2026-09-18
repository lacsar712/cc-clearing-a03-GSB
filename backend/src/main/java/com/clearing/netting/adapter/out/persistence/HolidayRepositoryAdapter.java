package com.clearing.netting.adapter.out.persistence;

import com.clearing.netting.adapter.out.persistence.repo.HolidayJpaRepository;
import com.clearing.netting.domain.model.Holiday;
import com.clearing.netting.domain.port.out.HolidayRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HolidayRepositoryAdapter implements HolidayRepositoryPort {

    private final HolidayJpaRepository repository;

    public HolidayRepositoryAdapter(HolidayJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Holiday save(Holiday holiday) {
        return PersistenceMapper.toDomain(repository.save(PersistenceMapper.toEntity(holiday)));
    }

    @Override
    public List<Holiday> findAllOrderByDateAsc() {
        return repository.findAllByOrderByHolidayDateAsc().stream()
                .map(PersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        return repository.existsById(date);
    }

    @Override
    public void deleteByDate(LocalDate date) {
        repository.deleteById(date);
    }
}
