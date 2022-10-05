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
import java.util.List;

@Service
public class ExportService {

    public static final int INFO_PANEL_INDEX = 40;
    public static final int LAST_CELL_INDEX = 100;
    public static final int FIRST_EMPTY_CELL_INDEX = 0;

    private final WorkdayService workdayService;
    private final ApplicationUserService applicationUserService;

    public ExportService(WorkdayService workdayService, ApplicationUserService applicationUserService) {
        this.workdayService = workdayService;
        this.applicationUserService = applicationUserService;
    }

    public void export(Integer year, Integer month, Long userId) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        ApplicationUser user = applicationUserService.findById(userId);

        List<Workday> workdays = getWorkdays(year, month, user);


        //TODO find username by id

        Sheet sheet = workbook.createSheet(String.format("%s_%s_%s_%s", user.getFirstName(), user.getLastName(), month, year));

        sheet.setColumnWidth(0, 300);

        sheet.createRow(FIRST_EMPTY_CELL_INDEX); //skip prima linea
        Row infoPanelRow = sheet.createRow(1);
        infoPanelRow.setHeight((short) 1000);



//        infoPanelStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
//        infoPanelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);



        Cell infoPanelCell = infoPanelRow.createCell(INFO_PANEL_INDEX); //skip prima linea
        infoPanelRow.createCell(LAST_CELL_INDEX); //creo l'ultima cella per non avere tagli
        infoPanelCell.setCellValue("A= ore complessive B= ore lavorare senza ferie e straordinari");
        sheet.addMergedRegion(new CellRangeAddress(1, 1, INFO_PANEL_INDEX, INFO_PANEL_INDEX + 6));

        CellStyle infoPanelStyle = workbook.createCellStyle();
        setInfoPanelStyle(infoPanelStyle);

        XSSFFont font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        infoPanelStyle.setFont(font);

        infoPanelCell.setCellStyle(infoPanelStyle);



//        infoPanelCell = infoPanelRow.createCell(2);
//        infoPanelCell.setCellValue("Age");
//        infoPanelCell.setCellStyle(infoPanelStyle);

        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);

        Row row = sheet.createRow(2);
        Cell cell = row.createCell(1);
        cell.setCellValue("John Smith");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(20);
        cell.setCellStyle(style);

        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + "temp.xlsx";

        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        workbook.close();
    }

    private List<Workday> getWorkdays(Integer year, Integer month, ApplicationUser user) {
        LocalDate date = LocalDate.of(year, month, 1);
        LocalDate from = date.withDayOfMonth(1);
        LocalDate to = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));
        return workdayService.findWorkdayByUser(user.getUsername(), from, to);
    }

    private static void setInfoPanelStyle(CellStyle infoPanelStyle) {
        infoPanelStyle.setWrapText(true);
        infoPanelStyle.setAlignment(HorizontalAlignment.CENTER);
        infoPanelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        infoPanelStyle.setBorderTop(BorderStyle.THIN);
        infoPanelStyle.setBorderBottom(BorderStyle.THIN);
        infoPanelStyle.setBorderLeft(BorderStyle.THIN);
        infoPanelStyle.setBorderRight(BorderStyle.THIN);
    }
}
