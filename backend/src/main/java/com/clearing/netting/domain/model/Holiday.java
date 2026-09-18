package com.clearing.netting.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A clearing calendar holiday. No netting run may target a holiday settleDate.
 * The date itself is the identity: one holiday entry per date.
 */
public class Holiday {

    private final LocalDate holidayDate;
    private String name;

    public Holiday(LocalDate holidayDate, String name) {
        this.holidayDate = Objects.requireNonNull(holidayDate, "holidayDate");
        this.name = Objects.requireNonNull(name, "name");
    }

    public static Holiday of(LocalDate holidayDate, String name) {
        String label = (name == null || name.isBlank()) ? "清算假日" : name.trim();
        return new Holiday(holidayDate, label);
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }
}
