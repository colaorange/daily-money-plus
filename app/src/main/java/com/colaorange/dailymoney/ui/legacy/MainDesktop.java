package com.colaorange.dailymoney.ui.legacy;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.data.Detail;
import com.colaorange.dailymoney.ui.Constants;
import com.colaorange.dailymoney.ui.LocalWebViewActivity;

/**
 * 
 * @author dennis
 *
 */
public class MainDesktop extends AbstractDesktop {

    public MainDesktop(Activity activity) {
        super(activity);
    }

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();
        label = i18n.string(R.string.dt_main);

        DesktopItem adddetdt = new DesktopItem(new Runnable() {
            public void run() {
                Detail d = new Detail("", "", new Date(), 0D, "");
                Intent intent = null;
                intent = new Intent(activity,DetailEditorActivity.class);
                intent.putExtra(DetailEditorActivity.PARAM_MODE_CREATE,true);
                intent.putExtra(DetailEditorActivity.PARAM_DETAIL,d);
                activity.startActivityForResult(intent, Constants.REQUEST_DETAIL_EDITOR_CODE);
            }
        }, i18n.string(R.string.dtitem_adddetail), R.drawable.dtitem_adddetail,999);

        Intent intent = new Intent(activity, DetailListActivity.class);
        intent.putExtra(DetailListActivity.PARAM_MODE, DetailListActivity.MODE_DAY);
        DesktopItem daylist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_detlist_day), R.drawable.dtitem_detail_day);
        
        intent = new Intent(activity, DetailListActivity.class);
        intent.putExtra(DetailListActivity.PARAM_MODE, DetailListActivity.MODE_WEEK);
        DesktopItem weeklist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_detlist_week), R.drawable.dtitem_detail_week);

        intent = new Intent(activity, DetailListActivity.class);
        intent.putExtra(DetailListActivity.PARAM_MODE, DetailListActivity.MODE_MONTH);
        DesktopItem monthlist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_detlist_month), R.drawable.dtitem_detail_month);

        intent = new Intent(activity, DetailListActivity.class);
        intent.putExtra(DetailListActivity.PARAM_MODE, DetailListActivity.MODE_YEAR);
        DesktopItem yearlist = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_detlist_year), R.drawable.dtitem_detail_year);

        DesktopItem accmgntdt = new DesktopItem(new ActivityRun(activity, AccountMgntActivity.class),
                i18n.string(R.string.dtitem_accmgnt), R.drawable.dtitem_account);
        
        DesktopItem bookmgntdt = new DesktopItem(new ActivityRun(activity, BookMgntActivity.class),
                i18n.string(R.string.dtitem_books), R.drawable.dtitem_books);
        
        DesktopItem datamaindt = new DesktopItem(new ActivityRun(activity, DataMaintenanceActivity.class),
                i18n.string(R.string.dtitem_datamain), R.drawable.dtitem_datamain);
        
        DesktopItem prefdt = new DesktopItem(new ActivityRun(activity, PrefsActivity.class),
                i18n.string(R.string.dtitem_prefs), R.drawable.dtitem_prefs);


        addItem(adddetdt);
        addItem(daylist);
        addItem(weeklist);
        addItem(monthlist);
        addItem(yearlist);
        addItem(accmgntdt);
        addItem(datamaindt);
        addItem(prefdt);
        addItem(bookmgntdt);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_how2use);
        intent.putExtra(LocalWebViewActivity.PARAM_TITLE, i18n.string(R.string.label_how2use));
        DesktopItem how2use = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_how2use),R.drawable.dtitem_how2use,0);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_about);
        intent.putExtra(LocalWebViewActivity.PARAM_TITLE, Contexts.instance().getAppVerName());
        DesktopItem about = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_about),R.drawable.dtitem_about,0);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_contributor);
        intent.putExtra(LocalWebViewActivity.PARAM_TITLE, i18n.string(R.string.label_contributor));
        DesktopItem contributor = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_contributor),-1,0);
        contributor.setHidden(true);

        intent = new Intent(activity, LocalWebViewActivity.class);
        intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_history);
        intent.putExtra(LocalWebViewActivity.PARAM_TITLE, i18n.string(R.string.label_history));
        DesktopItem history = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.label_history),-1,0);
        history.setHidden(true);
        
        addItem(how2use);
        addItem(contributor);
        addItem(history);
        addItem(about);

    }

}
