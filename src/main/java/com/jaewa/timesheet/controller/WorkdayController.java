package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.controller.dto.WorkdayDto;
import com.jaewa.timesheet.controller.mapper.WorkdayMapper;
import com.jaewa.timesheet.exception.IncoherentDataException;
import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.service.ApplicationUserService;
import com.jaewa.timesheet.service.AuthorizationService;
import com.jaewa.timesheet.service.WorkdayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class WorkdayController {

    private final WorkdayService workdayService;

    private final ApplicationUserService applicationUserService;

    private final WorkdayMapper workdayMapper;

    public WorkdayController(WorkdayService workdayService, ApplicationUserService applicationUserService, WorkdayMapper workdayMapper) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
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
        AuthorizationService.checkUserIsAuthorized(dto.getUsernameId());
        Workday result = workdayService.addNewWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @PutMapping("/workday/edit")
    public ResponseEntity<WorkdayDto> editWorkday(@RequestBody WorkdayDto dto) throws IncoherentDataException, UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(dto.getUsernameId());
        Workday result = workdayService.editWorkday(workdayMapper.toModel(dto));
        return ResponseEntity.ok(workdayMapper.toDto(result));
    }

    @DeleteMapping("/workday/{usernameId}/delete/{workdayId}")
    public ResponseEntity<String> deleteWorkday(
            @PathVariable(value = "usernameId") Long usernameId, @PathVariable(value = "workdayId") Long workdayId) throws UnauthorizedException {
        AuthorizationService.checkUserIsAuthorized(usernameId);
        workdayService.deleteWorkday(workdayId);
        return ResponseEntity.ok().build();
    }
}
