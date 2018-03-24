package com.colaorange.dailymoney.context;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Html;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Files;
import com.colaorange.commons.util.Formats;
import com.colaorange.commons.util.I18N;
import com.colaorange.commons.util.Logger;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.Book;
import com.colaorange.dailymoney.data.IDataProvider;
import com.colaorange.dailymoney.data.IMasterDataProvider;
import com.colaorange.dailymoney.data.SQLiteDataHelper;
import com.colaorange.dailymoney.data.SQLiteDataProvider;
import com.colaorange.dailymoney.data.SQLiteMasterDataHelper;
import com.colaorange.dailymoney.data.SQLiteMasterDataProvider;
import com.colaorange.dailymoney.data.SymbolPosition;
import com.colaorange.dailymoney.ui.Constants;
import com.colaorange.dailymoney.ui.DesktopActivity;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Helps me to do some quick access in context/ui thread
 * @author dennis
 *
 */
public class Contexts {

    private static Contexts instance;

    private ContextsApp contextsApp;
    private String appId;
    private String appVerName;
    private int appVerCode;

    private IDataProvider dataProvider;
    private IMasterDataProvider masterDataProvider;
    private I18N i18n;

    int pref_workingBookId = 0;//the book user selected, default is 0
    int pref_detailListLayout = 2;
    int pref_maxRecords = -1;//-1 is no limit
    int pref_firstdayWeek = 1;//sunday
    int pref_startdayMonth = 1;//
    boolean pref_openTestsDesktop = false;
    boolean pref_backupCSV = true;
    String pref_password = "";
    boolean pref_allowAnalytics = true;
    String pref_csvEncoding = "UTF8";
    boolean pref_hierarachicalReport = true;
    String pref_lastbackup = "Unknown";

    private CalendarHelper calendarHelper = new CalendarHelper();

    private static final int ANALYTICS_DISPATH_DELAY = 60;// dispatch queue at least 60s

    private GoogleAnalyticsTracker tracker;

    public static final String TRACKER_EVT_CREATE = "C";
    public static final String TRACKER_EVT_UPDATE = "U";
    public static final String TRACKER_EVT_DELETE = "D";

    private String currencySymbol = "$";

    public static final boolean DEBUG = true;

    private Contexts(){
    }

//    private DateFormat getLastBackupFormat(){
//        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    }

    /** get a Contexts instance for activity use **/
    static public Contexts instance(){
        if(instance == null){
            synchronized(Contexts.class){
                if(instance==null){
                    instance = new Contexts();
                }
            }
        }
        return instance;
    }

    synchronized boolean initApplication(ContextsApp contextsApp){

        if(this.contextsApp==null){
            this.contextsApp = contextsApp;
            appId = contextsApp.getPackageName();
            PackageInfo pi;
            try {
                pi = contextsApp.getPackageManager().getPackageInfo(appId,0);
                appVerName =  pi.versionName;
                appVerCode = pi.versionCode;
            } catch (NameNotFoundException e) {
            }
            Logger.d(">>initialial application context "+appId+","+ appVerName +","+ appVerCode);

            reloadPreference();

            this.i18n = new I18N(contextsApp);
            initDataProvider();
            initTracker();
            return true;
        }else{
            Logger.w("application context was initialized :"+contextsApp);
        }
        return false;
    }

    synchronized boolean destroyApplication(ContextsApp contextsApp){
        if(this.contextsApp!=null && this.contextsApp.equals(contextsApp)){
            cleanTracker();
            cleanDataProvider();
            Logger.d(">>destroyed application context :"+contextsApp);
            this.contextsApp = null;
            return true;
        }
        return false;
    }


    private void initTracker() {
        try {
            if(tracker==null) {
                tracker = GoogleAnalyticsTracker.getInstance();
                tracker.setProductVersion(i18n.string(R.string.app_code), getAppVerName());
                tracker.start(i18n.string(R.string.ga_code), ANALYTICS_DISPATH_DELAY, contextsApp);
            }
        } catch (Throwable t) {
            Logger.e(t.getMessage(), t);
        }
    }

    private void cleanTracker() {
        // Stop the tracker when it is no longer needed.
        try {
            if (tracker != null) {
                //just leave it.
                tracker.dispatch();
                tracker.stop();
                tracker = null;
                Logger.d("clean google tracker");
            }
        } catch (Throwable t) {
            Logger.e(t.getMessage(), t);
        }
    }

