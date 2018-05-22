package com.colaorange.dailymoney.core.ui.helper;

import android.content.Intent;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.legacy.AccountMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BalanceMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BookMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.DataMaintenanceActivity;
import com.colaorange.dailymoney.core.ui.legacy.DesktopMgntActivity;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuHeader;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuDivider;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuItem;
import com.colaorange.dailymoney.core.ui.legacy.RecordEditorActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordMgntActivity;
import com.colaorange.dailymoney.core.ui.pref.PrefsActivity;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.List;

/**
 * Created by Dennis
 */
public class NavMenuHelper {
    public static enum Page {
        RECORD_EDITOR,
        DAILY_LIST,
        WEEKLY_LIST,
        MONTHLY_LIST,
        YEARLY_LIST,
        ACCOUNT_MGNT,
        BOOK_MGNT,
        DATA_MAIN,
        PREFS,
        HOW2USE,
        ABOUT,
        CONTRIBUTOR,
        WHATISNEW,
        HISTORY,
        MONTHLY_BALANCE,
        YEARLY_BALANCE,
        CUMULATIVE_BALANCE,
    }


    ContextsActivity activity;

    public NavMenuHelper(ContextsActivity activity) {
        this.activity = activity;
    }

    public void reload(List<NavMenuAdapter.NavMenuObj> navMenuList) {
        I18N i18n = Contexts.instance().getI18n();

        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_addrec), new DoPageListener(Page.RECORD_EDITOR), getPageIcon(Page.RECORD_EDITOR)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_record)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_daily_list), new DoPageListener(Page.DAILY_LIST), getPageIcon(Page.DAILY_LIST)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_weekly_list), new DoPageListener(Page.WEEKLY_LIST), getPageIcon(Page.WEEKLY_LIST)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_monthly_list), new DoPageListener(Page.MONTHLY_BALANCE), getPageIcon(Page.MONTHLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_yearly_list), new DoPageListener(Page.YEARLY_BALANCE), getPageIcon(Page.YEARLY_BALANCE)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_accmgnt), new DoPageListener(Page.ACCOUNT_MGNT), getPageIcon(Page.ACCOUNT_MGNT)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_books), new DoPageListener(Page.BOOK_MGNT), getPageIcon(Page.BOOK_MGNT)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_reports)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_monthly_balance), new DoPageListener(Page.MONTHLY_BALANCE), getPageIcon(Page.MONTHLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_yearly_balance), new DoPageListener(Page.YEARLY_BALANCE), getPageIcon(Page.YEARLY_BALANCE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_report_cumulative_balance), new DoPageListener(Page.CUMULATIVE_BALANCE), getPageIcon(Page.CUMULATIVE_BALANCE)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_datamain), new DoPageListener(Page.DATA_MAIN), getPageIcon(Page.DATA_MAIN)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.dtitem_prefs), new DoPageListener(Page.PREFS), getPageIcon(Page.PREFS)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_what_is_new), new DoPageListener(Page.WHATISNEW), getPageIcon(Page.WHATISNEW)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_history), new DoPageListener(Page.HISTORY), getPageIcon(Page.HISTORY)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_contributor), new DoPageListener(Page.CONTRIBUTOR), getPageIcon(Page.CONTRIBUTOR)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_how2use), new DoPageListener(Page.HOW2USE), getPageIcon(Page.HOW2USE)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_about), new DoPageListener(Page.ABOUT), getPageIcon(Page.ABOUT)));
    }

    public class DoPageListener implements View.OnClickListener {
        Page page;

        public DoPageListener(Page page) {
            this.page = page;
        }

        @Override
        public void onClick(View v) {
            doPage(page);
        }
    }

    public int getPageIcon(Page page) {
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

    public void doPage(Page page) {
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
