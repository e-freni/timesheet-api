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
import java.util.List;

@Service
public class ExportService {

    public static final int INFO_PANEL_START_COLUMN = 36;
    public static final int INFO_PANEL_END_COLUMN = INFO_PANEL_START_COLUMN + 6;
    public static final int LAST_CELL_INDEX = 100;
    public static final int DAYS_HEADER_START_COLUMN = 1;
    public static final int DAYS_HEADER_EMPTY_CELL = DAYS_HEADER_START_COLUMN + 2;
    private static final int DAYS_HEADER_ROW = 3;

    private Integer year;
    private Integer month;

    private final WorkdayService workdayService;
    private final ApplicationUserService applicationUserService;

    public ExportService(WorkdayService workdayService, ApplicationUserService applicationUserService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
    }

    public void export(Integer year, Integer month, Long userId) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        this.year = year;
        this.month = month;

        ApplicationUser user = applicationUserService.findById(userId);

        List<Workday> workdays = getWorkdays(year, month, user);


        //TODO find username by id

        Sheet sheet = workbook.createSheet(String.format("%s_%s_%s_%s", user.getFirstName(), user.getLastName(), month, year));

        sheet.setColumnWidth(0, 300);

        Row infoPanelRow = sheet.createRow(1);
        infoPanelRow.setHeight((short) 1000);

//        infoPanelStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//        infoPanelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        writeInfoPanel(workbook, sheet, infoPanelRow);


//        infoPanelCell = infoPanelRow.createCell(2);
//        infoPanelCell.setCellValue("Age");
//        infoPanelCell.setCellStyle(infoPanelStyle);

        writeHeader(workbook, sheet, workdays);

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }

    private void writeHeader(XSSFWorkbook workbook, Sheet sheet, List<Workday> workdays) {
        Row headerRow = sheet.createRow(DAYS_HEADER_ROW);
        Cell cell = headerRow.createCell(DAYS_HEADER_START_COLUMN);
        cell.setCellStyle(getNameCellStyle(workbook));

        for (int i = DAYS_HEADER_START_COLUMN; i <= DAYS_HEADER_START_COLUMN + 1; i++) {
            Cell namePanelCellComplementary = headerRow.createCell(i);
            namePanelCellComplementary.setCellStyle(getNameCellStyle(workbook));
        }

        sheet.addMergedRegion(new CellRangeAddress(DAYS_HEADER_ROW, DAYS_HEADER_ROW,
                DAYS_HEADER_START_COLUMN, DAYS_HEADER_START_COLUMN + 1));

        cell.setCellValue("Nome");
        cell = headerRow.createCell(DAYS_HEADER_EMPTY_CELL); //cella vuota prima dei giorni
        cell.setCellStyle(getEmptyHeaderCellStyle(workbook));


        YearMonth yearMonthObject = YearMonth.of(year, month);
        int daysInMonth = yearMonthObject.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            cell = headerRow.createCell(DAYS_HEADER_EMPTY_CELL + day);
            cell.setCellValue(day);
            cell.setCellStyle(getDayNumberHeaderCellStyle(workbook));
            sheet.setColumnWidth(DAYS_HEADER_EMPTY_CELL + day, 1500);
        }


//        cell = row.createCell(2);
//        cell.setCellValue(20);
//        cell.setCellStyle(style);
    }

    private void writeInfoPanel(XSSFWorkbook workbook, Sheet sheet, Row infoPanelRow) {
        for (int i = INFO_PANEL_START_COLUMN; i <= INFO_PANEL_END_COLUMN; i++) {
            Cell infoPanelCellComplementary = infoPanelRow.createCell(i);
            infoPanelCellComplementary.setCellStyle(getInfoPanelStyle(workbook));
        }

        Cell infoPanelCell = infoPanelRow.createCell(INFO_PANEL_START_COLUMN); //skip prima linea
        infoPanelRow.createCell(LAST_CELL_INDEX); //creo l'ultima cella per non avere tagli
        infoPanelCell.setCellValue("A= ore complessive B= ore lavorare senza ferie e straordinari");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, INFO_PANEL_START_COLUMN, INFO_PANEL_END_COLUMN));

        infoPanelCell.setCellStyle(getInfoPanelStyle(workbook));
    }

    private List<Workday> getWorkdays(Integer year, Integer month, ApplicationUser user) {
        LocalDate date = LocalDate.of(year, month, 1);
        LocalDate from = date.withDayOfMonth(1);
        LocalDate to = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));

        return workdayService.findWorkdayByUser(user.getUsername(), from, to);
    }

    private static CellStyle getInfoPanelStyle(XSSFWorkbook workbook) {
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

    private static CellStyle getNameCellStyle(XSSFWorkbook workbook) {
        CellStyle nameCellStyle = workbook.createCellStyle();
        nameCellStyle.setWrapText(true);
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 16);
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

    private static CellStyle getEmptyHeaderCellStyle(XSSFWorkbook workbook) {
        CellStyle emptyHeaderCellStyle =  workbook.createCellStyle();
        emptyHeaderCellStyle.setBorderTop(BorderStyle.MEDIUM);
        emptyHeaderCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        emptyHeaderCellStyle.setBorderLeft(BorderStyle.THIN);
        emptyHeaderCellStyle.setBorderRight(BorderStyle.THIN);

        return emptyHeaderCellStyle;
    }

    private static CellStyle getDayNumberHeaderCellStyle(XSSFWorkbook workbook) {
        CellStyle dayNumberHeaderCell =  workbook.createCellStyle();
        dayNumberHeaderCell.setAlignment(HorizontalAlignment.CENTER);
        dayNumberHeaderCell.setVerticalAlignment(VerticalAlignment.CENTER);
        dayNumberHeaderCell.setBorderTop(BorderStyle.MEDIUM);
        dayNumberHeaderCell.setBorderBottom(BorderStyle.MEDIUM);
        dayNumberHeaderCell.setBorderLeft(BorderStyle.MEDIUM);
        dayNumberHeaderCell.setBorderRight(BorderStyle.MEDIUM);
        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        dayNumberHeaderCell.setFont(font);

        return dayNumberHeaderCell;
    }
}