    protected void trackEvent(final String category,final String action,final String label,final int value) {
        if (isPrefAllowAnalytics() && tracker != null) {
            try{
                Logger.d("track event " + category +", "+action);
                tracker.trackEvent(category, action, label, value);
            } catch (Throwable t) {
                Logger.e(t.getMessage(), t);
            }
        }
    }

    protected void trackPageView(final String path) {
        if (isPrefAllowAnalytics() && tracker != null) {
            try {
                Logger.d("track " + path);
                tracker.trackPageView(path);
            } catch (Throwable t) {
                Logger.e(t.getMessage(), t);
            }
        }
    }

    public boolean shareHtmlContent(String subject,String html){
        return shareHtmlContent(subject,html,null);
    }
    public boolean shareHtmlContent(String subject,String html,List<File> attachments){
        return shareContent(subject,html,true,attachments);
    }


    public boolean shareTextContent(String subject,String text){
        return shareTextContent(subject,text,null);
    }
    public boolean shareTextContent(String subject,String text,List<File> attachments){
        return shareContent(subject,text,false,attachments);
    }

    public String getCurrencySymbol(){
        return currencySymbol;
    }


    public boolean shareContent(String subject,String content,boolean htmlContent,List<File> attachments){
        Intent intent;
        if(attachments == null || attachments.size()<=1){
            intent = new Intent(Intent.ACTION_SEND);
        }else{
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        }
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        if(htmlContent){
            intent.setType("text/html");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(content));
        }else{
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        }

        ArrayList<Parcelable> parcels = new ArrayList<Parcelable>();
        if (attachments != null) {
            for (File f : attachments) {
                parcels.add(Uri.fromFile(f));
            }
        }

