package com.jaewa.timesheet.controller;

import com.jaewa.timesheet.exception.UnauthorizedException;
import com.jaewa.timesheet.service.AuthorizationService;
import com.jaewa.timesheet.service.ExportService;
import com.jaewa.timesheet.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class ExportController {
    private final ExportService exportService;
    private final MailService mailService;

    public ExportController(ExportService exportService, MailService mailService) {
        this.exportService = exportService;
        this.mailService = mailService;
    }

    @GetMapping("/export/{userId}/excel")
    public ResponseEntity<byte[]> exportMonthWorkdays(
            @PathVariable(value = "userId") Long userId,
            @PathParam(value = "year") Integer year,
            @PathParam(value = "month") Integer month) throws UnauthorizedException, IOException {
        AuthorizationService.checkUserIsAuthorized(userId);
        return ResponseEntity.ok(exportService.export(year, month, userId));
    }

    @PostMapping("/export/{userId}/email")
    public ResponseEntity<String> exportAndSendByEmail(
            @PathVariable(value = "userId") Long userId,
            @PathParam(value = "year") Integer year,
            @PathParam(value = "month") Integer month,
            @RequestBody List<String> recipients
    ) throws UnauthorizedException, IOException, MailSendException {
        AuthorizationService.checkUserIsAuthorized(userId);
        File file = this.exportService.exportToTempFile(year, month, userId);
        String[] recipientsArray = new String[recipients.size()];
        this.mailService.sendWorkdaysExportByEmail(recipients.toArray(recipientsArray), userId, month, year, file);
        this.exportService.deleteTempFile();
        return ResponseEntity.ok().build();
    }

}
