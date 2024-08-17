package com.jaewa.timesheet.service;

import com.jaewa.timesheet.model.ApplicationUser;
import com.jaewa.timesheet.model.Workday;
import com.jaewa.timesheet.model.specialday.EasterUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.jaewa.timesheet.model.specialday.FixedSpecialDay.*;

@Log4j2
@Service
public class ExportService {

    public static final int INFO_PANEL_START_COLUMN = 35;
    public static final int INFO_PANEL_END_COLUMN = INFO_PANEL_START_COLUMN + 6;
    public static final int LAST_CELL_INDEX = 100;
    public static final int DAYS_START_COLUMN = 1;
    public static final int HOUR_LABEL = DAYS_START_COLUMN + 1;
    public static final int DAYS_HEADER_FIRST_EMPTY_CELL = DAYS_START_COLUMN + 1;
    public static final int DAYS_HEADER_ROW = 3;
    public static final int MORNING_HOURS_ROW = 4;
    public static final int NIGHT_HOURS_ROW = 5;
    public static final int EXTRA_HOURS_ROW = 6;
    public static final int NOTES_ROW = 7;
    public static final int LEGEND_FIRST_ROW = 11;
    public static final int LEGEND_SECOND_ROW = 13;
    public static final int LEGEND_FIRST_COLUMN = 3;
    public static final int LEGEND_MIDDLE_COLUMN = 11;
    public static final int LEGEND_LAST_COLUMN = 19;
    public static final int COLUMN_WIDTH = 300;
    public static final int PANEL_HEIGHT = 1000;
    public static final int HEADER_ROW_HEIGHT = 500;
    public static final int DAYS_START_COLUMN_WIDTH = 7000;
    public static final int DAYS_HEADER_FIRST_EMPTY_CELL_WIDTH = 4000;
    public static final int MAX_WORKING_HOURS = 8;
    public static final int DAYS_HEADER_FIRST_EMPTY_CELL_COLUMN_WIDTH = 1500;
    public static final int DAYS_HEADER_LAST_EMPTY_CELL_COLUMN_WIDTH = 200;
    public static final int SHEET_TOTAL_COLUMN_WIDTH = 2500;
    public static final int INFO_PANEL_FONT_SIZE = 16;
    public static final int NAME_CELL_FONT_SIZE = 14;
    public static final int DAY_NUMBER_HEADER_FONT_SIZE = 11;
    public static final int CUSTOM_FONT_SIZE = 12;
    public static final int RED_FONT_SIZE = 10;
    public static final int TWO_COLUMN_AFTER_CURRENT = 2;
    public static final int THREE_NEXT_REGIONS = 3;

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

    public File exportToTempFile(Integer year, Integer month, Long userId) throws IOException {
        File tempFile = new File("temp.xlsx");
        FileUtils.writeByteArrayToFile(tempFile, export(year, month, userId));
        return tempFile;
    }

    public void deleteTempFile() {
        String tempFile = "temp.xlsx";
        try {
            Files.deleteIfExists(Paths.get(tempFile));
            log.debug(String.format("File %s has been deleted", tempFile));
        } catch (IOException e) {
            log.error(String.format("Can't find %s", tempFile));
        }
    }

    public byte[] export(Integer year, Integer month, Long userId) throws IOException {
        workbook = new XSSFWorkbook();

        this.exportYear = year;
        this.exportMonth = month;

        ApplicationUser user = applicationUserService.findById(userId);

        sheet = workbook.createSheet(String.format("%s_%s_%s_%s", user.getFirstName(), user.getLastName(), this.exportMonth, this.exportYear));

        sheet.setColumnWidth(0, COLUMN_WIDTH);

        Row infoPanelRow = sheet.createRow(1);
        infoPanelRow.setHeight((short) PANEL_HEIGHT);

        List<Workday> workdays = getWorkdays(user);
        writeInfoPanel(infoPanelRow);
        writeHeader(workdays);
        writeMorningHours(workdays);
        writeNightHours(workdays, user);
        writeExtraHours(workdays);
        writeNotes(workdays);
        writeLegend();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private void writeInfoPanel(Row infoPanelRow) {
        for (int i = INFO_PANEL_START_COLUMN; i <= INFO_PANEL_END_COLUMN; i++) {
            Cell infoPanelCellComplementary = infoPanelRow.createCell(i);
            infoPanelCellComplementary.setCellStyle(getInfoPanelStyle());
        }

        Cell infoPanelCell = infoPanelRow.createCell(INFO_PANEL_START_COLUMN); //skip prima linea
        infoPanelRow.createCell(LAST_CELL_INDEX); //creo l'ultima cella per non avere tagli
        infoPanelCell.setCellValue("A= ore complessive B= ore lavorate senza ferie e straordinari");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, INFO_PANEL_START_COLUMN, INFO_PANEL_END_COLUMN));

