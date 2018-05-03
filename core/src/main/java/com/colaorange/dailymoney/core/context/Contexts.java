package com.colaorange.dailymoney.core.context;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.data.SQLiteDataHelper;
import com.colaorange.dailymoney.core.data.SQLiteDataProvider;
import com.colaorange.dailymoney.core.data.SQLiteMasterDataHelper;
import com.colaorange.dailymoney.core.data.SQLiteMasterDataProvider;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Helps me to do some quick access in context/ui thread
 *
 * @author dennis
 */
public class Contexts {

    //shold't not modify it, code has some assumption.
    public static final int DEFAULT_BOOK_ID = 0;

    private static Contexts instance;

    private ContextsApp contextsApp;
    private String appId;
    private String appVerName;
    private int appVerCode;

    private IDataProvider dataProvider;
    private IMasterDataProvider masterDataProvider;
    private I18N i18n;

    private static final int ANALYTICS_DISPATH_DELAY = 60;// dispatch queue at least 60s

    private GoogleAnalytics sAnalytics;
    private Tracker sTracker;

    private String currencySymbol = "$";

    Preference preference;

    private Contexts() {
    }

    /**
     * get a Contexts instance for activity use
     **/
    static public Contexts instance() {
        if (instance == null) {
            synchronized (Contexts.class) {
                if (instance == null) {
                    instance = new Contexts();
                }
            }
        }
        return instance;
    }

    synchronized boolean initApplication(ContextsApp contextsApp) {

        if (this.contextsApp == null) {
            this.contextsApp = contextsApp;
            appId = contextsApp.getPackageName();
            PackageInfo pi;
            try {
                pi = contextsApp.getPackageManager().getPackageInfo(appId, 0);
                appVerName = pi.versionName;
                appVerCode = pi.versionCode;
            } catch (NameNotFoundException e) {
            }
            Logger.d(">>initialial application context " + appId + "," + appVerName + "," + appVerCode);

            //initial i18n util before any other init
            this.i18n = new I18N(contextsApp);

            preference = new Preference(contextsApp);
            preference.reloadPreference();

            initDataProvider();

            initTracker();
            return true;
        } else {
            Logger.w("application context was initialized :" + contextsApp);
        }
        return false;
    }

    synchronized boolean destroyApplication(ContextsApp contextsApp) {
        if (this.contextsApp != null && this.contextsApp.equals(contextsApp)) {
            cleanTracker();
            cleanDataProvider();
            Logger.d(">>destroyed application context :" + contextsApp);
            this.contextsApp = null;
            return true;
        }
        return false;
    }


    @SuppressLint("MissingPermission")
    private void initTracker() {
        try {
            if (sTracker == null) {
                sAnalytics = GoogleAnalytics.getInstance(contextsApp);
                sTracker = sAnalytics.newTracker(R.xml.ga_tracker);
                sTracker.setAppId(getAppId());
                sTracker.setAppName(i18n.string(R.string.app_code));
                sTracker.setAppVersion(getAppVerName());
            }
        } catch (Throwable t) {
            Logger.e(t.getMessage(), t);
        }
    }

    private void cleanTracker() {
        // Stop the tracker when it is no longer needed.
        try {
            if (sTracker != null) {
                Logger.d("clean google tracker");
                //just leave it.
                sTracker = null;
                sAnalytics = null;
            }
        } catch (Throwable t) {
            Logger.e(t.getMessage(), t);
        }
    }

