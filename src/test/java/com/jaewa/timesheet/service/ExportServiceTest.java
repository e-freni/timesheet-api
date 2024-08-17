package com.jaewa.timesheet.service;

import com.jaewa.timesheet.AbstractIntegrationTest;
import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.repository.ApplicationUserRepository;
import com.jaewa.timesheet.model.repository.WorkdayRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import static com.jaewa.timesheet.model.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExportServiceTest extends AbstractIntegrationTest {

    public static final int MONDAY = 3; // 3 is monday on January
    public static final int TUESDAY = 4;
    public static final int WEDNESDAY = 5;
    public static final int FIRST_SHEET = 0;
    public static final int HEADER_ROW = 3;
    public static final int MORNING_HOURS_ROW = 4;

    @Autowired
    ExportService exportService;

    @Autowired
    WorkdayRepository workdayRepository;

    @Autowired
    ApplicationUserRepository applicationUserRepository;


    ApplicationUser u1;

    private void setup() {
        u1 = ApplicationUser.builder()
                .username("baka")
                .email("baka@gmail.com")
                .firstName("Mario")
                .lastName("Rossi")
                .password("$2a$10$zdn7CgQp6TRKIuWvPv6vdOAf7RyJPTxnyDntFqunM2s")
                .role(USER)
                .build();

        this.applicationUserRepository.save(u1);
    }

    @Test
    @Transactional
    void testWorkdaysLength() throws IOException {
        setup();

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
        export = exportService.export(2022, 2, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("28.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(30).getRawValue());
        export = exportService.export(2022, 3, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("30.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
        export = exportService.export(2022, 4, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("30.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
        export = exportService.export(2022, 5, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
        export = exportService.export(2022, 6, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("30.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
        export = exportService.export(2022, 7, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
        export = exportService.export(2022, 8, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
        export = exportService.export(2022, 9, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("30.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
        export = exportService.export(2022, 10, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
        export = exportService.export(2022, 11, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("30.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
        export = exportService.export(2022, 12, u1.getId());
        workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("31.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(33).getRawValue());
    }

    @Test
    @Transactional
    void testWorkdaysLengthForLeapYear() throws IOException {
        setup();

        byte[] export = exportService.export(2020, 2, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("1.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(3).getRawValue());
        assertEquals("29.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(31).getRawValue());
        assertNull(workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(32).getRawValue());
    }

    @Test
    @Transactional
    void testNormalWorkingHoursMonth() throws IOException {
        setup();

        for (int i = 1; i <= 31; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .accidentAtWork(false)
                    .sick(false)
                    .holiday(false)
                    .workingHours(8)
                    .extraHours(0)
                    .workPermitHours(0)
                    .nightWorkingHours(0)
                    .funeralLeave(false)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertNull(workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(2).getRawValue());

        for (int i = 1; i <= 31; i++) {
            assertEquals(String.valueOf(Double.valueOf(i)), workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(i + 2).getRawValue());
        }

        assertNull(workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(34).getRawValue());
    }

    @Test
    @Transactional
    void testWorkPermitHoursStructure() throws IOException {
        setup();

        Workday fractionalPermitWorkday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(6)
                .extraHours(0)
                .workPermitHours(2)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, MONDAY))
                .build();
        this.workdayRepository.save(fractionalPermitWorkday);

        Workday anotherFractionalPermitWorkday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(5)
                .extraHours(0)
                .workPermitHours(3)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, TUESDAY))
                .build();
        this.workdayRepository.save(anotherFractionalPermitWorkday);

        Workday entirePermitWorkday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(8)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, WEDNESDAY))
                .build();
        this.workdayRepository.save(entirePermitWorkday);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("3.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(MONDAY + 2).getRawValue());
        assertEquals(String.valueOf(Double.valueOf(fractionalPermitWorkday.getWorkingHours())), workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getRawValue());

        CellStyle normalHourStyle = workbook.createCellStyle();
        normalHourStyle.setAlignment(HorizontalAlignment.CENTER);
        normalHourStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        normalHourStyle.setBorderTop(BorderStyle.THIN);
        normalHourStyle.setBorderBottom(BorderStyle.THIN);
        normalHourStyle.setBorderLeft(BorderStyle.THIN);
        normalHourStyle.setBorderRight(BorderStyle.THIN);

        assertEquals(normalHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getCellStyle());

        assertEquals("3.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(MONDAY + 3).getRawValue());
        assertEquals(String.valueOf(Double.valueOf(fractionalPermitWorkday.getWorkPermitHours())), workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 3).getRawValue());

        CellStyle fractionalWorkPermitHourStyle = workbook.createCellStyle();
        fractionalWorkPermitHourStyle.cloneStyleFrom(normalHourStyle);
        fractionalWorkPermitHourStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        fractionalWorkPermitHourStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        assertEquals(fractionalWorkPermitHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 3).getCellStyle());

        assertEquals("4.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(TUESDAY + 2 + 1).getRawValue());
        assertEquals("4.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(TUESDAY + 3 + 1).getRawValue());
        assertEquals(String.valueOf(Double.valueOf(anotherFractionalPermitWorkday.getWorkingHours())), workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(TUESDAY + 2 + 1).getRawValue());
        assertEquals(normalHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(TUESDAY + 2 + 1).getCellStyle());
        assertEquals(String.valueOf(Double.valueOf(anotherFractionalPermitWorkday.getWorkPermitHours())), workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(TUESDAY + 3 + 1).getRawValue());
        assertEquals(fractionalWorkPermitHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(TUESDAY + 3 + 1).getCellStyle());

        assertEquals("5.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(WEDNESDAY + 2 + 2).getRawValue());
        assertEquals("6.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(WEDNESDAY + 3 + 2).getRawValue());
        assertEquals(String.valueOf(Double.valueOf(entirePermitWorkday.getWorkPermitHours())), workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(WEDNESDAY + 2 + 2).getRawValue());
        assertEquals(fractionalWorkPermitHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(WEDNESDAY + 2 + 2).getCellStyle());

    }

    @Test
    @Transactional
    void testSickHoursStructure() throws IOException {
        setup();

        Workday sickDay = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(true)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, MONDAY))
                .build();
        this.workdayRepository.save(sickDay);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("3.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(MONDAY + 2).getRawValue());
        assertEquals("8.0", workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getRawValue());

        CellStyle sickHourStyle = workbook.createCellStyle();
        sickHourStyle.setAlignment(HorizontalAlignment.CENTER);
        sickHourStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        sickHourStyle.setBorderTop(BorderStyle.THIN);
        sickHourStyle.setBorderBottom(BorderStyle.THIN);
        sickHourStyle.setBorderLeft(BorderStyle.THIN);
        sickHourStyle.setBorderRight(BorderStyle.THIN);
        sickHourStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sickHourStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());

        assertEquals(sickHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getCellStyle());
    }

    @Test
    @Transactional
    void testHolidayHoursStructure() throws IOException {
        setup();

        Workday holiday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(false)
                .sick(false)
                .holiday(true)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, MONDAY))
                .build();
        this.workdayRepository.save(holiday);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("3.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(MONDAY + 2).getRawValue());
        assertEquals("8.0", workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getRawValue());

        CellStyle sickHourStyle = workbook.createCellStyle();
        sickHourStyle.setAlignment(HorizontalAlignment.CENTER);
        sickHourStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        sickHourStyle.setBorderTop(BorderStyle.THIN);
        sickHourStyle.setBorderBottom(BorderStyle.THIN);
        sickHourStyle.setBorderLeft(BorderStyle.THIN);
        sickHourStyle.setBorderRight(BorderStyle.THIN);
        sickHourStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        sickHourStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());

        assertEquals(sickHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getCellStyle());
    }

    @Test
    @Transactional
    void testAccidentAtWorkHoursStructure() throws IOException {
        setup();

        Workday accidentAtWorkday = Workday.builder()
                .applicationUser(u1)
                .accidentAtWork(true)
                .sick(false)
                .holiday(false)
                .workingHours(0)
                .extraHours(0)
                .workPermitHours(0)
                .nightWorkingHours(0)
                .funeralLeave(false)
                .date(LocalDate.of(2022, 1, MONDAY))
                .build();
        this.workdayRepository.save(accidentAtWorkday);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        assertEquals("3.0", workbook.getSheetAt(FIRST_SHEET).getRow(HEADER_ROW).getCell(MONDAY + 2).getRawValue());
        assertEquals("I", workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getStringCellValue());

        CellStyle sickHourStyle = workbook.createCellStyle();
        sickHourStyle.setAlignment(HorizontalAlignment.CENTER);
        sickHourStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        sickHourStyle.setBorderTop(BorderStyle.THIN);
        sickHourStyle.setBorderBottom(BorderStyle.THIN);
        sickHourStyle.setBorderLeft(BorderStyle.THIN);
        sickHourStyle.setBorderRight(BorderStyle.THIN);

        assertEquals(sickHourStyle, workbook.getSheetAt(FIRST_SHEET).getRow(MORNING_HOURS_ROW).getCell(MONDAY + 2).getCellStyle());
    }

    @Test
    @Transactional
    void testExportToTempFileAndDeletion() throws IOException {
        setup();

        File tempFile = exportService.exportToTempFile(2022, 1, u1.getId());
        Assertions.assertTrue(tempFile.exists());
        Assertions.assertTrue(tempFile.isFile());

        exportService.deleteTempFile();
        Assertions.assertFalse(tempFile.exists());
    }

    @Test
    @Transactional
    void testWriteInfoPanel() throws IOException {
        setup();

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row infoPanelRow = sheet.getRow(1);
        Cell infoPanelCell = infoPanelRow.getCell(ExportService.INFO_PANEL_START_COLUMN);
        assertEquals("A= ore complessive B= ore lavorate senza ferie e straordinari", infoPanelCell.getStringCellValue());

        workbook.close();
    }

    @Test
    @Transactional
    void testWriteHeader() throws IOException {
        setup();

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(ExportService.DAYS_HEADER_ROW);
        assertEquals("Nome", headerRow.getCell(ExportService.DAYS_START_COLUMN).getStringCellValue());

        for (int i = 1; i <= 31; i++) {
            Cell dayCell = headerRow.getCell(ExportService.DAYS_HEADER_FIRST_EMPTY_CELL + i);
            assertEquals(i, dayCell.getNumericCellValue());
        }

        workbook.close();
    }

    @Test
    @Transactional
    void testWriteHours() throws IOException {
        setup();

        for (int i = 1; i <= 5; i++) {
            Workday workday = Workday.builder()
                    .applicationUser(u1)
                    .workingHours(8)
                    .nightWorkingHours(2)
                    .extraHours(1)
                    .date(LocalDate.of(2022, 1, i))
                    .build();
            this.workdayRepository.save(workday);
        }

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row morningHoursRow = sheet.getRow(ExportService.MORNING_HOURS_ROW);
        for (int i = 1; i <= 5; i++) {
            Cell morningCell = morningHoursRow.getCell(ExportService.HOUR_LABEL + i);
            assertEquals(8.0, morningCell.getNumericCellValue());
        }

        Row nightHoursRow = sheet.getRow(ExportService.NIGHT_HOURS_ROW);
        for (int i = 1; i <= 5; i++) {
            Cell nightCell = nightHoursRow.getCell(ExportService.HOUR_LABEL + i);
            assertEquals(2.0, nightCell.getNumericCellValue());
        }

        Row extraHoursRow = sheet.getRow(ExportService.EXTRA_HOURS_ROW);
        for (int i = 1; i <= 5; i++) {
            Cell extraCell = extraHoursRow.getCell(ExportService.HOUR_LABEL + i);
            assertEquals(1.0, extraCell.getNumericCellValue());
        }

        workbook.close();
    }

    @Test
    @Transactional
    void testHandleMergedCellsAndPermitHours() throws IOException {
        setup();

        Workday permitDay = Workday.builder()
                .applicationUser(u1)
                .workingHours(6)
                .workPermitHours(2)
                .date(LocalDate.of(2022, 1, 3))
                .build();
        this.workdayRepository.save(permitDay);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row morningHoursRow = sheet.getRow(ExportService.MORNING_HOURS_ROW);

        Cell permitCell = morningHoursRow.getCell(ExportService.HOUR_LABEL + 3);
        assertEquals(6.0, permitCell.getNumericCellValue());

        Cell nextCell = morningHoursRow.getCell(ExportService.HOUR_LABEL + 4);
        assertEquals(2.0, nextCell.getNumericCellValue());

        workbook.close();
    }

    @Test
    @Transactional
    void testWriteLegend() throws IOException {
        setup();

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row legendFirstRow = sheet.getRow(ExportService.LEGEND_FIRST_ROW);
        Cell legendFirstCell = legendFirstRow.getCell(ExportService.LEGEND_FIRST_COLUMN);
        assertEquals("8", legendFirstCell.getStringCellValue());

        Cell legendDescriptionCell = legendFirstRow.getCell(ExportService.LEGEND_FIRST_COLUMN + 2);
        assertEquals("Malattia", legendDescriptionCell.getStringCellValue());

        workbook.close();
    }

    @Test
    @Transactional
    void testTotalNonWorkingDayHours() throws IOException {
        setup();

        Workday holiday = Workday.builder()
                .applicationUser(u1)
                .holiday(true)
                .date(LocalDate.of(2022, 1, 6)) // Epifania
                .build();
        this.workdayRepository.save(holiday);

        byte[] export = exportService.export(2022, 1, u1.getId());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(export));
        Sheet sheet = workbook.getSheetAt(0);

        Row morningHoursRow = sheet.getRow(ExportService.MORNING_HOURS_ROW);
        Cell holidayCell = morningHoursRow.getCell(ExportService.HOUR_LABEL + 6);
        assertEquals(8.0, holidayCell.getNumericCellValue());

        workbook.close();
    }

}