        if(parcels.size()==1){
            intent.putExtra(Intent.EXTRA_STREAM, parcels.get(0));
        }else if(parcels.size()>1){
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, parcels);
        }
        try{
            contextsApp.startActivity(Intent.createChooser(intent, i18n.string(R.string.clabel_share)));
        }catch(Exception x){
            Logger.e(x.getMessage(),x);
            return false;
        }
        return true;
    }

    /**
     * return true is this is first time you call this api in this application.
     * note that, when calling this twice, it returns false. see {@link DesktopActivity#initialApplicationInfo}
     */
    public boolean isFirstTime(){
        try{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            if(!prefs.contains("app_firsttime")){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("app_firsttime",Formats.normalizeDate2String(new Date()));
                editor.commit();
                return true;
            }
        }catch(Exception x){
            Logger.w(x.getMessage(),x);
        }
        return false;
    }

    /**
     * return true is this is first time you call this api in this application and current version
     */
    public boolean isFirstVersionTime(){
        int curr = getAppVerCode();
        try{
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            int last = prefs.getInt("app_lastver",-1);
            if(curr!=last){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("app_lastver",curr);
                editor.commit();
                return true;
            }
        }catch(Exception x){
            Logger.w(x.getMessage(),x);
        }
        return false;
    }

    public String getAppId(){
        return appId;
    }

    public String getAppVerName(){
        return appVerName;
    }

    /**
     * for ui context only
     * @return
     */
    public int getAppVerCode(){
        return appVerCode;
    }

    public void reloadPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);

        int bookid = 0;
        try{
            bookid = prefs.getInt(Constants.PREFS_WORKING_BOOK_ID, pref_workingBookId);
        }catch(Exception x){Logger.e(x.getMessage());}
        if(bookid<0){
            bookid = 0;
        }

        try{
            String pd1  = prefs.getString(Constants.PREFS_PASSWORD, pref_password);
            String pd2  = prefs.getString(Constants.PREFS_PASSWORDVD, pref_password);
            if(pd1.equals(pd2)){
                pref_password = pd1;
            }else{
                pref_password = "";
            }
        }catch(Exception x){Logger.e(x.getMessage());}


        try{
            pref_detailListLayout = Integer.parseInt(prefs.getString(Constants.PREFS_DETAIL_LIST_LAYOUT, String.valueOf(pref_detailListLayout)));
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_firstdayWeek = Integer.parseInt(prefs.getString(Constants.PREFS_FIRSTDAY_WEEK,  String.valueOf(pref_firstdayWeek)));
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_startdayMonth = Integer.parseInt(prefs.getString(Constants.PREFS_STARTDAY_MONTH,  String.valueOf(pref_startdayMonth)));
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_maxRecords = Integer.parseInt(prefs.getString(Constants.PREFS_MAX_RECORDS,String.valueOf(pref_maxRecords)));
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_openTestsDesktop = prefs.getBoolean(Constants.PREFS_OPEN_TESTS_DESKTOP, false);
        }catch(Exception x){Logger.e(x.getMessage());}

        try{
            pref_backupCSV = prefs.getBoolean(Constants.PREFS_BACKUP_CSV, pref_backupCSV);
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_allowAnalytics = prefs.getBoolean(Constants.PREFS_ALLOW_ANALYTICS, pref_allowAnalytics);
        }catch(Exception x){Logger.e(x.getMessage());}
        try{
            pref_csvEncoding = prefs.getString(Constants.PREFS_CSV_ENCODING, pref_csvEncoding);
        }catch(Exception x){Logger.e(x.getMessage());}

        try{
            pref_hierarachicalReport = prefs.getBoolean(Constants.PREFS_HIERARCHICAL_REPORT, pref_hierarachicalReport);
        }catch(Exception x){Logger.e(x.getMessage());}

        try {
            pref_lastbackup = prefs.getString(Constants.PREFS_LAST_BACKUP, pref_lastbackup);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }


        if(pref_workingBookId!=bookid){
            pref_workingBookId = bookid;
            refreshDataProvider();
        }

        if(DEBUG){
            Logger.d("preference : working book "+pref_workingBookId);
            Logger.d("preference : detail layout "+pref_detailListLayout);
            Logger.d("preference : firstday of week "+pref_firstdayWeek);
            Logger.d("preference : startday of month "+pref_startdayMonth);
            Logger.d("preference : max records "+pref_maxRecords);
            Logger.d("preference : open tests desktop "+pref_openTestsDesktop);
            Logger.d("preference : backup csv "+pref_backupCSV);
            Logger.d("preference : csv encoding "+pref_csvEncoding);
            Logger.d("preference : last backup " + pref_lastbackup);
        }
        calendarHelper.setFirstDayOfWeek(getPrefFirstdayWeek());
        calendarHelper.setStartDayOfMonth(getPrefStartdayMonth());
    }

    public int getWorkingBookId(){
        return pref_workingBookId;
    }

    public void setWorkingBookId(int id){
        if(id<0){
            id = 0;
        }
        if(pref_workingBookId!=id) {
            pref_workingBookId = id;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.PREFS_WORKING_BOOK_ID, id);
            editor.commit();

            refreshDataProvider();
        }
    }

    public String getPrefPassword(){
        return pref_password;
    }

    public boolean isPrefAllowAnalytics(){
        return pref_allowAnalytics;
    }

    public String getPrefCSVEncoding(){
        return pref_csvEncoding;
    }

    public boolean isPrefBackupCSV(){
        return pref_backupCSV;
    }

    public boolean isPrefHierarachicalReport(){
        return pref_hierarachicalReport;
    }

    public void setPrefHierarachicalReport(boolean hierarachicalReport){
        pref_hierarachicalReport = hierarachicalReport;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREFS_HIERARCHICAL_REPORT,pref_hierarachicalReport);
        editor.commit();
    }

    public int getPrefDetailListLayout(){
        return pref_detailListLayout;
    }

    public int getPrefMaxRecords(){
        return pref_maxRecords;
    }

    public int getPrefFirstdayWeek(){
        return pref_firstdayWeek;
    }
    public int getPrefStartdayMonth(){
        return pref_startdayMonth>28?28:(pref_startdayMonth<1?1:pref_startdayMonth);
    }

    public boolean isPrefOpenTestsDesktop(){
        return pref_openTestsDesktop;
    }

    public CalendarHelper getCalendarHelper(){
        return calendarHelper;
    }


    public I18N getI18n() {
        return i18n;
    }



    /** to reset a deat provider for a book **/
    public boolean deleteData(Book book){
        //can't delete default(0) and working book
        if(book.getId()==0 || book.getId()==pref_workingBookId){
            return false;
        }
        String dbname = "dm_"+book.getId()+".db";
        boolean r = contextsApp.deleteDatabase(dbname);
        return r;
    }

    public void refreshDataProvider(){
        cleanDataProvider();
        initDataProvider();
    }


    private void initDataProvider() {
        String dbname = "dm.db";
        if(pref_workingBookId>0){
            dbname = "dm_"+pref_workingBookId+".db";
        }
        dataProvider = new SQLiteDataProvider(new SQLiteDataHelper(contextsApp,dbname),calendarHelper);
        dataProvider.init();
        if(DEBUG){
            Logger.d("initDataProvider :"+dataProvider);
        }

        dbname = "dm_master.db";
        masterDataProvider = new SQLiteMasterDataProvider(new SQLiteMasterDataHelper(contextsApp,dbname),calendarHelper);
        masterDataProvider.init();
        if(DEBUG){
            Logger.d("masterDataProvider :"+masterDataProvider);
        }
        //create selected book if not exist;
        int sbid = getWorkingBookId();
        Book book = masterDataProvider.findBook(sbid);
        if(book==null){
            String name = i18n.string(R.string.title_book)+sbid;
            book = new Book(name,i18n.string(R.string.label_default_book_symbol),SymbolPosition.FRONT,"");
            masterDataProvider.newBookNoCheck(getWorkingBookId(), book);
        }
        currencySymbol = book.getSymbol();
    }


    private void cleanDataProvider(){
        if(dataProvider!=null){
            if(DEBUG){
                Logger.d("cleanDataProvider :"+dataProvider);
            }
            dataProvider.destroyed();
            dataProvider = null;
        }

        if(masterDataProvider!=null){
            if(DEBUG){
                Logger.d("cleanMasterDataProvider :"+masterDataProvider);
            }
            masterDataProvider.destroyed();
            masterDataProvider = null;
        }
    }

    public int getOrientation(){
        if(contextsApp==null){
            return Configuration.ORIENTATION_UNDEFINED;
        }
        return contextsApp.getResources().getConfiguration().orientation;
    }

    public IDataProvider getDataProvider(){
        if(dataProvider==null){
            throw new IllegalStateException("no available dataProvider, did you get data provider out of life cycle");
        }
        return dataProvider;
    }

    public IMasterDataProvider getMasterDataProvider(){
        if(masterDataProvider==null){
            throw new IllegalStateException("no available dataProvider, did you get data provider out of life cycle");
        }
        return masterDataProvider;
    }

    public DateFormat getDateFormat(){
        return android.text.format.DateFormat.getDateFormat(contextsApp);
    }

    public DateFormat getLongDateFormat(){
        return android.text.format.DateFormat.getLongDateFormat(contextsApp);
    }

    public DateFormat getMediumDateFormat(){
        return android.text.format.DateFormat.getMediumDateFormat(contextsApp);
    }

    public DateFormat getTimeFormat(){
        return android.text.format.DateFormat.getTimeFormat(contextsApp);
    }
    public Drawable getDrawable(int id){
        return contextsApp.getResources().getDrawable(id);
    }

    public String toFormattedMoneyString(double money){
        IMasterDataProvider imdp = getMasterDataProvider();
        Book book = imdp.findBook(getWorkingBookId());
        return Formats.money2String(money, book.getSymbol(), book.getSymbolPosition());
    }


    private File getStorageFolder() {
        File f = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            f = Environment.getExternalStorageDirectory();
            Logger.d("storage:external "+f);
        }else{
            f = contextsApp.getFilesDir();
            Logger.d("storage:default "+f);
        }

        if(f!=null && !f.exists()){
            Logger.w("app storage folder "+f+" doest not exist");
        }
        return f;
    }

    public boolean hasWorkingFolderPermission(){
        try {
            File touch = new File(getWorkingFolder(), Strings.randomName(10)+".touch");
            Files.saveString("", touch, "utf-8");
            touch.delete();
            return true;
        }catch(Exception x){
            return false;
        }
    }

    public File getWorkingFolder(){
        File f = new File(getStorageFolder(), "bwDailyMoney");
        if(!f.exists()){
            f.mkdir();
        }
        return f;
    }

    public File getAppDbFolder() {
        File f= new File(Environment.getDataDirectory(),"/data/"+appId+"/databases");
        if(!f.exists()){
            Logger.w("app db folder "+f+" doest not exist");
        }
        return f;

    }

    public File getAppPrefFolder() {
        File f = new File(Environment.getDataDirectory(),"/data/"+appId+"/shared_prefs");
        if(!f.exists()){
            Logger.w("app pref folder "+f+" doest not exist");
        }
        return f;
    }

    private DateFormat getLastBackupFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void setPrefLastBackupTime(long date) {
        DateFormat fmt = getLastBackupFormat();
        pref_lastbackup = fmt.format(date);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_LAST_BACKUP, pref_lastbackup);
        editor.commit();
    }

    public String getPrefLastBackup(){
        return pref_lastbackup;
    }

    public Long getPrefLastBackupTime(){
        DateFormat fmt = getLastBackupFormat();
        try{
            return fmt.parse(pref_lastbackup).getTime();
        }catch(Exception x){}
        return null;
    }
}
