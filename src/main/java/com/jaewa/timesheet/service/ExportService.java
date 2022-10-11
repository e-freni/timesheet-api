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
    public static final int HOUR_LABEL = DAYS_START_COLUMN + 1;
    public static final int DAYS_HEADER_FIRST_EMPTY_CELL = DAYS_START_COLUMN + 1;
    private static final int DAYS_HEADER_ROW = 3;
    private static final int MORNING_HOURS_ROW = 4;
    private static final int NIGHT_HOURS_ROW = 5;
    private static final int EXTRA_HOURS_ROW = 6;
    private static final int NOTES_ROW = 7;
    private final WorkdayService workdayService;
    private final ApplicationUserService applicationUserService;
    private Integer exportYear;
    private Integer exportMonth;
    private XSSFWorkbook workbook;
    private Sheet sheet;


    public ExportService(WorkdayService workdayService, ApplicationUserService applicationUserService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
    }

    public void export(Integer year, Integer month, Long userId) throws IOException {

        workbook = new XSSFWorkbook();

        this.exportYear = year;
        this.exportMonth = month;

        ApplicationUser user = applicationUserService.findById(userId);


        //TODO find username by id

        sheet = workbook.createSheet(String.format("%s_%s_%s_%s", user.getFirstName(), user.getLastName(), this.exportMonth, this.exportYear));

        sheet.setColumnWidth(0, 300);

        Row infoPanelRow = sheet.createRow(1);
        infoPanelRow.setHeight((short) 1000);

        List<Workday> workdays = getWorkdays(user);
        writeInfoPanel(infoPanelRow);
        writeHeader(workdays);
        writeMorningHours(workdays);
        writeNightHours(workdays, user);
        writeExtraHours(workdays);
        writeNotes(workdays);
        writeLegend(workdays);

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
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

        int notEntirePermitDays = 0;

        for (int calendarDay = 1; calendarDay <= daysInMonth; calendarDay++) {

            int currentIterationDay = calendarDay;

            Workday selectedDay = workdays.stream()
                    .filter(day -> day.getDate().equals(LocalDate.of(exportYear, exportMonth, currentIterationDay)))
                    .findAny().orElse(new Workday());

            headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay);
            headerCell.setCellValue(calendarDay);
            headerCell.setCellStyle(getDayNumberHeaderCellStyle());

            if (selectedDay.getWorkPermitHours() > 0) {
                if (selectedDay.getWorkPermitHours() < 8) {
                    sheet.addMergedRegion(new CellRangeAddress(DAYS_HEADER_ROW, DAYS_HEADER_ROW, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay + 1));
                    notEntirePermitDays++;
                }
                headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL + calendarDay + 1);
                headerCell.setCellValue(calendarDay);
                headerCell.setCellStyle(getDayNumberHeaderCellStyle());
            }
        }

        for (int i = 1; i <= daysInMonth + notEntirePermitDays; i++) {
            sheet.setColumnWidth(DAYS_HEADER_FIRST_EMPTY_CELL + i, 1500);
        }

        int daysHeaderLastEmptyCell = writeSeparatorCell(headerRow, daysInMonth + notEntirePermitDays);

        Cell workingHoursTotalCell = createTotalCell(headerRow, daysHeaderLastEmptyCell, "Feriale");
        Cell nonWorkingHoursTotalCell = createTotalCell(headerRow, workingHoursTotalCell.getColumnIndex(), "Festivo", IndexedColors.LIGHT_YELLOW.getIndex());
        Cell holidaysHoursTotalCell = createTotalCell(headerRow, nonWorkingHoursTotalCell.getColumnIndex(), "Ferie", IndexedColors.LIGHT_GREEN.getIndex());
        Cell sicknessHoursTotalCell = createTotalCell(headerRow, holidaysHoursTotalCell.getColumnIndex(), "Malattia", IndexedColors.ROSE.getIndex());
        Cell totalHoursTotalCell = createTotalCell(headerRow, sicknessHoursTotalCell.getColumnIndex(), "Totali");
        Cell aTotalCell = createTotalCell(headerRow, totalHoursTotalCell.getColumnIndex(), "A");
        createTotalCell(headerRow, aTotalCell.getColumnIndex(), "B");
    }


    private void writeMorningHours(List<Workday> workdays) {
        Row hoursRow = sheet.createRow(MORNING_HOURS_ROW);
        Cell currentMonthDateCell = hoursRow.createCell(DAYS_START_COLUMN);
        currentMonthDateCell.setCellStyle(getHourLabelStyle(true));
        hoursRow.setHeight((short) 500);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        currentMonthDateCell.setCellValue(LocalDate.of(exportYear, exportMonth, 1).format(formatter));
        Cell morningHourLabelCell = hoursRow.createCell(HOUR_LABEL);
        morningHourLabelCell.setCellStyle(getHourLabelStyle(false));
        morningHourLabelCell.setCellValue("Ore diurne");

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            Cell cell = hoursRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
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

            addHoliday(cell, hoursCellStyle, selectedDay);

            addSickness(cell, hoursCellStyle, selectedDay);

            addAccidentAtWork(cell, selectedDay);

            addFuneralLeave(cell, selectedDay);

            addWorkingHours(cell, selectedDay);

            notEntirePermitDays = handlePermitDayHours(hoursRow, notEntirePermitDays, i, hoursCellStyle, selectedDay);

            cell.setCellStyle(hoursCellStyle);

        }

        int daysHeaderLastEmptyCell = writeSeparatorCell(hoursRow, daysInMonth + notEntirePermitDays);

        int totalWorkingHours = workdays.stream()
                .map(w -> w.getWorkingHours() + w.getWorkPermitHours())
                .mapToInt(w -> w).sum();

        Cell workingHoursTotalCell = createTotalCell(hoursRow, daysHeaderLastEmptyCell, totalWorkingHours);

        //TODO understand how nonWorkingDayHours are handled

        Cell nonWorkingHoursTotalCell = createTotalCell(hoursRow, workingHoursTotalCell.getColumnIndex(), "Cosa ci si mette qui?", IndexedColors.LIGHT_YELLOW.getIndex());

        int totalHolidayHours = workdays.stream()
                .map(w -> w.isHoliday() ? 8 : 0)
                .mapToInt(w -> w).sum();

        Cell holidaysHoursTotalCell = createTotalCell(hoursRow, nonWorkingHoursTotalCell.getColumnIndex(), totalHolidayHours, IndexedColors.LIGHT_GREEN.getIndex());

        int totalSicknessHours = workdays.stream()
                .map(w -> w.isSick() ? 8 : 0)
                .mapToInt(w -> w).sum();

        Cell sicknessHoursTotalCell = createTotalCell(hoursRow, holidaysHoursTotalCell.getColumnIndex(), totalSicknessHours, IndexedColors.ROSE.getIndex());

        int totalOfTotals = (int) Math.round(workingHoursTotalCell.getNumericCellValue() + holidaysHoursTotalCell.getNumericCellValue() + sicknessHoursTotalCell.getNumericCellValue());

        Cell totalHoursTotalCell = createTotalCell(hoursRow, sicknessHoursTotalCell.getColumnIndex(), totalOfTotals);

        int totalExtraHours = workdays.stream()
                .map(Workday::getExtraHours)
                .mapToInt(w -> w).sum();

        int totalA = (int) Math.round(totalHoursTotalCell.getNumericCellValue() + totalExtraHours);

        Cell totalACell = createTotalCell(hoursRow, totalHoursTotalCell.getColumnIndex(), totalA);

        int totalNightHours = workdays.stream()
                .map(Workday::getNightWorkingHours)
                .mapToInt(w -> w).sum();

        //TODO add calculation nonWorkingDayHours and nightNonWorkingDayHours

        int totalNonWorkingDayHours = 0;

        int totalNightNonWorkingDayHours = 0;

        int totalB = totalWorkingHours + totalNightHours + totalNonWorkingDayHours + totalNightNonWorkingDayHours;

        createTotalCell(hoursRow, totalACell.getColumnIndex(), totalB);

    }


    private void writeNightHours(List<Workday> workdays, ApplicationUser user) {
        Row hoursRow = sheet.createRow(NIGHT_HOURS_ROW);
        Cell userFullNameCell = hoursRow.createCell(DAYS_START_COLUMN);
        userFullNameCell.setCellStyle(getHourLabelStyle(true));
        hoursRow.setHeight((short) 500);

        userFullNameCell.setCellValue(String.format("%s %s", user.getFirstName(), user.getLastName()));
        sheet.addMergedRegion(new CellRangeAddress(NIGHT_HOURS_ROW, NOTES_ROW, DAYS_START_COLUMN, DAYS_START_COLUMN));
        Cell nightHourLabelCell = hoursRow.createCell(HOUR_LABEL);
        nightHourLabelCell.setCellStyle(getHourLabelStyle(false));
        nightHourLabelCell.setCellValue("Ore notturne");

        //TODO write nightWorkingHours
    }

    private void writeExtraHours(List<Workday> workdays) {
        Row hoursRow = sheet.createRow(EXTRA_HOURS_ROW);
        Cell emptyCell = hoursRow.createCell(DAYS_START_COLUMN);
        emptyCell.setCellStyle(getHourLabelStyle(true));
        hoursRow.setHeight((short) 500);

        Cell nightHourLabelCell = hoursRow.createCell(HOUR_LABEL);
        nightHourLabelCell.setCellStyle(getHourLabelStyle(false));
        nightHourLabelCell.setCellValue("Straordinario");

        //TODO write extra hours

    }

    private void writeNotes(List<Workday> workdays) {
        Row notesRow = sheet.createRow(NOTES_ROW);
        Cell emptyCell = notesRow.createCell(DAYS_START_COLUMN);
        emptyCell.setCellStyle(getNotesLabelStyle(true));
        notesRow.setHeight((short) 500);

        Cell nightHourLabelCell = notesRow.createCell(HOUR_LABEL);
        nightHourLabelCell.setCellStyle(getNotesLabelStyle(false));
        nightHourLabelCell.setCellValue("Note");

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            Cell cell = notesRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
            CellStyle notesCellStyle = workbook.createCellStyle();
            notesCellStyle.setAlignment(HorizontalAlignment.CENTER);
            notesCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            notesCellStyle.setBorderTop(BorderStyle.THIN);
            notesCellStyle.setBorderLeft(BorderStyle.THIN);
            notesCellStyle.setBorderRight(BorderStyle.THIN);
            notesCellStyle.setBorderBottom(BorderStyle.MEDIUM);

            int currentIterationDay = i;
            Workday selectedDay = workdays.stream()
                    .filter(day -> day.getDate().equals(LocalDate.of(exportYear, exportMonth, currentIterationDay)))
                    .findAny().orElse(new Workday());

            if (selectedDay.getWorkPermitHours() > 0 && selectedDay.getWorkPermitHours() < 8) {
                sheet.addMergedRegion(new CellRangeAddress(NOTES_ROW, NOTES_ROW, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + i, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + i + 1));
                Cell nextCell = notesRow.createCell(HOUR_LABEL + i + 1 + notEntirePermitDays);

                nextCell.setCellValue(selectedDay.getWorkPermitHours());
                nextCell.setCellStyle(notesCellStyle);
                notEntirePermitDays++;
            }

            if (selectedDay.getDate() != null) {
                //TODO resize cell to make notes text fit into
            }
            cell.setCellStyle(notesCellStyle);
            cell.setCellValue(selectedDay.getNotes());

        }
    }

    private void writeLegend(List<Workday> workdays) {
        //TODO structure legend
    }

    private int writeSeparatorCell(Row headerRow, int daysInMonth) {
        Cell headerCell;
        int daysHeaderLastEmptyCell = DAYS_HEADER_FIRST_EMPTY_CELL + daysInMonth + 1;
        headerCell = headerRow.createCell(daysHeaderLastEmptyCell);
        headerCell.setCellStyle(getDayNumberHeaderCellStyle());
        sheet.setColumnWidth(daysHeaderLastEmptyCell, 200);
        return daysHeaderLastEmptyCell;
    }


    private void addWorkingHours(Cell cell, Workday selectedDay) {
        if (selectedDay.getWorkingHours() > 0) {
            cell.setCellValue(selectedDay.getWorkingHours());
        }
    }

    private void addFuneralLeave(Cell cell, Workday selectedDay) {
        if (selectedDay.getFuneralLeaveHours() > 0) {
            cell.setCellValue("PL");
            //FIXME funeral leave is treated as a boolean in the real model, but funeral leaves are fractional, so?
        }
    }

    private void addAccidentAtWork(Cell cell, Workday selectedDay) {
        if (selectedDay.isAccidentAtWork()) {
            cell.setCellValue("I");
        }
    }

    private void addSickness(Cell cell, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.isSick()) {
            cell.setCellValue(8);
            hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hoursCellStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        }
    }

    private void addHoliday(Cell cell, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.isHoliday()) {
            cell.setCellValue(8);
            hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hoursCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        }
    }

    private int getDaysInMonth() {
        return YearMonth.of(exportYear, exportMonth).lengthOfMonth();
    }


    private Cell createTotalCell(Row row, int referenceToPreviousCell, String title, Short... backgroundColor) {
        int totalCell = referenceToPreviousCell + 1;
        Cell cell = row.createCell(totalCell);
        cell.setCellValue(title);
        cell.setCellStyle(getDayNumberHeaderCellStyle(backgroundColor));
        sheet.setColumnWidth(totalCell, 2500);
        return cell;
    }

    private Cell createTotalCell(Row row, int referenceToPreviousCell, int hours, Short... backgroundColor) {
        int totalCell = referenceToPreviousCell + 1;
        Cell cell = row.createCell(totalCell);
        cell.setCellValue(hours);
        cell.setCellStyle(getDayNumberHeaderCellStyle(backgroundColor));
        sheet.setColumnWidth(totalCell, 2500);
        return cell;
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
        setHardSquareStyle(dayNumberHeaderStyle);
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

    private CellStyle getHourLabelStyle(boolean bold) {
        CellStyle dateHeaderStyle = workbook.createCellStyle();
        dateHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        dateHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dateHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
        setCustomFont(bold, dateHeaderStyle);

        return dateHeaderStyle;
    }

    private void setCustomFont(boolean bold, CellStyle dateHeaderStyle) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        if (bold) {
            font.setBold(true);
        }
        dateHeaderStyle.setFont(font);
    }

    private CellStyle getNotesLabelStyle(boolean bold) {
        CellStyle notesHeaderStyle = workbook.createCellStyle();
        setHardSquareStyle(notesHeaderStyle);
        setCustomFont(bold, notesHeaderStyle);

        return notesHeaderStyle;
    }

    private void setHardSquareStyle(CellStyle notesHeaderStyle) {
        notesHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        notesHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        notesHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
    }

    private int handlePermitDayHours(Row hoursRow, int notEntirePermitDays, int i, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.getWorkPermitHours() > 0) {
            Cell nextCell;
            if (selectedDay.getWorkPermitHours() < 8) {
                nextCell = hoursRow.createCell(HOUR_LABEL + i + 1 + notEntirePermitDays);
                notEntirePermitDays++;
            } else {
                nextCell = hoursRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
            }
            nextCell.setCellValue(selectedDay.getWorkPermitHours());
            CellStyle nextCellStyle = workbook.createCellStyle();
            nextCellStyle.cloneStyleFrom(hoursCellStyle);
            nextCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            nextCellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            nextCell.setCellStyle(nextCellStyle);
        }
        return notEntirePermitDays;
    }

}

