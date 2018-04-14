package com.colaorange.dailymoney.ui.legacy;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.Book;
import com.colaorange.dailymoney.data.DataCreator;
import com.colaorange.dailymoney.data.IDataProvider;
import com.colaorange.dailymoney.data.SymbolPosition;
import com.colaorange.dailymoney.ui.Constants;

/**
 * 
 * @author dennis
 *
 */
public class TestsDesktop extends AbstractDesktop {
    
    public TestsDesktop(Activity activity) {
        super(activity);
        
    }
    
    @Override
    public boolean isAvailable(){
        return Contexts.instance().getPreference().isOpenTestsDesktop();
    }

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();

        label = i18n.string(R.string.dt_tests);
        
        DesktopItem dt = null;
        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance(); 
                ctx.getMasterDataProvider().reset();
            }
        }, "Reset Master Dataprovider", R.drawable.dtitem_test);
        
        addItem(dt);
        
        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance(); 
                Intent intent = null;
                intent = new Intent(activity,BookMgntActivity.class);
                activity.startActivityForResult(intent,0);
            }
        }, "Book Management", R.drawable.dtitem_test);
        
        addItem(dt);
        
        
        dt = new DesktopItem(new Runnable() {
            public void run() {
                Contexts ctx = Contexts.instance(); 
                Book book = ctx.getMasterDataProvider().findBook(ctx.getWorkingBookId());
                
                Intent intent = null;
                intent = new Intent(activity,BookEditorActivity.class);
                intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE,false);
                intent.putExtra(BookEditorActivity.PARAM_BOOK,book);
                activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
            }
        }, "Edit selected book", R.drawable.dtitem_test);
        
        addItem(dt);
        
        dt = new DesktopItem(new Runnable() {
            public void run() {
                Book book = new Book("test","$",SymbolPosition.AFTER,"");
                Intent intent = null;
                intent = new Intent(activity,BookEditorActivity.class);
                intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE,true);
                intent.putExtra(BookEditorActivity.PARAM_BOOK,book);
                activity.startActivityForResult(intent,Constants.REQUEST_BOOK_EDITOR_CODE);
            }
        }, "Add book", R.drawable.dtitem_test);
        
        addItem(dt);
        
        
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                Contexts.instance().getDataProvider().reset();
                GUIs.shortToast(activity,"reset data provider");
            }}, "rest data provider",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testFirstDayOfWeek();
            }}, "first day of week",R.drawable.dtitem_test){
        });
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testBusy(200,null);
            }}, "Busy 200ms",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testBusy(200, "error short");
            }}, "Busy 200ms Error",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testBusy(5000,null);
            }}, "Busy 5s",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testBusy(5000, "error long");
            }}, "Busy 5s Error",R.drawable.dtitem_test));
        
        
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testCreateTestdata(25);
            }}, "test data25",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testCreateTestdata(50);
            }}, "test data50",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testCreateTestdata(100);
            }}, "test data100",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testCreateTestdata(200);
            }}, "test data200",R.drawable.dtitem_test));
        addItem(new DesktopItem(new Runnable(){
            @Override
            public void run() {
                testJust();
            }}, "just test",R.drawable.dtitem_test));
        
        DesktopItem padding = new DesktopItem(new Runnable(){
            @Override
            public void run() {
                
            }}, "padding",R.drawable.dtitem_test);
        
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
    
    protected void testBusy(final long i, final String error) {
        GUIs.doBusy(activity,new GUIs.BusyAdapter(){
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity,"task finished");
            }
            public void onBusyError(Throwable x) {
                GUIs.shortToast(activity,"Error "+x.getMessage());
            }
            @Override
            public void run() {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(error!=null){
                    throw new RuntimeException(error);
                }
            }});
    }

    protected void testFirstDayOfWeek() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        for(int i=0;i<100;i++){
            Date now = new Date();
            Date start = calHelper.weekStartDate(now);
            Date end = calHelper.weekEndDate(now);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
//            System.out.println("1>>>>>>>>>>> "+now);
            System.out.println("2>>>>>>>>>>> "+start);
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
        GUIs.doBusy(activity,new GUIs.BusyAdapter(){
            @Override
            public void onBusyFinish() {
                GUIs.shortToast(activity,"create test data");
            }
            @Override
            public void run() {
                I18N i18n = Contexts.instance().getI18n();
                IDataProvider idp = Contexts.instance().getDataProvider();
                new DataCreator(idp,i18n).createTestData(loop);
            }});
        
    }


    protected void testJust() {
        CalendarHelper calHelper = Contexts.instance().getCalendarHelper();
        Date now = new Date();
        Date start = calHelper.weekStartDate(now);
        Date end = calHelper.weekEndDate(now);
        System.out.println(">>>>>>>>>>>>>>> "+now);
        System.out.println("1>>>>>>>>>>> "+now);
        System.out.println("2>>>>>>>>>>> "+start);
        System.out.println("3>>>>>>>>>>> "+end);
        System.out.println(">>>>>>>>>>>>>> "+now);
        
    }

}
