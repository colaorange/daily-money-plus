package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.dailymoney.core.ui.pref.PrefsActivity;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;

/**
 * @author dennis
 */
public class MainDesktop extends AbstractDesktop {
    public static final String NAME = "primary";

    public MainDesktop(Activity activity) {
        super(NAME, activity);
    }

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();
        label = i18n.string(R.string.dt_main);

        DesktopItem addrec = new DesktopItem(new Runnable() {
            public void run() {
                Intent intent = null;
                intent = new Intent(activity, RecordEditorActivity.class);
                intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
                activity.startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
            }
        }, i18n.string(R.string.dtitem_addrec), R.drawable.dtitem_add_record, true, false, 999);

        Intent intent = new Intent(activity, RecordMgntActivity.class);
        intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_DAY);
        DesktopItem daylist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_daily_list), R.drawable.dtitem_detail_day);

        intent = new Intent(activity, RecordMgntActivity.class);
        intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_WEEK);
        DesktopItem weeklist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_weekly_list), R.drawable.dtitem_detail_week);

        intent = new Intent(activity, RecordMgntActivity.class);
        intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_MONTH);
        DesktopItem monthlist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_monthly_list), R.drawable.dtitem_detail_month);

        intent = new Intent(activity, RecordMgntActivity.class);
        intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_YEAR);
        DesktopItem yearlist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_yearly_list), R.drawable.dtitem_detail_year);

        DesktopItem accmgntdt = new DesktopItem(new ActivityRun(activity, AccountMgntActivity.class),
                i18n.string(R.string.dtitem_accmgnt), R.drawable.dtitem_account);

        DesktopItem bookmgntdt = new DesktopItem(new ActivityRun(activity, BookMgntActivity.class),
                i18n.string(R.string.dtitem_books), R.drawable.dtitem_books);

        DesktopItem datamaindt = new DesktopItem(new ActivityRun(activity, DataMaintenanceActivity.class),
                i18n.string(R.string.dtitem_datamain), R.drawable.dtitem_datamain);

        DesktopItem prefdt = new DesktopItem(new ActivityRun(activity, PrefsActivity.class),
                i18n.string(R.string.dtitem_prefs), R.drawable.dtitem_prefs);


        addItem(addrec);
        addItem(daylist);
        addItem(weeklist);
        addItem(monthlist);
        addItem(yearlist);
        addItem(accmgntdt);
        addItem(bookmgntdt);
        addItem(datamaindt);
        addItem(prefdt);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_how2use);
        intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_how2use));
        DesktopItem how2use = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_how2use), R.drawable.dtitem_how2use, true, true, -998);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_about);
        intent.putExtra(LocalWebViewActivity.ARG_TITLE, Contexts.instance().getAppVerName());
        DesktopItem about = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_about), -1, false, true, -999);

        addItem(how2use);
        addItem(about);


        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_what_is_new);
        intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_what_is_new));
        DesktopItem whatisnew = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_what_is_new));

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_contributor);
        intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_contributor));
        DesktopItem contributor = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_contributor));

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_history);
        intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_history));
        DesktopItem history = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_history));


        addItem(contributor);
        addItem(whatisnew);
        addItem(history);


    }

}
