package com.jaewa.timesheet.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ExportServiceTest {

    @Autowired
    ExportService exportService;

    @Test
    void export() throws IOException {
        exportService.export(2022, 9, 2L);
    }
}
