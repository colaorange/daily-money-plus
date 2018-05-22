package com.colaorange.dailymoney.core.ui.nav;

import android.content.Intent;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.legacy.AccountMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BalanceMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BookMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.DataMaintenanceActivity;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuHeader;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuDivider;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuItem;
import com.colaorange.dailymoney.core.ui.legacy.RecordEditorActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordMgntActivity;
import com.colaorange.dailymoney.core.ui.pref.PrefsActivity;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.List;

/**
 * Created by Dennis
 */
public class NavMenuHelper {



    ContextsActivity activity;

    public NavMenuHelper(ContextsActivity activity) {
        this.activity = activity;
    }

    public void reload(List<NavMenuAdapter.NavMenuObj> navMenuList) {
        I18N i18n = Contexts.instance().getI18n();

        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_addrec), new DoPageListener(NavPage.RECORD_EDITOR), getPageIcon(NavPage.RECORD_EDITOR)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_record)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_daily_list), new DoPageListener(NavPage.DAILY_LIST), getPageIcon(NavPage.DAILY_LIST)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_weekly_list), new DoPageListener(NavPage.WEEKLY_LIST), getPageIcon(NavPage.WEEKLY_LIST)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_monthly_list), new DoPageListener(NavPage.MONTHLY_BALANCE), getPageIcon(NavPage.MONTHLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_yearly_list), new DoPageListener(NavPage.YEARLY_BALANCE), getPageIcon(NavPage.YEARLY_BALANCE)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_accmgnt), new DoPageListener(NavPage.ACCOUNT_MGNT), getPageIcon(NavPage.ACCOUNT_MGNT)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_books), new DoPageListener(NavPage.BOOK_MGNT), getPageIcon(NavPage.BOOK_MGNT)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_reports)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_monthly_balance), new DoPageListener(NavPage.MONTHLY_BALANCE), getPageIcon(NavPage.MONTHLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_yearly_balance), new DoPageListener(NavPage.YEARLY_BALANCE), getPageIcon(NavPage.YEARLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_cumulative_balance), new DoPageListener(NavPage.CUMULATIVE_BALANCE), getPageIcon(NavPage.CUMULATIVE_BALANCE)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_datamain), new DoPageListener(NavPage.DATA_MAIN), getPageIcon(NavPage.DATA_MAIN)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_prefs), new DoPageListener(NavPage.PREFS), getPageIcon(NavPage.PREFS)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_what_is_new), new DoPageListener(NavPage.WHATISNEW), getPageIcon(NavPage.WHATISNEW)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_history), new DoPageListener(NavPage.HISTORY), getPageIcon(NavPage.HISTORY)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_contributor), new DoPageListener(NavPage.CONTRIBUTOR), getPageIcon(NavPage.CONTRIBUTOR)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_how2use), new DoPageListener(NavPage.HOW2USE), getPageIcon(NavPage.HOW2USE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_about), new DoPageListener(NavPage.ABOUT), getPageIcon(NavPage.ABOUT)));
    }

    public class DoPageListener implements View.OnClickListener {
        NavPage page;

        public DoPageListener(NavPage page) {
            this.page = page;
        }

        @Override
        public void onClick(View v) {
            doPage(page);
        }
    }

    public int getPageIcon(NavPage page) {
        switch (page) {
            case RECORD_EDITOR:
                return R.drawable.dtitem_add_record;
            case DAILY_LIST:
                return R.drawable.dtitem_detail_day;
            case WEEKLY_LIST:
                return R.drawable.dtitem_detail_week;
            case MONTHLY_LIST:
                return R.drawable.dtitem_detail_month;
            case YEARLY_LIST:
                return R.drawable.dtitem_detail_year;
            case ACCOUNT_MGNT:
                return R.drawable.dtitem_account;
            case BOOK_MGNT:
                return R.drawable.dtitem_books;
            case DATA_MAIN:
                return R.drawable.dtitem_datamain;
            case PREFS:
                return R.drawable.dtitem_prefs;
            case HOW2USE:
                return R.drawable.dtitem_how2use;
            case MONTHLY_BALANCE:
                return R.drawable.dtitem_balance_month;
            case YEARLY_BALANCE:
                return R.drawable.dtitem_balance_year;
            case CUMULATIVE_BALANCE:
                return R.drawable.dtitem_balance_cumulative_month;
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
                intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_DAY);
                break;
            case WEEKLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_WEEK);
                break;
            case MONTHLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_MONTH);
                break;
            case YEARLY_LIST:
                intent = new Intent(activity, RecordMgntActivity.class);
                intent.putExtra(RecordMgntActivity.ARG_MODE, RecordMgntActivity.MODE_YEAR);
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
                intent.putExtra(BalanceMgntActivity.ARG_TOTAL_MODE, false);
                intent.putExtra(BalanceMgntActivity.ARG_MODE, BalanceMgntActivity.MODE_MONTH);
                break;
            case YEARLY_BALANCE:
                intent = new Intent(activity, BalanceMgntActivity.class);
                intent.putExtra(BalanceMgntActivity.ARG_TOTAL_MODE, false);
                intent.putExtra(BalanceMgntActivity.ARG_MODE, BalanceMgntActivity.MODE_YEAR);
                break;
            case CUMULATIVE_BALANCE:
                intent = new Intent(activity, BalanceMgntActivity.class);
                intent.putExtra(BalanceMgntActivity.ARG_TOTAL_MODE, true);
                intent.putExtra(BalanceMgntActivity.ARG_MODE, BalanceMgntActivity.MODE_MONTH);
                break;
            default:
                Logger.w("Unknow page {}", page);
        }
        if (intent != null) {
            activity.startActivity(intent);
        }
    }
}
