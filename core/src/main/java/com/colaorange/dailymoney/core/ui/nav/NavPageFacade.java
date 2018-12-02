package com.colaorange.dailymoney.core.ui.nav;

import android.content.Intent;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.legacy.AccountMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BalanceMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BookMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.DataMaintenanceActivity;
import com.colaorange.dailymoney.core.ui.legacy.GoogleDriveActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordEditorActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordSearcherActivity;
import com.colaorange.dailymoney.core.ui.pref.PrefsActivity;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Dennis
 */
public class NavPageFacade {

    ContextsActivity activity;

    public NavPageFacade(ContextsActivity activity) {
        this.activity = activity;
    }


    public int getPageIcon(NavPage page) {
        switch (page) {
            case RECORD_EDITOR:
                return R.drawable.nav_pg_add_record;
            case DAILY_LIST:
                return R.drawable.nav_pg_reclist_day;
            case WEEKLY_LIST:
                return R.drawable.nav_pg_reclist_week;
            case MONTHLY_LIST:
                return R.drawable.nav_pg_reclist_month;
            case YEARLY_LIST:
                return R.drawable.nav_pg_reclist_year;
            case ACCOUNT_MGNT:
                return R.drawable.nav_pg_account;
            case BOOK_MGNT:
                return R.drawable.nav_pg_books;
            case DATA_MAIN:
                return R.drawable.nav_pg_datamain;
            case PREFS:
                return R.drawable.nav_pg_prefs;
            case HOW2USE:
                return R.drawable.nav_pg_how2use;
            case MONTHLY_BALANCE:
                return R.drawable.nav_pg_balance_month;
            case YEARLY_BALANCE:
                return R.drawable.nav_pg_balance_year;
            case FROM_BEGINNING_BALANCE:
                return R.drawable.nav_pg_balance_from_beginning_month;
            case RECORD_SEARCHER:
                return R.drawable.nav_pg_search;
            case GOOGLE_DRIVE:
                return R.drawable.nav_pg_google_drive;
            case ABOUT:
            case CONTRIBUTOR:
            case WHATISNEW:
            case HISTORY:
            default:
                return -1;
        }
    }

    public void doPage(NavPage page) {
        I18N i18n = Contexts.instance().getI18n();
        Intent intent = null;
        switch (page) {
            case RECORD_EDITOR:
                intent = new Intent(activity, RecordEditorActivity.class);
                intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
                break;
            case DAILY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_PERIOD_MODE, PeriodMode.DAILY);
                break;
            case WEEKLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_PERIOD_MODE, PeriodMode.WEEKLY);
                break;
            case MONTHLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_PERIOD_MODE, PeriodMode.MONTHLY);
                break;
            case YEARLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_PERIOD_MODE, PeriodMode.YEARLY);
                break;
            case RECORD_SEARCHER:
                intent = new Intent(activity, RecordSearcherActivity.class);
                break;
            case ACCOUNT_MGNT:
                intent = new Intent(activity, AccountMgntActivity.class);
                break;
            case BOOK_MGNT:
                intent = new Intent(activity, BookMgntActivity.class);
                break;
            case DATA_MAIN:
                intent = new Intent(activity, DataMaintenanceActivity.class);
                break;
            case PREFS:
                intent = new Intent(activity, PrefsActivity.class);
                break;
            case HOW2USE:
                intent = new Intent(activity, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_how2use);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_how2use));
                break;
            case ABOUT:
                intent = new Intent(activity, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_about);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, Contexts.instance().getAppVerName());
                break;
            case CONTRIBUTOR:
                intent = new Intent(activity, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_contributor);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_contributor));
                break;
            case WHATISNEW:
                intent = new Intent(activity, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_what_is_new);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_what_is_new));
                break;
            case HISTORY:
                intent = new Intent(activity, LocalWebViewActivity.class);
                intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_history);
                intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n.string(R.string.label_history));
                break;
            case MONTHLY_BALANCE:
                intent = new Intent(activity, BalanceMgntActivity.class);
                intent.putExtra(BalanceMgntActivity.ARG_FROM_BEGINNING, false);
                intent.putExtra(BalanceMgntActivity.ARG_PERIOD_MODE, PeriodMode.MONTHLY);
                break;
            case YEARLY_BALANCE:
                intent = new Intent(activity, BalanceMgntActivity.class);
                intent.putExtra(BalanceMgntActivity.ARG_FROM_BEGINNING, false);
                intent.putExtra(BalanceMgntActivity.ARG_PERIOD_MODE, PeriodMode.YEARLY);
                break;
            case FROM_BEGINNING_BALANCE:
                intent = new Intent(activity, BalanceMgntActivity.class);
                intent.putExtra(BalanceMgntActivity.ARG_FROM_BEGINNING, true);
                intent.putExtra(BalanceMgntActivity.ARG_PERIOD_MODE, PeriodMode.MONTHLY);
                break;
            case GOOGLE_DRIVE:
                intent = new Intent(activity, GoogleDriveActivity.class);
                break;
            default:
                Logger.w("Unknow page {}", page);
        }
        if (intent != null) {
            activity.startActivity(intent);
        }
    }

    public String getPageText(NavPage page) {
        I18N i18n = Contexts.instance().getI18n();
        switch (page) {
            case RECORD_EDITOR:
                return i18n.string(R.string.nav_pg_add_record);
            case DAILY_LIST:
                return i18n.string(R.string.nav_pg_daily_list);
            case WEEKLY_LIST:
                return i18n.string(R.string.nav_pg_weekly_list);
            case MONTHLY_LIST:
                return i18n.string(R.string.nav_pg_monthly_list);
            case YEARLY_LIST:
                return i18n.string(R.string.nav_pg_yearly_list);
            case ACCOUNT_MGNT:
                return i18n.string(R.string.nav_pg_account);
            case BOOK_MGNT:
                return i18n.string(R.string.nav_pg_book);
            case DATA_MAIN:
                return i18n.string(R.string.nav_pg_data_main);
            case PREFS:
                return i18n.string(R.string.nav_pg_prefs);
            case HOW2USE:
                return i18n.string(R.string.label_how2use);
            case MONTHLY_BALANCE:
                return i18n.string(R.string.nav_pg_report_monthly_balance);
            case YEARLY_BALANCE:
                return i18n.string(R.string.nav_pg_report_yearly_balance);
            case FROM_BEGINNING_BALANCE:
                return i18n.string(R.string.nav_pg_report_from_beginning_balance);
            case ABOUT:
                return i18n.string(R.string.label_about);
            case CONTRIBUTOR:
                return i18n.string(R.string.label_contributor);
            case WHATISNEW:
                return i18n.string(R.string.label_what_is_new);
            case HISTORY:
                return i18n.string(R.string.label_history);
            case RECORD_SEARCHER:
                return i18n.string(R.string.label_search);
            case GOOGLE_DRIVE:
                return i18n.string(R.string.label_google_drive);
            default:
                return i18n.string(R.string.label_unknown);
        }
    }

    public List<NavPage> listPrimary() {
        List<NavPage> l = new LinkedList<>();
        for (NavPage p : NavPage.values()) {
            if (getPageIcon(p) > 0) {
                l.add(p);
            }
        }
        return l;
    }
}