    public void trackEvent(final String category, final String action, final String label, final Long value) {
        if (preference.isAllowAnalytics() && sTracker != null) {
            try {
                Logger.d("track event " + category + ", " + action);
                sTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(value == null ? 0 : value.longValue()).build());
            } catch (Throwable t) {
                Logger.e(t.getMessage(), t);
            }
        }
    }

    public boolean shareHtmlContent(Activity activity, String subject, String html) {
        return shareHtmlContent(activity, subject, html, null);
    }

    public boolean shareHtmlContent(Activity activity, String subject, String html, List<File> attachments) {
        return shareContent(activity, subject, html, true, attachments);
    }


    public boolean shareTextContent(Activity activity, String subject, String text) {
        return shareTextContent(activity, subject, text, null);
    }

    public boolean shareTextContent(Activity activity, String subject, String text, List<File> attachments) {
        return shareContent(activity, subject, text, false, attachments);
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }


    public boolean shareContent(Activity activity, String subject, String content, boolean htmlContent, List<File> attachments) {
        Intent intent;
        if (attachments == null || attachments.size() <= 1) {
            intent = new Intent(Intent.ACTION_SEND);
        } else {
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        }
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        if (htmlContent) {
            intent.setType("text/html");
            //Key android.intent.extra.TEXT expected ArrayList<CharSequence> but value was a java.lang.String.  The default value <null> was returned.
            ArrayList<CharSequence> arr = new ArrayList<>();
            arr.add(Html.fromHtml(content));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, arr);
        } else {
            intent.setType("text/plain");
            //Key android.intent.extra.TEXT expected ArrayList<CharSequence> but value was a java.lang.String.  The default value <null> was returned.
            ArrayList<CharSequence> arr = new ArrayList<>();
            arr.add(content);
            intent.putExtra(android.content.Intent.EXTRA_TEXT, arr);
        }

        ArrayList<Parcelable> parcels = new ArrayList<Parcelable>();
        if (attachments != null) {
            for (File f : attachments) {
                parcels.add(Uri.fromFile(f));
            }
        }

        if (parcels.size() == 1) {
            intent.putExtra(Intent.EXTRA_STREAM, parcels.get(0));
        } else if (parcels.size() > 1) {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, parcels);
        }
        try {
            activity.startActivity(Intent.createChooser(intent, i18n.string(R.string.label_share)));
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            return false;
        }
        return true;
    }

    /**
     * return true is this is first time you call this api in this application.
     * note that, when calling this twice, it returns false.
     */
    public boolean getAndSetFirstTime() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            if (!prefs.contains("app_firsttime")) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("app_firsttime", Formats.normalizeDate2String(new Date()));
                editor.commit();
                return true;
            }
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        return false;
    }

    /**
     * return true is this is first time you call this api in this application and current version
     */
    public boolean getAndSetFirstVersionTime() {
        int curr = getAppVerCode();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            int last = prefs.getInt("app_lastver", -1);
            if (curr != last) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("app_lastver", curr);
                editor.commit();
                return true;
            }
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        return false;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppVerName() {
        return appVerName;
    }

    public int getAppVerCode() {
        return appVerCode;
    }


    public I18N getI18n() {
        return i18n;
    }

    public CalendarHelper getCalendarHelper() {
        return getPreference().getCalendarHelper();
    }

    public Preference getPreference() {
        return preference;
    }

    /**
     * to reset a deat provider for a book
     **/
    public boolean deleteData(Book book) {
        //can't delete default(0) and working book
        if (book.getId() == DEFAULT_BOOK_ID || book.getId() == preference.getWorkingBookId()) {
            return false;
        }
        String dbname = "dm_" + book.getId() + ".db";
        boolean r = contextsApp.deleteDatabase(dbname);
        return r;
    }

    public void refreshDataProvider() {
        cleanDataProvider();
        initDataProvider();
    }

    public int getWorkingBookId() {
        return preference.getWorkingBookId();
    }

    public void setWorkingBookId(int id) {
        int oid = preference.getWorkingBookId();
        if (id < 0) {
            id = Contexts.DEFAULT_BOOK_ID;
        }
        if (oid != id) {
            preference.setWorkingBookId(id);
            refreshDataProvider();
        }
    }

    public void reloadPreference() {
        int oid = preference.getWorkingBookId();
        preference.reloadPreference();
        if (oid != preference.getWorkingBookId()) {
            refreshDataProvider();
        }
    }


    /**
     * new a dataprovider for a book, the caller has to destroy after using it.
     */
    public IDataProvider newDataProvider(int bookId) {
        CalendarHelper calHelper = getCalendarHelper();

        String dbname = "dm.db";
        if (bookId > DEFAULT_BOOK_ID) {
            dbname = "dm_" + bookId + ".db";
        }
        IDataProvider provider = new SQLiteDataProvider(new SQLiteDataHelper(contextsApp, dbname), calHelper);
        provider.init();

        Logger.d("newDataProvider : {}", provider);

        return provider;
    }


    private void initDataProvider() {

        int bookid = getWorkingBookId();
        CalendarHelper calHelper = getCalendarHelper();
        dataProvider = newDataProvider(bookid);

        Logger.d("initDataProvider {}", dataProvider);

        String dbname = "dm_master.db";
        masterDataProvider = new SQLiteMasterDataProvider(new SQLiteMasterDataHelper(contextsApp, dbname), calHelper);
        masterDataProvider.init();

        Logger.d("initMasterDataProvider {}", masterDataProvider);

        //create selected book if not exist;

        Book book = masterDataProvider.findBook(bookid);
        if (book == null) {
            String name = i18n.string(R.string.title_book) + bookid;
            book = new Book(name, i18n.string(R.string.label_default_book_symbol), SymbolPosition.FRONT, "");
            masterDataProvider.newBookNoCheck(bookid, book);
        }
        currencySymbol = book.getSymbol();
    }


    private void cleanDataProvider() {
        if (dataProvider != null) {

            Logger.d("cleanDataProvider {}", dataProvider);

            dataProvider.destroyed();
            dataProvider = null;
        }

        if (masterDataProvider != null) {

            Logger.d("cleanMasterDataProvider {}", masterDataProvider);

            masterDataProvider.destroyed();
            masterDataProvider = null;
        }
    }

    public int getOrientation() {
        if (contextsApp == null) {
            return Configuration.ORIENTATION_UNDEFINED;
        }
        return contextsApp.getResources().getConfiguration().orientation;
    }

    public IDataProvider getDataProvider() {
        if (dataProvider == null) {
            throw new IllegalStateException("no available dataProvider, did you get data provider out of life cycle");
        }
        return dataProvider;
    }

    public IMasterDataProvider getMasterDataProvider() {
        if (masterDataProvider == null) {
            throw new IllegalStateException("no available dataProvider, did you get data provider out of life cycle");
        }
        return masterDataProvider;
    }

    public Drawable getDrawable(int id) {
        return contextsApp.getResources().getDrawable(id);
    }

    public String toFormattedMoneyString(double money) {
        IMasterDataProvider imdp = getMasterDataProvider();
        Book book = imdp.findBook(preference.getWorkingBookId());
        return SymbolPosition.money2String(money, book.getSymbol(), book.getSymbolPosition());
    }


    private File getStorageFolder() {
        File f = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            f = Environment.getExternalStorageDirectory();
            Logger.d("storage:external " + f);
        } else {
            f = contextsApp.getFilesDir();
            Logger.d("storage:default " + f);
        }

        if (f != null && !f.exists()) {
            Logger.w("app storage folder " + f + " doest not exist");
        }
        return f;
    }

    public boolean hasWorkingFolderPermission() {
        try {
            File touch = new File(getWorkingFolder(), Strings.randomName(10) + ".touch");
            Files.saveString("", touch, "utf-8");
            touch.delete();
            return true;
        } catch (Exception x) {
            return false;
        }
    }

    public File getWorkingFolder() {
        File f = new File(getStorageFolder(), "bwDailyMoney");
        if (!f.exists()) {
            f.mkdir();
        }
        return f;
    }

    public File getAppDbFolder() {
        File f = new File(Environment.getDataDirectory(), "/data/" + appId + "/databases");
        if (!f.exists()) {
            Logger.w("app db folder " + f + " doest not exist");
        }
        return f;

    }

    public File getAppPrefFolder() {
        File f = new File(Environment.getDataDirectory(), "/data/" + appId + "/shared_prefs");
        if (!f.exists()) {
            Logger.w("app pref folder " + f + " doest not exist");
        }
        return f;
    }

    public ContextsApp getApp() {
        return contextsApp;
    }

    @SuppressWarnings("rawtypes")
    public static String getTrackerPath(Class clz) {
        String name = clz.getSimpleName();
        String pkg = clz.getPackage() == null ? "" : clz.getPackage().getName();
        StringBuilder sb = new StringBuilder("/a/");
        int i;
        if ((i = pkg.lastIndexOf('.')) != -1) {
            pkg = pkg.substring(i + 1);
        }
        sb.append(pkg).append(".").append(name);
        return sb.toString();
    }



    public interface TE {
        String CREATE_BOOK = "cb-";
        String CREATE_ACCOUNT = "ca-";
        String CREATE_RECORD = "ce-";

        String UPDDATE_BOOK = "ub-";
        String UPDDATE_ACCOUNT = "ua-";
        String UPDDATE_RECORD = "ur-";

        String DELETE_BOOK = "cb-";
        String DELETE_ACCOUNT = "ca-";
        String DELETE_RECORD = "cr-";

        String BALANCE = "bala-";
        String RECORD_LIST = "recl-";
        String PREFENCE = "pref-";
        String FIRST_TIME = "firt-";
        String EXPORT = "expo-";
        String BACKUP = "baku-";
        String RESTORE = "rest-";
        String IMPORT = "impo-";
        String SHARE = "share-";
        String PROTECTION = "prot-";
        String STARTUP = "stau-";
        String THEME = "theme-";

        String CHART = "chart-";
        String WEBVIEW = "webv-";

    }
}
