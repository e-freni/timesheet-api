package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.controller.mapper.WorkdayMapper;
import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.WorkdayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<WorkdayDto>> getUserWorkDays(@RequestParam(value = "username") String username
    ) {
        //TODO only admin or owner user can access data
        return ResponseEntity.ok(
                workdayService.findWorkdayByUser(username).stream()
                        .map(workdayMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/workday/{id}")
    public ResponseEntity<WorkdayDto> getWorkDay(@PathVariable(value = "id") Long id) {
        //TODO only admin or owner user can access data
        Optional<Workday> result = workdayService.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(workdayMapper.toDto(result.get()));
    }

    @PostMapping("/workday/new")
    public ResponseEntity<WorkdayDto> addNewWorkday(@RequestBody WorkdayDto dto) throws IncoherentDataException {
        //TODO only admin or owner user can add data
        Workday result = workdayService.addNewWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @PutMapping("/workday/edit")
    public ResponseEntity<WorkdayDto> editWorkday(@RequestBody WorkdayDto dto) throws IncoherentDataException {
        //TODO only admin or owner user can add data
        Workday result = workdayService.editWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @DeleteMapping("/workday/delete")
    public ResponseEntity<String> deleteWorkday(@RequestBody WorkdayDto dto) {
        //TODO only admin or owner user can add data
        workdayService.deleteWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok().build();
    }
}
