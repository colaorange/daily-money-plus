package com.colaorange.dailymoney.core.ui.nav;

import android.content.Intent;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.legacy.RecordMigratorActivity;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuHeader;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuDivider;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter.NavMenuItem;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List;

/**
 * Created by Dennis
 */
public class NavMenuHelper {



    ContextsActivity activity;
    NavPageFacade facade;

    public NavMenuHelper(ContextsActivity activity) {
        this.activity = activity;
        this.facade = new NavPageFacade(activity);
    }

    public void reload(List<NavMenuAdapter.NavMenuObj> navMenuList) {
        I18N i18n = Contexts.instance().getI18n();

        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.RECORD_EDITOR), new DoPageListener(NavPage.RECORD_EDITOR), facade.getPageIcon(NavPage.RECORD_EDITOR)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_record)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.DAILY_LIST), new DoPageListener(NavPage.DAILY_LIST), facade.getPageIcon(NavPage.DAILY_LIST)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.WEEKLY_LIST), new DoPageListener(NavPage.WEEKLY_LIST), facade.getPageIcon(NavPage.WEEKLY_LIST)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.MONTHLY_LIST), new DoPageListener(NavPage.MONTHLY_LIST), facade.getPageIcon(NavPage.MONTHLY_LIST)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.YEARLY_LIST), new DoPageListener(NavPage.YEARLY_LIST), facade.getPageIcon(NavPage.YEARLY_LIST)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.RECORD_SEARCHER), new DoPageListener(NavPage.RECORD_SEARCHER), facade.getPageIcon(NavPage.RECORD_SEARCHER)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.ACCOUNT_MGNT), new DoPageListener(NavPage.ACCOUNT_MGNT), facade.getPageIcon(NavPage.ACCOUNT_MGNT)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.BOOK_MGNT), new DoPageListener(NavPage.BOOK_MGNT), facade.getPageIcon(NavPage.BOOK_MGNT)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_reports)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.MONTHLY_BALANCE), new DoPageListener(NavPage.MONTHLY_BALANCE), facade.getPageIcon(NavPage.MONTHLY_BALANCE)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.YEARLY_BALANCE), new DoPageListener(NavPage.YEARLY_BALANCE), facade.getPageIcon(NavPage.YEARLY_BALANCE)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.FROM_BEGINNING_BALANCE), new DoPageListener(NavPage.FROM_BEGINNING_BALANCE), facade.getPageIcon(NavPage.FROM_BEGINNING_BALANCE)));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.DATA_MAIN), new DoPageListener(NavPage.DATA_MAIN), facade.getPageIcon(NavPage.DATA_MAIN)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.PREFS), new DoPageListener(NavPage.PREFS), facade.getPageIcon(NavPage.PREFS)));

        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader(i18n.string(R.string.label_zone_ring)));
        navMenuList.add(new NavMenuItem(i18n.string(R.string.label_migrate_records), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMigrateRecords();
            }
        }, -1));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.GOOGLE_DRIVE), new DoPageListener(NavPage.GOOGLE_DRIVE), facade.getPageIcon(NavPage.GOOGLE_DRIVE)));

        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.WHATISNEW), new DoPageListener(NavPage.WHATISNEW), facade.getPageIcon(NavPage.WHATISNEW)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.HISTORY), new DoPageListener(NavPage.HISTORY), facade.getPageIcon(NavPage.HISTORY)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.CONTRIBUTOR), new DoPageListener(NavPage.CONTRIBUTOR), facade.getPageIcon(NavPage.CONTRIBUTOR)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.HOW2USE), new DoPageListener(NavPage.HOW2USE), facade.getPageIcon(NavPage.HOW2USE)));
        navMenuList.add(new NavMenuItem(facade.getPageText(NavPage.ABOUT), new DoPageListener(NavPage.ABOUT), facade.getPageIcon(NavPage.ABOUT)));
    }

    private void doMigrateRecords() {
        Intent intent = new Intent(activity, RecordMigratorActivity.class);
        activity.startActivity(intent);
    }

    public class DoPageListener implements View.OnClickListener {
        NavPage page;

        public DoPageListener(NavPage page) {
            this.page = page;
        }

        @Override
        public void onClick(View v) {
            facade.doPage(page);
        }
    }
}
