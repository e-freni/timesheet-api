package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.controller.mapper.WorkdayMapper;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.WorkdayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class WorkdayController {

    private final WorkdayService workdayService;

    private final WorkdayMapper workdayMapper;

    public WorkdayController(WorkdayService workdayService, WorkdayMapper workdayMapper) {
        this.workdayService = workdayService;
        this.workdayMapper = workdayMapper;
    }

    @GetMapping("/workday")
    public ResponseEntity<List<WorkdayDto>> getUserWorkDays(
            @RequestParam(value = "username") String username
    ) {

        return ResponseEntity.ok(
                workdayService.findWorkdayByUser(username).stream()
                        .map(workdayMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/workday/{id}")
    public ResponseEntity<WorkdayDto> getWorkDay(
            @PathVariable(value = "id") Long id) {
        Optional<Workday> result = workdayService.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(workdayMapper.toDto(result.get()));
    }
}
