package com.colaorange.dailymoney.core.ui.legacy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardDesktop;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.data.DefaultCardDesktopCreator;
import com.colaorange.dailymoney.core.ui.cards.CardFacade;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.util.Notifications;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author dennis
 */
public class TestsDesktop extends AbstractDesktop {

    public static final String NAME = "tests";

    public TestsDesktop(Activity activity) {
        super(NAME, activity);

    }

    @Override
    public boolean isAvailable() {
        return Contexts.instance().getPreference().isTestsDesktop();
    }

    static int notificationCount=0;

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();

        label = i18n.string(R.string.desktop_tests);

        addItem(new DesktopItem(new Runnable() {

            public void run() {
                Notifications.send(activity, Notifications.nextGroupId(),
                        "A info message "+Notifications.currGroupId(), "A info title "+Notifications.currGroupId(),
                        Notifications.Channel.BACKUP,
                        Notifications.currGroupId()%3==0?Notifications.Level.ERROR:Notifications.Level.WARN, null);
            }
        }, "Notification Test", R.drawable.nav_pg_test));

        addItem(new DesktopItem(new Runnable() {
            public void run() {
               activity.startActivity(new Intent(activity, GoogleDriveActivity.class));
            }
        }, "Drive Test", R.drawable.nav_pg_test));

        addItem(new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance();
                ctx.getMasterDataProvider().reset();
            }
        }, "Reset Master Dataprovider", R.drawable.nav_pg_test));

        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                Contexts.instance().getDataProvider().reset();
                GUIs.shortToast(activity, "reset data provider");
            }
        }, "rest data provider", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(200, null);
            }
        }, "Busy 200ms", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(200, "error short");
            }
        }, "Busy 200ms Error", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(5000, null);
            }
        }, "Busy 5s", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testBusy(5000, "error long");
            }
        }, "Busy 5s Error", R.drawable.nav_pg_test));


        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(25);
            }
        }, "test data25", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(50);
            }
        }, "test data50", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(100);
            }
        }, "test data100", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testCreateTestdata(200);
            }
        }, "test data200", R.drawable.nav_pg_test));
        addItem(new DesktopItem(new Runnable() {
            @Override
            public void run() {
                testJust();
            }
        }, "just test", R.drawable.nav_pg_test));

        DesktopItem padding = new DesktopItem(new Runnable() {
            @Override
            public void run() {

            }
        }, "padding", R.drawable.nav_pg_test);

        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
        addItem(padding);
    }

    private void testCreateXLS() {
        List<Map<String,String>> employees = new LinkedList<>();
        for(int i=0;i<10;i++){
            Map<String,String> employee = new HashMap<>();
            employee.put("name","Name "+(i+1));
            employee.put("birthday","Birthday "+(i+1));
            employee.put("payment","Payment "+(i+1));
            employee.put("bonus","Bonus "+(i+1));
            employees.add(employee);
        }
        String[] columns = {"Name",  "Date Of Birth", "Payment", "Bonus"};

        InputStream is = null;
        OutputStream os = null;
        try {

//            javax.xml.stream.XMLEventFactory

            is = activity.getAssets().open("test.xlsx");

            File file = new File(Contexts.instance().getWorkingFolder(),"reports");
            file.mkdir();
            file = new File(file,"test2.xlsx");

            Workbook workbook = new XSSFWorkbook();

             /* CreationHelper helps us create instances of various things like DataFormat,
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
            CreationHelper createHelper = workbook.getCreationHelper();

            // Create a Sheet
            Sheet sheet = workbook.createSheet("Employee");

            // Create a Font for styling header cells
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.RED.getIndex());

            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create a Row
            Row headerRow = sheet.createRow(0);

            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Cell Style for formatting Date
            CellStyle dateCellStyle = workbook.createCellStyle();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

            // Create Other rows and cells with employees data
            int rowNum = 1;
            for(Map<String,String> employee: employees) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(employee.get("name"));

                row.createCell(1)
                        .setCellValue(employee.get("birthday"));

                Cell dateOfBirthCell = row.createCell(2);
                dateOfBirthCell.setCellValue(employee.get("payment"));
                dateOfBirthCell.setCellStyle(dateCellStyle);

                row.createCell(3)
                        .setCellValue(employee.get("bonus"));
            }

            // Resize all columns to fit the content size
            for(int i = 0; i < columns.length; i++) {
//                sheet.autoSizeColumn(i);
            }

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();


            Logger.i(">>>> write report to "+file.getAbsolutePath());
        }catch(Exception x){
            x.printStackTrace();
        }finally{
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                }
            }

        }

    }

    protected void testBusy(final long i, final String error) {
        GUIs.doBusy(activity, new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity, "task finished");
            }

            public void onBusyError(Throwable x) {
                GUIs.shortToast(activity, "Error " + x.getMessage());
            }

            @Override
            public void run() {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (error != null) {
                    throw new RuntimeException(error);
                }
            }
        });
    }

    protected void testFirstDayOfWeek() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        for (int i = 0; i < 100; i++) {
            Date now = new Date();
            Date start = calHelper.weekStartDate(now);
            Date end = calHelper.weekEndDate(now);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
//            System.out.println("1>>>>>>>>>>> "+now);
            System.out.println("2>>>>>>>>>>> " + start);
//            System.out.println("3>>>>>>>>>>> "+end);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void testCreateTestdata(final int loop) {
        GUIs.doBusy(activity, new GUIs.BusyAdapter() {
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity, "create test data");
            }

            @Override
            public void run() {
                I18N i18n = Contexts.instance().getI18n();
                IDataProvider idp = Contexts.instance().getDataProvider();
                new DataCreator(idp, i18n).createTestData(loop);
            }
        });

    }


    protected void testJust() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        Date now = new Date();
        Date start = calHelper.weekStartDate(now);
        Date end = calHelper.weekEndDate(now);
        System.out.println(">>>>>>>>>>>>>>> " + now);
        System.out.println("1>>>>>>>>>>> " + now);
        System.out.println("2>>>>>>>>>>> " + start);
        System.out.println("3>>>>>>>>>>> " + end);
        System.out.println(">>>>>>>>>>>>>> " + now);

    }

}
