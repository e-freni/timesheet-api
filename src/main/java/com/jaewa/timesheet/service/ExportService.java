package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    public static final int INFO_PANEL_START_COLUMN = 35;
    public static final int INFO_PANEL_END_COLUMN = INFO_PANEL_START_COLUMN + 6;
    public static final int LAST_CELL_INDEX = 100;
    public static final int DAYS_START_COLUMN = 1;
    public static final int MORNING_HOUR_LABEL = DAYS_START_COLUMN + 1;
    public static final int DAYS_HEADER_FIRST_EMPTY_CELL = DAYS_START_COLUMN + 1;
    private static final int DAYS_HEADER_ROW = 3;

    private static final int MORNING_HOURS_ROW = 4;

    private static final int NIGHT_HOURS_ROW = 5;

    private static final int EXTRA_HOURS_ROW = 6;

    private static final int NOTES_ROW = 7;

    private Integer exportYear;
    private Integer exportMonth;
    private XSSFWorkbook workbook;
    private Sheet sheet;
    private final WorkdayService workdayService;
    private final ApplicationUserService applicationUserService;


    public ExportService(WorkdayService workdayService, ApplicationUserService applicationUserService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
    }

    public void export(Integer year, Integer month, Long userId) throws IOException {

        workbook = new XSSFWorkbook();

        this.exportYear = year;
        this.exportMonth = month;

        ApplicationUser user = applicationUserService.findById(userId);

        List<Workday> workdays = getWorkdays(user);


        //TODO find username by id

        sheet = workbook.createSheet(String.format("%s_%s_%s_%s", user.getFirstName(), user.getLastName(), this.exportMonth, this.exportYear));

        sheet.setColumnWidth(0, 300);

        Row infoPanelRow = sheet.createRow(1);
        infoPanelRow.setHeight((short) 1000);

//        infoPanelStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//        infoPanelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        writeInfoPanel(infoPanelRow);


//        infoPanelCell = infoPanelRow.createCell(2);
//        infoPanelCell.setCellValue("Age");
//        infoPanelCell.setCellStyle(infoPanelStyle);

        writeHeader(workdays);
        writeMorningHours(workdays);

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }

    private void writeMorningHours(List<Workday> workdays) {
        Row headerRow = sheet.createRow(MORNING_HOURS_ROW);
        Cell morningHourCell = headerRow.createCell(DAYS_START_COLUMN);
        morningHourCell.setCellStyle(getMorningHourLabelStyle(true));
        headerRow.setHeight((short) 500);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        morningHourCell.setCellValue(LocalDate.of(exportYear, exportMonth, 1).format(formatter));
        Cell morningHourLabelCell = headerRow.createCell(MORNING_HOUR_LABEL);
        morningHourLabelCell.setCellStyle(getMorningHourLabelStyle(false));
        morningHourLabelCell.setCellValue("Ore diurne");

        int daysInMonth = getDaysInMonth();


        boolean isPermitDay = false;

        for (int i = 1; i <= daysInMonth; i++) {
            if (isPermitDay) {
                isPermitDay = false;
                continue;
            }
            Cell cell = headerRow.createCell(MORNING_HOUR_LABEL + i);
            CellStyle hoursCellStyle = workbook.createCellStyle();
            hoursCellStyle.setAlignment(HorizontalAlignment.CENTER);
            hoursCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            hoursCellStyle.setBorderBottom(BorderStyle.THIN);
            hoursCellStyle.setBorderLeft(BorderStyle.THIN);
            hoursCellStyle.setBorderRight(BorderStyle.THIN);

            int currentIterationDay = i;
            Workday selectedDay = workdays.stream()
                    .filter(day -> day.getDate().equals(LocalDate.of(exportYear, exportMonth, currentIterationDay)))
                    .findAny().orElse(new Workday());

            if (selectedDay.isHoliday()) {
                cell.setCellValue(8);
                hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                hoursCellStyle.setFillForegroundColor( IndexedColors.LIGHT_GREEN.getIndex());
            }

            if (selectedDay.isSick()) {
                cell.setCellValue(8);
                hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                hoursCellStyle.setFillForegroundColor( IndexedColors.ROSE.getIndex());
            }

            if (selectedDay.isAccidentAtWork()) {
                cell.setCellValue("I");
            }

            if (selectedDay.getFuneralLeaveHours() > 0) {
                cell.setCellValue("PL");
                //FIXME funeral leave is treated as a boolean in the real model, but funeral leaves are fractional, so?
            }

            if (selectedDay.getWorkingHours() > 0) {
                cell.setCellValue(selectedDay.getWorkingHours());

            }

            if (selectedDay.getWorkPermitHours() > 0) {
                cell = headerRow.createCell(MORNING_HOUR_LABEL + i + 1);
                cell.setCellValue(selectedDay.getWorkPermitHours());
                hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                hoursCellStyle.setFillForegroundColor( IndexedColors.OLIVE_GREEN.getIndex());
                isPermitDay = true;
            }

            cell.setCellStyle(hoursCellStyle);


        }

    }


    private void writeHeader(List<Workday> workdays) {
        Row headerRow = sheet.createRow(DAYS_HEADER_ROW);
        Cell headerCell = headerRow.createCell(DAYS_START_COLUMN);
        headerCell.setCellStyle(getNameCellStyle());
        headerRow.setHeight((short) 500);


        for (int i = DAYS_START_COLUMN; i <= DAYS_START_COLUMN + 1; i++) {
            Cell namePanelCellComplementary = headerRow.createCell(i);
            namePanelCellComplementary.setCellStyle(getNameCellStyle());
        }


        headerCell.setCellValue("Nome");
        sheet.setColumnWidth(DAYS_START_COLUMN, 7000);
        headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL); //cella vuota prima dei giorni
        sheet.setColumnWidth(DAYS_HEADER_FIRST_EMPTY_CELL, 4000);
        headerCell.setCellStyle(getEmptyHeaderCellStyle());


        int daysInMonth = getDaysInMonth();

        for (int calendarDay = 1; calendarDay <= daysInMonth; calendarDay++) {

            int currentIterationDay = calendarDay;

            Workday selectedDay = workdays.stream()
                    .filter(day -> day.getDate().equals(LocalDate.of(exportYear, exportMonth, currentIterationDay)))
                    .findAny().orElse(new Workday());

            if (selectedDay.getWorkPermitHours() > 0) {
                sheet.addMergedRegion(new CellRangeAddress(DAYS_HEADER_ROW, DAYS_HEADER_ROW, DAYS_HEADER_FIRST_EMPTY_CELL + calendarDay, DAYS_HEADER_FIRST_EMPTY_CELL + + calendarDay + 1));
            }

            headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL + calendarDay);
            headerCell.setCellValue(calendarDay);
            headerCell.setCellStyle(getDayNumberHeaderCellStyle());
            sheet.setColumnWidth(DAYS_HEADER_FIRST_EMPTY_CELL + calendarDay, 1500);
        }

        int daysHeaderLastEmptyCell = DAYS_HEADER_FIRST_EMPTY_CELL + daysInMonth + 1;
        headerCell = headerRow.createCell(daysHeaderLastEmptyCell);
        headerCell.setCellStyle(getDayNumberHeaderCellStyle());
        sheet.setColumnWidth(daysHeaderLastEmptyCell, 200);

        Cell workingHoursTotalCell = createTotalHeader(headerRow, daysHeaderLastEmptyCell, "Feriale");
        Cell nonWorkingHoursTotalCell = createTotalHeader(headerRow, workingHoursTotalCell.getColumnIndex(), "Festivo", IndexedColors.LIGHT_YELLOW.getIndex());
        Cell holidaysHoursTotalCell = createTotalHeader(headerRow, nonWorkingHoursTotalCell.getColumnIndex(), "Ferie", IndexedColors.LIGHT_GREEN.getIndex());
        Cell sicknessHoursTotalCell = createTotalHeader(headerRow, holidaysHoursTotalCell.getColumnIndex(), "Malattia", IndexedColors.ROSE.getIndex());
        Cell totalHoursTotalCell = createTotalHeader(headerRow, sicknessHoursTotalCell.getColumnIndex(), "Totali");
        Cell aTotalCell = createTotalHeader(headerRow, totalHoursTotalCell.getColumnIndex(), "A");
        createTotalHeader(headerRow, aTotalCell.getColumnIndex(), "B");
    }

    private int getDaysInMonth() {
        return YearMonth.of(exportYear, exportMonth).lengthOfMonth();
    }


    private Cell createTotalHeader(Row headerRow, int referenceToPreviousCell, String title, Short... backgroundColor) {
        Cell cell;
        int totalCell = referenceToPreviousCell + 1;
        cell = headerRow.createCell(totalCell);
        cell.setCellValue(title);
        cell.setCellStyle(getDayNumberHeaderCellStyle(backgroundColor));
        sheet.setColumnWidth(totalCell, 2500);
        return cell;
    }

    private void writeInfoPanel(Row infoPanelRow) {
        for (int i = INFO_PANEL_START_COLUMN; i <= INFO_PANEL_END_COLUMN; i++) {
            Cell infoPanelCellComplementary = infoPanelRow.createCell(i);
            infoPanelCellComplementary.setCellStyle(getInfoPanelStyle());
        }

        Cell infoPanelCell = infoPanelRow.createCell(INFO_PANEL_START_COLUMN); //skip prima linea
        infoPanelRow.createCell(LAST_CELL_INDEX); //creo l'ultima cella per non avere tagli
        infoPanelCell.setCellValue("A= ore complessive B= ore lavorare senza ferie e straordinari");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, INFO_PANEL_START_COLUMN, INFO_PANEL_END_COLUMN));

        infoPanelCell.setCellStyle(getInfoPanelStyle());
    }

    private List<Workday> getWorkdays(ApplicationUser user) {
        LocalDate date = LocalDate.of(exportYear, exportMonth, 1);
        LocalDate from = date.withDayOfMonth(1);
        LocalDate to = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));

        return workdayService.findWorkdayByUser(user.getUsername(), from, to);
    }

    private CellStyle getInfoPanelStyle() {
        CellStyle infoPanelStyle = workbook.createCellStyle();
        infoPanelStyle.setWrapText(true);
        infoPanelStyle.setAlignment(HorizontalAlignment.CENTER);
        infoPanelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        infoPanelStyle.setBorderTop(BorderStyle.THIN);
        infoPanelStyle.setBorderBottom(BorderStyle.THIN);
        infoPanelStyle.setBorderLeft(BorderStyle.THIN);
        infoPanelStyle.setBorderRight(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        infoPanelStyle.setFont(font);

        return infoPanelStyle;
    }

    private CellStyle getNameCellStyle() {
        CellStyle nameCellStyle = workbook.createCellStyle();
        nameCellStyle.setWrapText(true);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        nameCellStyle.setFont(font);
        nameCellStyle.setAlignment(HorizontalAlignment.CENTER);
        nameCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        nameCellStyle.setBorderTop(BorderStyle.MEDIUM);
        nameCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        nameCellStyle.setBorderLeft(BorderStyle.MEDIUM);
        nameCellStyle.setBorderRight(BorderStyle.THIN);

        return nameCellStyle;
    }

    private CellStyle getEmptyHeaderCellStyle() {
        CellStyle emptyHeaderCellStyle = workbook.createCellStyle();
        emptyHeaderCellStyle.setBorderTop(BorderStyle.MEDIUM);
        emptyHeaderCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        emptyHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
        emptyHeaderCellStyle.setBorderRight(BorderStyle.THIN);

        return emptyHeaderCellStyle;
    }

    private CellStyle getDayNumberHeaderCellStyle(Short... backgroundColor) {
        CellStyle dayNumberHeaderStyle = workbook.createCellStyle();
        dayNumberHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        dayNumberHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dayNumberHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        dayNumberHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
        dayNumberHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        dayNumberHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);

        //XXX fake array to allow optional parameter
        if (backgroundColor.length > 0) {
            dayNumberHeaderStyle.setFillForegroundColor(backgroundColor[0]);
            dayNumberHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        dayNumberHeaderStyle.setFont(font);

        return dayNumberHeaderStyle;
    }

    private CellStyle getMorningHourLabelStyle(boolean bold) {
        CellStyle dateHeaderStyle = workbook.createCellStyle();
        dateHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        dateHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dateHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        if (bold) {
            font.setBold(true);
        }
        dateHeaderStyle.setFont(font);

        return dateHeaderStyle;
    }

}