        infoPanelCell.setCellStyle(getInfoPanelStyle());
    }

    private void writeHeader(List<Workday> workdays) {
        Row headerRow = sheet.createRow(DAYS_HEADER_ROW);
        Cell headerCell = headerRow.createCell(DAYS_START_COLUMN);
        headerCell.setCellStyle(getNameCellStyle());
        headerRow.setHeight((short) HEADER_ROW_HEIGHT);


        for (int i = DAYS_START_COLUMN; i <= DAYS_START_COLUMN + 1; i++) {
            Cell namePanelCellComplementary = headerRow.createCell(i);
            namePanelCellComplementary.setCellStyle(getNameCellStyle());
        }

        headerCell.setCellValue("Nome");
        sheet.setColumnWidth(DAYS_START_COLUMN, DAYS_START_COLUMN_WIDTH);
        //cella vuota prima dei giorni
        headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL);
        sheet.setColumnWidth(DAYS_HEADER_FIRST_EMPTY_CELL, DAYS_HEADER_FIRST_EMPTY_CELL_WIDTH);
        headerCell.setCellStyle(getEmptyHeaderCellStyle());

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;

        for (int calendarDay = 1; calendarDay <= daysInMonth; calendarDay++) {

            Workday selectedDay = getSelectedDay(workdays, calendarDay);
            headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay);
            headerCell.setCellValue(calendarDay);
            headerCell.setCellStyle(getDayNumberHeaderCellStyle());

            if (selectedDay.getWorkPermitHours() > 0) {
                if (selectedDay.getWorkPermitHours() < MAX_WORKING_HOURS) {
                    sheet.addMergedRegion(new CellRangeAddress(DAYS_HEADER_ROW, DAYS_HEADER_ROW, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + calendarDay + 1));
                    headerCell = headerRow.createCell(DAYS_HEADER_FIRST_EMPTY_CELL + calendarDay + notEntirePermitDays + 1);
                    notEntirePermitDays++;
                }
                headerCell.setCellValue(calendarDay);
                headerCell.setCellStyle(getDayNumberHeaderCellStyle());
            }
        }

        for (int i = 1; i <= daysInMonth + notEntirePermitDays; i++) {
            sheet.setColumnWidth(DAYS_HEADER_FIRST_EMPTY_CELL + i, DAYS_HEADER_FIRST_EMPTY_CELL_COLUMN_WIDTH);
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
        hoursRow.setHeight((short) HEADER_ROW_HEIGHT);

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
            setStandardTextAlignment(hoursCellStyle);
            hoursCellStyle.setBorderTop(BorderStyle.THIN);
            hoursCellStyle.setBorderBottom(BorderStyle.THIN);
            hoursCellStyle.setBorderLeft(BorderStyle.THIN);
            hoursCellStyle.setBorderRight(BorderStyle.THIN);

            Workday selectedDay = getSelectedDay(workdays, i);

            addHoliday(cell, hoursCellStyle, selectedDay);

            addSickness(cell, hoursCellStyle, selectedDay);

            addAccidentAtWork(cell, selectedDay);

            addFuneralLeave(cell, selectedDay);

            addWorkingHours(cell, selectedDay);

            notEntirePermitDays = handlePermitDayHoursOnMorningRow(hoursRow, notEntirePermitDays, i, hoursCellStyle, selectedDay);

            cell.setCellStyle(hoursCellStyle);

        }

        int daysHeaderLastEmptyCell = writeSeparatorCell(hoursRow, daysInMonth + notEntirePermitDays);

        int totalWorkingHours = workdays.stream()
                .map(w -> w.getWorkingHours() + (w.isFuneralLeave() ? MAX_WORKING_HOURS : 0))
                .mapToInt(w -> w).sum();

        int totalWorkPermitHours = workdays.stream()
                .map(Workday::getWorkPermitHours)
                .mapToInt(w -> w).sum();

        Cell workingHoursTotalCell = createTotalCell(hoursRow, daysHeaderLastEmptyCell, totalWorkingHours);

        int totalNonWorkingDayHours = getTotalNonWorkingDayHours(workdays);

        Cell nonWorkingHoursTotalCell = createTotalCell(hoursRow, workingHoursTotalCell.getColumnIndex(), totalNonWorkingDayHours, IndexedColors.LIGHT_YELLOW.getIndex());

        int totalHolidayHours = workdays.stream()
                .map(w -> w.isHoliday() ? MAX_WORKING_HOURS : 0)
                .mapToInt(w -> w).sum();

        Cell holidaysHoursTotalCell = createTotalCell(hoursRow, nonWorkingHoursTotalCell.getColumnIndex(), totalHolidayHours, IndexedColors.LIGHT_GREEN.getIndex());

        int totalSicknessHours = workdays.stream()
                .map(w -> w.isSick() ? MAX_WORKING_HOURS : 0)
                .mapToInt(w -> w).sum();

        Cell sicknessHoursTotalCell = createTotalCell(hoursRow, holidaysHoursTotalCell.getColumnIndex(), totalSicknessHours, IndexedColors.ROSE.getIndex());

        int totalOfTotals = (int) Math.round(workingHoursTotalCell.getNumericCellValue() + holidaysHoursTotalCell.getNumericCellValue() + sicknessHoursTotalCell.getNumericCellValue());

        Cell totalHoursTotalCell = createTotalCell(hoursRow, sicknessHoursTotalCell.getColumnIndex(), totalOfTotals);

        int totalExtraHours = workdays.stream()
                .map(Workday::getExtraHours)
                .mapToInt(w -> w).sum();

        int totalA = (int) Math.round(totalHoursTotalCell.getNumericCellValue() + totalExtraHours + totalWorkPermitHours);

        Cell totalACell = createTotalCell(hoursRow, totalHoursTotalCell.getColumnIndex(), totalA);

        int mergeColumn = totalACell.getColumnIndex();
        sheet.addMergedRegion(new CellRangeAddress(MORNING_HOURS_ROW, EXTRA_HOURS_ROW, mergeColumn, mergeColumn));

        int totalNightHours = workdays.stream()
                .map(Workday::getNightWorkingHours)
                .mapToInt(w -> w).sum();

        int totalB = totalWorkingHours + totalNightHours + totalNonWorkingDayHours + totalWorkPermitHours;

        Cell totalBCell = createTotalCell(hoursRow, totalACell.getColumnIndex(), totalB);

        mergeColumn = totalBCell.getColumnIndex();
        sheet.addMergedRegion(new CellRangeAddress(MORNING_HOURS_ROW, EXTRA_HOURS_ROW, mergeColumn, mergeColumn));

    }

    private void writeNightHours(List<Workday> workdays, ApplicationUser user) {
        Row nightHoursRow = sheet.createRow(NIGHT_HOURS_ROW);
        Cell userFullNameCell = nightHoursRow.createCell(DAYS_START_COLUMN);
        userFullNameCell.setCellStyle(getHourLabelStyle(true));
        nightHoursRow.setHeight((short) HEADER_ROW_HEIGHT);

        userFullNameCell.setCellValue(String.format("%s %s", user.getFirstName(), user.getLastName()));
        sheet.addMergedRegion(new CellRangeAddress(NIGHT_HOURS_ROW, NOTES_ROW, DAYS_START_COLUMN, DAYS_START_COLUMN));
        Cell nightHourLabelCell = nightHoursRow.createCell(HOUR_LABEL);
        nightHourLabelCell.setCellStyle(getHourLabelStyle(false));
        nightHourLabelCell.setCellValue("Ore notturne");

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;
        for (int i = 1; i <= daysInMonth; i++) {
            Cell cell = nightHoursRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
            CellStyle nightHoursCellStyle = workbook.createCellStyle();
            setStandardTextAlignment(nightHoursCellStyle);
            nightHoursCellStyle.setBorderBottom(BorderStyle.THIN);
            nightHoursCellStyle.setBorderLeft(BorderStyle.THIN);
            nightHoursCellStyle.setBorderRight(BorderStyle.THIN);
            setRedFontStyle(nightHoursCellStyle);

            Workday selectedDay = getSelectedDay(workdays, i);

            notEntirePermitDays = handleMergedCells(nightHoursRow, notEntirePermitDays, i, nightHoursCellStyle, selectedDay, NIGHT_HOURS_ROW);

            cell.setCellStyle(nightHoursCellStyle);

            addNightWorkingHours(cell, selectedDay);
        }

        int daysHeaderLastEmptyCell = writeSeparatorCell(nightHoursRow, daysInMonth + notEntirePermitDays);

        int totalNightWorkingHours = workdays.stream()
                .map(Workday::getNightWorkingHours)
                .mapToInt(w -> w).sum();

        Cell nightWorkingHoursTotalCell = createTotalCell(nightHoursRow, daysHeaderLastEmptyCell, totalNightWorkingHours);
        XSSFFont font = setRedFontForTotals(nightWorkingHoursTotalCell);

        Cell nonWorkingHoursTotalCell = createTotalCell(nightHoursRow, nightWorkingHoursTotalCell.getColumnIndex(), "", IndexedColors.LIGHT_YELLOW.getIndex());
        nonWorkingHoursTotalCell.getCellStyle().setFont(font);

        int firstMergeColumn = nonWorkingHoursTotalCell.getColumnIndex() + 1;
        int secondMergeColumn = firstMergeColumn + 1;
        sheet.addMergedRegion(new CellRangeAddress(NIGHT_HOURS_ROW, EXTRA_HOURS_ROW, firstMergeColumn, secondMergeColumn));

        Cell nightHoursTotalCellCopy = createTotalCell(nightHoursRow, secondMergeColumn, totalNightWorkingHours);
        nightHoursTotalCellCopy.getCellStyle().setFont(font);

        int totalACell = nightHoursTotalCellCopy.getColumnIndex() + 1;
        sheet.getRow(NIGHT_HOURS_ROW).createCell(totalACell).setCellStyle(getDayNumberHeaderCellStyle());
        int totalBCell = nightHoursTotalCellCopy.getColumnIndex() + TWO_COLUMN_AFTER_CURRENT;
        sheet.getRow(NIGHT_HOURS_ROW).createCell(totalBCell).setCellStyle(getDayNumberHeaderCellStyle());
    }

    private void setRedFontStyle(CellStyle notesCellStyle) {
        XSSFFont font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        font.setFontHeightInPoints((short) RED_FONT_SIZE);
        notesCellStyle.setFont(font);
    }

    private XSSFFont setRedFontForTotals(Cell nightWorkingHoursTotalCell) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) DAY_NUMBER_HEADER_FONT_SIZE);
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        nightWorkingHoursTotalCell.getCellStyle().setFont(font);
        return font;
    }

    private Workday getSelectedDay(List<Workday> workdays, int currentIterationDay) {
        return workdays.stream()
                .filter(day -> day.getDate().equals(LocalDate.of(exportYear, exportMonth, currentIterationDay)))
                .findAny().orElse(new Workday());
    }

    private void writeExtraHours(List<Workday> workdays) {
        Row extraHoursRow = sheet.createRow(EXTRA_HOURS_ROW);
        Cell emptyCell = extraHoursRow.createCell(DAYS_START_COLUMN);
        emptyCell.setCellStyle(getHourLabelStyle(true));
        extraHoursRow.setHeight((short) HEADER_ROW_HEIGHT);

        Cell extraHourLabelCell = extraHoursRow.createCell(HOUR_LABEL);
        extraHourLabelCell.setCellStyle(getHourLabelStyle(false));
        extraHourLabelCell.setCellValue("Straordinario");

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            Cell cell = extraHoursRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
            CellStyle extraHoursStyle = createMiddleCellStyle();
            setRedFontStyle(extraHoursStyle);

            Workday selectedDay = getSelectedDay(workdays, i);

            notEntirePermitDays = handleMergedCells(extraHoursRow, notEntirePermitDays, i, extraHoursStyle, selectedDay, EXTRA_HOURS_ROW);

            cell.setCellStyle(extraHoursStyle);
            if (selectedDay.getExtraHours() > 0) {
                cell.setCellValue(selectedDay.getExtraHours());
            }

        }

        int totalExtraHours = workdays.stream()
                .map(Workday::getExtraHours)
                .mapToInt(w -> w).sum();

        int daysHeaderLastEmptyCell = writeSeparatorCell(extraHoursRow, daysInMonth + notEntirePermitDays);
        Cell extraHoursTotalCell = createTotalCell(extraHoursRow, daysHeaderLastEmptyCell, totalExtraHours);
        XSSFFont font = setRedFontForTotals(extraHoursTotalCell);

        Cell nonWorkingHoursTotalCell = createTotalCell(extraHoursRow, extraHoursTotalCell.getColumnIndex(), "", IndexedColors.LIGHT_YELLOW.getIndex());

        Cell extraHoursTotalCellCopy = createTotalCell(extraHoursRow, nonWorkingHoursTotalCell.getColumnIndex() + TWO_COLUMN_AFTER_CURRENT, totalExtraHours);
        extraHoursTotalCellCopy.getCellStyle().setFont(font);

        int totalACell = extraHoursTotalCellCopy.getColumnIndex() + 1;
        sheet.getRow(EXTRA_HOURS_ROW).createCell(totalACell).setCellStyle(getDayNumberHeaderCellStyle());
        int totalBCell = extraHoursTotalCellCopy.getColumnIndex() + TWO_COLUMN_AFTER_CURRENT;
        sheet.getRow(EXTRA_HOURS_ROW).createCell(totalBCell).setCellStyle(getDayNumberHeaderCellStyle());
    }

    private CellStyle createMiddleCellStyle() {
        CellStyle extraHoursStyle = workbook.createCellStyle();
        setStandardTextAlignment(extraHoursStyle);
        extraHoursStyle.setBorderTop(BorderStyle.THIN);
        extraHoursStyle.setBorderLeft(BorderStyle.THIN);
        extraHoursStyle.setBorderRight(BorderStyle.THIN);
        return extraHoursStyle;
    }

    private void writeNotes(List<Workday> workdays) {
        Row notesRow = sheet.createRow(NOTES_ROW);
        Cell emptyCell = notesRow.createCell(DAYS_START_COLUMN);
        emptyCell.setCellStyle(getNotesLabelStyle(true));
        notesRow.setHeight((short) HEADER_ROW_HEIGHT);

        Cell nightHourLabelCell = notesRow.createCell(HOUR_LABEL);
        nightHourLabelCell.setCellStyle(getNotesLabelStyle(false));
        nightHourLabelCell.setCellValue("Note");

        int daysInMonth = getDaysInMonth();

        int notEntirePermitDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            Cell cell = notesRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
            CellStyle notesCellStyle = createMiddleCellStyle();
            if (i == daysInMonth) {
                notesCellStyle.setBorderRight(BorderStyle.MEDIUM);
            }
            notesCellStyle.setBorderBottom(BorderStyle.MEDIUM);
            notesCellStyle.setWrapText(true);
            setRedFontStyle(notesCellStyle);

            Workday selectedDay = getSelectedDay(workdays, i);

            notEntirePermitDays = handleMergedCells(notesRow, notEntirePermitDays, i, notesCellStyle, selectedDay, NOTES_ROW);

            cell.setCellStyle(notesCellStyle);
            cell.setCellValue(selectedDay.getNotes());

        }

        int daysHeaderLastEmptyCell = writeSeparatorCell(notesRow, daysInMonth + notEntirePermitDays);
        Cell workingHoursTotalCell = createTotalCell(notesRow, daysHeaderLastEmptyCell, "");
        Cell nonWorkingHoursTotalCell = createTotalCell(notesRow, workingHoursTotalCell.getColumnIndex(), "", IndexedColors.LIGHT_YELLOW.getIndex());
        Cell holidaysHoursTotalCell = createTotalCell(notesRow, nonWorkingHoursTotalCell.getColumnIndex(), "", IndexedColors.LIGHT_GREEN.getIndex());
        Cell sicknessHoursTotalCell = createTotalCell(notesRow, holidaysHoursTotalCell.getColumnIndex(), "", IndexedColors.ROSE.getIndex());
        Cell totalHoursTotalCell = createTotalCell(notesRow, sicknessHoursTotalCell.getColumnIndex(), "");
        Cell aTotalCell = createTotalCell(notesRow, totalHoursTotalCell.getColumnIndex(), "");
        createTotalCell(notesRow, aTotalCell.getColumnIndex(), "");

    }

    private void writeLegend() {
        Row legendRow = sheet.createRow(LEGEND_FIRST_ROW);
        writeLegendValue(legendRow, LEGEND_FIRST_COLUMN, "8", IndexedColors.ROSE.getIndex());
        writeEqualsSign(legendRow, LEGEND_FIRST_COLUMN);
        writeLegendDescription(legendRow, LEGEND_FIRST_COLUMN, "Malattia");

        writeLegendValue(legendRow, LEGEND_MIDDLE_COLUMN, "8", IndexedColors.SEA_GREEN.getIndex());
        writeEqualsSign(legendRow, LEGEND_MIDDLE_COLUMN);
        writeLegendDescription(legendRow, LEGEND_MIDDLE_COLUMN, "Permesso Retribuito");

        writeLegendValue(legendRow, LEGEND_LAST_COLUMN, "8", IndexedColors.LIGHT_GREEN.getIndex());
        writeEqualsSign(legendRow, LEGEND_LAST_COLUMN);
        writeLegendDescription(legendRow, LEGEND_LAST_COLUMN, "Ferie");

        legendRow = sheet.createRow(LEGEND_SECOND_ROW);
        writeLegendValue(legendRow, LEGEND_FIRST_COLUMN, "PL", IndexedColors.WHITE.getIndex());
        writeEqualsSign(legendRow, LEGEND_FIRST_COLUMN);
        writeLegendDescription(legendRow, LEGEND_FIRST_COLUMN, "Permesso per lutto");

        writeLegendValue(legendRow, LEGEND_MIDDLE_COLUMN, "PS", IndexedColors.RED.getIndex());
        writeEqualsSign(legendRow, LEGEND_MIDDLE_COLUMN);
        writeLegendDescription(legendRow, LEGEND_MIDDLE_COLUMN, "Permesso sindacale");

        writeLegendValue(legendRow, LEGEND_LAST_COLUMN, "I", IndexedColors.WHITE.getIndex());
        writeEqualsSign(legendRow, LEGEND_LAST_COLUMN);
        writeLegendDescription(legendRow, LEGEND_LAST_COLUMN, "Infortunio");
    }

    private void writeLegendValue(Row legendRow, int column, String legendContent, short backgroundColorIndex) {
        Cell legendCell = legendRow.createCell(column);
        CellStyle legendStyle = workbook.createCellStyle();
        setThinSquareStyle(legendStyle);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) RED_FONT_SIZE);
        legendStyle.setFillForegroundColor(backgroundColorIndex);
        legendStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setStandardTextAlignment(legendStyle);
        legendCell.setCellValue(legendContent);
        legendCell.setCellStyle(legendStyle);
    }

    private void writeLegendDescription(Row legendRow, int column, String legendDescription) {
        int columnOffset = column + TWO_COLUMN_AFTER_CURRENT;
        Cell legendCell = legendRow.createCell(columnOffset);
        CellStyle legendStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) CUSTOM_FONT_SIZE);
        setStandardTextAlignment(legendStyle);
        legendCell.setCellValue(legendDescription);
        legendCell.setCellStyle(legendStyle);
        int rowNum = legendRow.getRowNum();
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, columnOffset, columnOffset + THREE_NEXT_REGIONS));

    }

    private void writeEqualsSign(Row headerRow, int legendEarlierColumn) {
        Cell equalSign = headerRow.createCell(legendEarlierColumn + 1);
        CellStyle signStyle = workbook.createCellStyle();
        setStandardTextAlignment(signStyle);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) CUSTOM_FONT_SIZE);
        equalSign.setCellStyle(signStyle);
        signStyle.setFont(font);
        equalSign.setCellValue("=");
    }

    private static void setStandardTextAlignment(CellStyle legendStyle) {
        legendStyle.setAlignment(HorizontalAlignment.CENTER);
        legendStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    private int handleMergedCells(Row currentRow, int notEntirePermitDays, int i, CellStyle cellStyle, Workday selectedDay, int rowIndex) {
        if (selectedDay.getWorkPermitHours() > 0 && selectedDay.getWorkPermitHours() < MAX_WORKING_HOURS) {
            sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + i, DAYS_HEADER_FIRST_EMPTY_CELL + notEntirePermitDays + i + 1));
            Cell nextCell = currentRow.createCell(HOUR_LABEL + i + 1 + notEntirePermitDays);

            if (currentRow.getRowNum() == MORNING_HOURS_ROW) {
                nextCell.setCellValue(selectedDay.getWorkPermitHours());
            }
            nextCell.setCellStyle(cellStyle);
            notEntirePermitDays++;
        }
        return notEntirePermitDays;
    }

    private int writeSeparatorCell(Row headerRow, int daysInMonth) {
        Cell headerCell;
        int daysHeaderLastEmptyCell = DAYS_HEADER_FIRST_EMPTY_CELL + daysInMonth + 1;
        headerCell = headerRow.createCell(daysHeaderLastEmptyCell);
        headerCell.setCellStyle(getDayNumberHeaderCellStyle());
        sheet.setColumnWidth(daysHeaderLastEmptyCell, DAYS_HEADER_LAST_EMPTY_CELL_COLUMN_WIDTH);
        return daysHeaderLastEmptyCell;
    }


    private static void addWorkingHours(Cell cell, Workday selectedDay) {
        if (selectedDay.getWorkingHours() > 0) {
            cell.setCellValue(selectedDay.getWorkingHours());
        }
    }

    private static void addNightWorkingHours(Cell cell, Workday selectedDay) {
        if (selectedDay.getNightWorkingHours() > 0) {
            cell.setCellValue(selectedDay.getNightWorkingHours());
        }
    }


    private static void addFuneralLeave(Cell cell, Workday selectedDay) {
        if (selectedDay.isFuneralLeave()) {
            cell.setCellValue("PL");
        }
    }

    private static void addAccidentAtWork(Cell cell, Workday selectedDay) {
        if (selectedDay.isAccidentAtWork()) {
            cell.setCellValue("I");
        }
    }

    private static void addSickness(Cell cell, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.isSick()) {
            cell.setCellValue(MAX_WORKING_HOURS);
            hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            hoursCellStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        }
    }

    private static void addHoliday(Cell cell, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.isHoliday()) {
            cell.setCellValue(MAX_WORKING_HOURS);
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
        sheet.setColumnWidth(totalCell, SHEET_TOTAL_COLUMN_WIDTH);
        return cell;
    }

    private Cell createTotalCell(Row row, int referenceToPreviousCell, int hours, Short... backgroundColor) {
        int totalCell = referenceToPreviousCell + 1;
        Cell cell = row.createCell(totalCell);
        cell.setCellValue(hours);
        cell.setCellStyle(getDayNumberHeaderCellStyle(backgroundColor));
        sheet.setColumnWidth(totalCell, SHEET_TOTAL_COLUMN_WIDTH);
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
        setStandardTextAlignment(infoPanelStyle);
        infoPanelStyle.setBorderTop(BorderStyle.THIN);
        infoPanelStyle.setBorderBottom(BorderStyle.THIN);
        infoPanelStyle.setBorderLeft(BorderStyle.THIN);
        infoPanelStyle.setBorderRight(BorderStyle.THIN);
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) INFO_PANEL_FONT_SIZE);
        font.setBold(true);
        infoPanelStyle.setFont(font);

        return infoPanelStyle;
    }

    private CellStyle getNameCellStyle() {
        CellStyle nameCellStyle = workbook.createCellStyle();
        nameCellStyle.setWrapText(true);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) NAME_CELL_FONT_SIZE);
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        nameCellStyle.setFont(font);
        setStandardTextAlignment(nameCellStyle);
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
        font.setFontHeightInPoints((short) DAY_NUMBER_HEADER_FONT_SIZE);
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
        setStandardTextAlignment(dateHeaderStyle);
        dateHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        dateHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
        setCustomFont(bold, dateHeaderStyle);

        return dateHeaderStyle;
    }

    private void setCustomFont(boolean bold, CellStyle dateHeaderStyle) {
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) CUSTOM_FONT_SIZE);
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

    private static void setHardSquareStyle(CellStyle notesHeaderStyle) {
        setStandardTextAlignment(notesHeaderStyle);
        notesHeaderStyle.setBorderTop(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderLeft(BorderStyle.MEDIUM);
        notesHeaderStyle.setBorderRight(BorderStyle.MEDIUM);
    }

    private static void setThinSquareStyle(CellStyle notesHeaderStyle) {
        setStandardTextAlignment(notesHeaderStyle);
        notesHeaderStyle.setBorderTop(BorderStyle.THIN);
        notesHeaderStyle.setBorderBottom(BorderStyle.THIN);
        notesHeaderStyle.setBorderLeft(BorderStyle.THIN);
        notesHeaderStyle.setBorderRight(BorderStyle.THIN);
    }

    private int handlePermitDayHoursOnMorningRow(Row hoursRow, int notEntirePermitDays, int i, CellStyle hoursCellStyle, Workday selectedDay) {
        if (selectedDay.getWorkPermitHours() > 0) {
            Cell nextCell;
            if (selectedDay.getWorkPermitHours() < MAX_WORKING_HOURS) {
                nextCell = hoursRow.createCell(HOUR_LABEL + i + 1 + notEntirePermitDays);
                notEntirePermitDays++;
            } else {
                nextCell = hoursRow.createCell(HOUR_LABEL + i + notEntirePermitDays);
                hoursCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                hoursCellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
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


    private int getTotalNonWorkingDayHours(List<Workday> workdays) {
        int totalNonWorkingDayHours = 0;
        String easter = EasterUtility.calculateEaster(exportYear).getDayAndMonth();
        String easterMonday = EasterUtility.calculateEasterMonday(exportYear).getDayAndMonth();
        for (Workday day : workdays) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M");
            LocalDate dayDate = day.getDate();
            if (dayDate.format(formatter).equals(NEW_YEARS_EVE.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(EPIPHANY.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(APRIL_TWENTY_FIFTH.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(WORKERS_DAY.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(REPUBLIC_DAY.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(MIDSUMMER.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(MID_AUGUST.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(ALL_SAINTS_DAY.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(IMMACULATE_CONCEPTION.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(CHRISTMAS.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(BOXING_DAY.getSpecialDay().getDayAndMonth())
                    || dayDate.format(formatter).equals(easter)
                    || dayDate.format(formatter).equals(easterMonday)
                    || dayDate.getDayOfWeek() == DayOfWeek.SATURDAY
                    || dayDate.getDayOfWeek() == DayOfWeek.SUNDAY
            ) {
                totalNonWorkingDayHours += day.getWorkingHours() + day.getNightWorkingHours() + day.getExtraHours();
            }

        }
        return totalNonWorkingDayHours;
    }

}

