package com.colaorange.dailymoney.core.xlsx;

import android.content.Context;
import android.content.res.AssetManager;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Formats;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dennis
 */
public class XlsxBalanceExporter {


    private Context ctx;

    private String errMsg;

    public XlsxBalanceExporter(Context ctx) {
        this.ctx = ctx;
    }


    public void export(String sheetName, String subject, List<Balance> balanceList, File destFile) {
        Contexts contexts = Contexts.instance();
        CalendarHelper cal = contexts.getCalendarHelper();
        I18N i18n = contexts.getI18n();


        int cellStart = 1;
        int rowStart = 1;

        InputStream is = null;
        OutputStream os = null;
        try {

            int decimalLength = 0;
            int indentLength = 0;

            DecimalFormat df = Formats.getMoneyFormat();
            for (Balance balance : balanceList) {
                decimalLength = Math.max(decimalLength, Formats.getDecimalLength(df, balance.getMoney()));
                indentLength = Math.max(indentLength, balance.getIndent());
            }


//                    is = getContextsActivity().getAssets().open("test.xlsx");

            Workbook workbook = new XSSFWorkbook();

            //CreationHelper helps us create instances of various things like DataFormat,
            //Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way
            CreationHelper createHelper = workbook.getCreationHelper();

            //create styles
            // Create a Font for styling header cells
            Font defaultFont = workbook.createFont();
            defaultFont.setFontHeightInPoints((short) 11);

            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setFont(defaultFont);


            Font subjectFont = workbook.createFont();
            subjectFont.setBold(true);
            subjectFont.setFontHeightInPoints((short) 14);

            CellStyle subjectStyle = workbook.createCellStyle();
            subjectStyle.setFont(subjectFont);
            subjectStyle.setAlignment(HorizontalAlignment.CENTER);
            subjectStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            subjectStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle moneyStyle = workbook.createCellStyle();
            String format = decimalLength == 0 ? "#,##0" : Strings.padEnd("#,##0.", decimalLength, '0');
            moneyStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));

            CellStyle headerMoneyStyle = workbook.createCellStyle();
            headerMoneyStyle.setDataFormat(moneyStyle.getDataFormat());
            headerMoneyStyle.setFont(headerFont);
            headerMoneyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerMoneyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create a Sheet
            Sheet sheet = workbook.createSheet(sheetName);

            //apply default style
            for (int i = 0; i <= cellStart + indentLength + 1; i++) {
                sheet.setDefaultColumnStyle(i, defaultStyle);
            }

            //adjust column length
            for (int i = cellStart; i <= cellStart + indentLength; i++) {
                if(i==cellStart+indentLength) {
                    sheet.setColumnWidth(i, XlsxUtil.characterToWidth(20));
                }else{
                    sheet.setColumnWidth(i, XlsxUtil.characterToWidth(4));
                }
            }
            sheet.setColumnWidth(cellStart + indentLength + 1, XlsxUtil.characterToWidth(16));

            int rowIdx = rowStart;
            //subject
            Row row = sheet.createRow(rowIdx);
            Cell cell = row.createCell(cellStart);
            cell.setCellValue(subject);
            cell.setCellStyle(subjectStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, cellStart, cellStart + indentLength + 1));

            rowIdx += 2;

            //balances
            for (Balance balance : balanceList) {
                row = sheet.createRow(rowIdx);

                //account column
                int cellIdx = cellStart + balance.getIndent();
                cell = row.createCell(cellIdx);
                cell.setCellValue(balance.getName());

                if (balance.getIndent() == 0) {
                    //don't reuse, header and header money has different format
                    cell.setCellStyle(XlsxUtil.newAccountFillStyle(workbook, headerStyle, balance.getType(), null));
                }
                if (cellStart + indentLength - cellIdx > 0) {
                    sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, cellIdx, cellStart + indentLength));
                }


                //money column
                cell = row.createCell(cellStart + indentLength + 1);
                cell.setCellValue(balance.getMoney());

                if (balance.getIndent() == 0) {
                    cell.setCellStyle(XlsxUtil.newAccountFillStyle(workbook, headerMoneyStyle, balance.getType(), null));
                } else {
                    cell.setCellStyle(moneyStyle);
                }

                rowIdx++;
            }


            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(destFile);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();


            Logger.d(">>>> write report to " + destFile.getAbsolutePath());
        } catch (Exception x) {
            errMsg = i18n.string(R.string.msg_error, x.getMessage());
            Logger.e(x.getMessage(), x);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }

        }
    }

    public String getErrMsg() {
        return errMsg;
    }
}
