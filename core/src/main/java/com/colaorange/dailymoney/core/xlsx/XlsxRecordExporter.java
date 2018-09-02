package com.colaorange.dailymoney.core.xlsx;

import android.content.Context;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Formats;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import org.apache.poi.ss.usermodel.BorderStyle;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Dennis
 */
public class XlsxRecordExporter {


    private Context ctx;

    private String errMsg;

    private Map<String, Account> accountMap;

    public XlsxRecordExporter(Context ctx, Map<String, Account> accountMap) {
        this.ctx = ctx;
        this.accountMap = accountMap;
    }


    public void export(String sheetName, String subject, List<Record> recordList, File destFile) {

        recordList = new LinkedList<>(recordList);

        Collections.sort(recordList, new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                return Long.valueOf(o1.getDate().getTime()).compareTo(Long.valueOf(o2.getDate().getTime()));
            }
        });

        Contexts contexts = Contexts.instance();
        CalendarHelper cal = contexts.getCalendarHelper();
        I18N i18n = contexts.getI18n();
        DateFormat dateFormat = contexts.getPreference().getDateFormat();


        int cellStart = 1;
        int rowStart = 1;

        InputStream is = null;
        OutputStream os = null;
        try {

            int decimalLength = 0;

            DecimalFormat df = Formats.getMoneyFormat();
            for (Record record : recordList) {
                decimalLength = Math.max(decimalLength, Formats.getDecimalLength(df, record.getMoney()));
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
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            CellStyle fieldStyle = workbook.createCellStyle();
            fieldStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moneyStyle = workbook.createCellStyle();
            String format = decimalLength == 0 ? "#,##0" : Strings.padEnd("#,##0.", decimalLength, '0');
            moneyStyle.setDataFormat(createHelper.createDataFormat().getFormat(format));
            moneyStyle.setBorderBottom(BorderStyle.THIN);

            // Create a Sheet
            Sheet sheet = workbook.createSheet(sheetName);


            //cloumns
            String columns[] = new String[]{
                    i18n.string(R.string.label_from_account),
                    i18n.string(R.string.label_to_account),
                    i18n.string(R.string.label_date),
                    i18n.string(R.string.label_money),
                    i18n.string(R.string.label_note),
            };
            int columnNum = columns.length;

            //apply default style
            for (int i = 0; i <= cellStart + columnNum; i++) {
                sheet.setDefaultColumnStyle(i, defaultStyle);
            }

            int rowIdx = rowStart;
            //subject
            Row row = sheet.createRow(rowIdx);
            Cell cell = row.createCell(cellStart);
            cell.setCellValue(subject);
            cell.setCellStyle(subjectStyle);
            //border
            for (int i = 1; i < columnNum; i++) {
                cell = row.createCell(cellStart+i);
                cell.setCellStyle(subjectStyle);
            }

            CellRangeAddress range = new CellRangeAddress(rowIdx, rowIdx, cellStart, cellStart + columnNum - 1);
            sheet.addMergedRegion(range);


            rowIdx += 2;

            row = sheet.createRow(rowIdx);
            for (int i = 0; i < columns.length; i++) {
                int cellIdx = cellStart + i;
                cell = row.createCell(cellIdx);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(columns[i]);
                switch (i) {
                    case 0:
                    case 1:
                        sheet.setColumnWidth(cellIdx, XlsxUtil.characterToWidth(30));
                        break;
                    case 2:
                        sheet.setColumnWidth(cellIdx, XlsxUtil.characterToWidth(18));
                        break;
                    case 3:
                        sheet.setColumnWidth(cellIdx, XlsxUtil.characterToWidth(16));
                        break;
                    case 4:
                        sheet.setColumnWidth(cellIdx, XlsxUtil.characterToWidth(60));
                        break;
                }
            }

            rowIdx++;

            Map<String, CellStyle> reuse = new LinkedHashMap<>();
            //balances
            for (Record record : recordList) {
                row = sheet.createRow(rowIdx);

                //From(0)
                int cellIdx = cellStart;
                cell = row.createCell(cellIdx);
                cell.setCellValue(XlsxUtil.toAccountDisplay(i18n, accountMap, record.getFrom()));
                cell.setCellStyle(XlsxUtil.newAccountFillStyle(workbook, fieldStyle, record.getFromType(), reuse));

                //To(1)
                cellIdx++;
                cell = row.createCell(cellIdx);
                cell.setCellValue(XlsxUtil.toAccountDisplay(i18n, accountMap, record.getTo()));
                cell.setCellStyle(XlsxUtil.newAccountFillStyle(workbook, fieldStyle, record.getToType(), reuse));

                //Date(2)
                cellIdx++;
                cell = row.createCell(cellIdx);
                cell.setCellValue(dateFormat.format(record.getDate()));
                cell.setCellStyle(fieldStyle);

                //Money(3)
                cellIdx++;
                cell = row.createCell(cellIdx);
                cell.setCellValue(record.getMoney());
                cell.setCellStyle(moneyStyle);

                //Note(4)
                cellIdx++;
                cell = row.createCell(cellIdx);
                cell.setCellValue(record.getNote());
                cell.setCellStyle(fieldStyle);

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
