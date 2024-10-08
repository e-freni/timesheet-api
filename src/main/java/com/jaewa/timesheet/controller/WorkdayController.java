package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.SpecialDayDto;
import com.jaewa.timesheet.controller.dto.SummaryDto;
import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.controller.mapper.SpecialDayMapper;
import com.jaewa.timesheet.controller.mapper.SummaryMapper;
import com.jaewa.timesheet.controller.mapper.WorkdayMapper;
import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class WorkdayController {

    private final WorkdayService workdayService;
    private final ApplicationUserService applicationUserService;
    private final SummaryService summaryService;
    private final SpecialDayService specialDayService;
    private final WorkdayMapper workdayMapper;
    private final SummaryMapper summaryMapper;
    private final SpecialDayMapper specialDayMapper;

    public WorkdayController(WorkdayService workdayService, ApplicationUserService applicationUserService, SpecialDayService specialDayService, WorkdayMapper workdayMapper, SummaryService summaryService, SummaryMapper summaryMapper, SpecialDayMapper specialDayMapper) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
        this.specialDayService = specialDayService;
        this.workdayMapper = workdayMapper;
        this.summaryService = summaryService;
        this.summaryMapper = summaryMapper;
        this.specialDayMapper = specialDayMapper;
    }

    @GetMapping("/workday")
    public ResponseEntity<List<WorkdayDto>> getUserWorkDays(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "fromDate") String fromDate,
            @RequestParam(value = "toDate") String toDate
    ) throws UnauthorizedException {

        Optional<ApplicationUser> user = applicationUserService.getByUsername(username);

        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AuthorizationService.checkUserIsAuthorized(user.get().getId());

        return ResponseEntity.ok(
                workdayService.findWorkdayByUser(username, fromDate, toDate)
                        .stream()
                        .map(workdayMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @GetMapping("/workday/{id}")
    public ResponseEntity<WorkdayDto> getWorkDay(@PathVariable(value = "id") Long id) throws UnauthorizedException {
        Optional<Workday> workday = workdayService.findById(id);

        if (workday.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AuthorizationService.checkUserIsAuthorized(workday.get().getApplicationUser().getId());

        return ResponseEntity.ok(workdayMapper.toDto(workday.get()));
    }

    @PostMapping("/workday/new")
    public ResponseEntity<WorkdayDto> addNewWorkday(@RequestBody WorkdayDto dto) throws IncoherentDataException, UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(dto.getUserId());
        Workday result = workdayService.addNewWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @PutMapping("/workday/edit")
    public ResponseEntity<WorkdayDto> editWorkday(@RequestBody WorkdayDto dto) throws IncoherentDataException, UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(dto.getUserId());
        Workday result = workdayService.editWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @DeleteMapping("/workday/{userId}/delete/{workdayId}")
    public ResponseEntity<String> deleteWorkday(
            @PathVariable(value = "userId") Long userId, @PathVariable(value = "workdayId") Long workdayId) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(userId);
        workdayService.deleteWorkday(workdayId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/workday/{userId}/summary")
    public ResponseEntity<SummaryDto> workdaysSummary(
            @PathVariable(value = "userId") Long userId,
            @PathParam(value = "year") Integer year,
            @PathParam(value = "month") Integer month) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(userId);
        return ResponseEntity.ok(summaryMapper.toDto(summaryService.getSummaryData(year, month, userId)));
    }

    @GetMapping("/workday/special-days")
    public ResponseEntity<List<SpecialDayDto>> getSpecialDays(
            @PathParam(value = "year") Integer year,
            @PathParam(value = "month") Integer month) {
        return ResponseEntity.ok(specialDayMapper.toDtos(specialDayService.getSpecialDays(year, month)));
    }
}
