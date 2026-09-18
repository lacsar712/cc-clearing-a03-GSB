package com.clearing.netting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 清算日历中的一个假日。目标交割日落在假日时，轧差必须被拒绝。
 */
public class ClearingHoliday {
    private final LocalDate holidayDate;
    private String name;

    public ClearingHoliday(LocalDate holidayDate, String name) {
        this.holidayDate = Objects.requireNonNull(holidayDate);
        this.name = name == null ? "" : name.trim();
    }

    public static ClearingHoliday of(LocalDate date, String name) {
        return new ClearingHoliday(date, name);
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name.trim();
    }
}
