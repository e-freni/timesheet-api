package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.controller.mapper.WorkdayMapper;
import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.AuthorizationService;
import com.jaewa.timesheet.service.ExportService;
import com.jaewa.timesheet.service.WorkdayService;
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

    private final ExportService exportService;

    private final WorkdayMapper workdayMapper;

    public WorkdayController(WorkdayService workdayService, ApplicationUserService applicationUserService, WorkdayMapper workdayMapper, ExportService exportService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
        this.exportService = exportService;
        this.workdayMapper = workdayMapper;
    }

    @GetMapping("/workday")
    public ResponseEntity<List<WorkdayDto>> getUserWorkDays(
            @RequestParam(value = "username") String username,
            @RequestParam(value = "fromDate") String fromDate ,
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

    @GetMapping("/workday/{userId}/export")
    public ResponseEntity<String> exportMonthWorkdays(
            @PathVariable(value = "userId") Long userId,
            @PathParam(value = "year") Integer year,
            @PathParam(value = "month") Integer month) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(userId);
        exportService.export(year, month, userId);
        return ResponseEntity.ok().build();
    }
}
