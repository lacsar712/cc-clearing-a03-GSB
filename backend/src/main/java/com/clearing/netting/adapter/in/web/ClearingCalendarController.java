package com.clearing.netting.adapter.in.web;

import com.clearing.netting.adapter.in.web.auth.AuthContext;
import com.clearing.netting.application.ClearingCalendarApplicationService;
import com.clearing.netting.domain.model.ClearingHoliday;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clearing-calendar")
public class ClearingCalendarController {

    private final ClearingCalendarApplicationService calendarService;

    public ClearingCalendarController(ClearingCalendarApplicationService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/holidays")
    public List<HolidayResponse> listHolidays() {
        AuthContext.require();
        return calendarService.listHolidays().stream().map(HolidayResponse::from).collect(Collectors.toList());
    }

    @PostMapping("/holidays")
    public HolidayResponse addHoliday(@Valid @RequestBody AddHolidayRequest request) {
        AuthContext.requireOperator();
        return HolidayResponse.from(calendarService.addHoliday(request.date(), request.name()));
    }

    @DeleteMapping("/holidays/{date}")
    public void removeHoliday(@PathVariable("date") LocalDate date) {
        AuthContext.requireOperator();
        calendarService.removeHoliday(date);
    }

    public record AddHolidayRequest(@NotNull LocalDate date, @NotBlank String name) {
    }

    public record HolidayResponse(LocalDate date, String name) {
        static HolidayResponse from(ClearingHoliday h) {
            return new HolidayResponse(h.getHolidayDate(), h.getName());
        }
    }
}
