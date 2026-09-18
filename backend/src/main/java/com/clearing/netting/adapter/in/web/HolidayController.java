package com.clearing.netting.adapter.in.web;

import com.clearing.netting.adapter.in.web.auth.AuthContext;
import com.clearing.netting.application.HolidayApplicationService;
import com.clearing.netting.domain.model.Holiday;
import jakarta.validation.Valid;
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
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayApplicationService holidayService;

    public HolidayController(HolidayApplicationService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    public List<HolidayResponse> list() {
        AuthContext.require();
        return holidayService.listHolidays().stream()
                .map(HolidayResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    public HolidayResponse add(@Valid @RequestBody AddHolidayRequest request) {
        AuthContext.requireOperator();
        return HolidayResponse.from(holidayService.addHoliday(request.holidayDate(), request.name()));
    }

    @DeleteMapping("/{date}")
    public void delete(@PathVariable("date") LocalDate date) {
        AuthContext.requireOperator();
        holidayService.removeHoliday(date);
    }

    public record AddHolidayRequest(@NotNull LocalDate holidayDate, String name) {
    }

    public record HolidayResponse(LocalDate holidayDate, String name) {
        static HolidayResponse from(Holiday h) {
            return new HolidayResponse(h.getHolidayDate(), h.getName());
        }
    }
}
