package com.colaorange.dailymoney.core.ui.helper;

import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.ui.legacy.DesktopMgntActivity;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuHeader;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuDivider;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter.NavMenuItem;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List; /**
 * Created by Dennis
 */
public class NavMenuListLoader {
    ContextsActivity activity;
    public NavMenuListLoader(ContextsActivity activity) {
        this.activity = activity;
    }

    public void reload(List<NavMenuAdapter.NavMenuObj> navMenuList) {
        I18N i18n = Contexts.instance().getI18n();

        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GUIs.shortToast(activity,"Hello!!");
            }
        };

        navMenuList.add(new NavMenuItem("Add", l, R.drawable.dtitem_add_record));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader("Lists"));
        navMenuList.add(new NavMenuItem("A List", l, R.drawable.dtitem_detail_day));
        navMenuList.add(new NavMenuItem("B List", l, R.drawable.dtitem_detail_week));
        navMenuList.add(new NavMenuItem("C List", l, R.drawable.dtitem_detail_month));
        navMenuList.add(new NavMenuItem("D List", l, R.drawable.dtitem_detail_year));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem("FN1", l, R.drawable.dtitem_account));
        navMenuList.add(new NavMenuItem("FN2", l, R.drawable.dtitem_books));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuHeader("Reports"));
        navMenuList.add(new NavMenuItem("A Report", l, R.drawable.dtitem_balance_month));
        navMenuList.add(new NavMenuItem("B Report", l, R.drawable.dtitem_balance_year));
        navMenuList.add(new NavMenuItem("C Report", l, R.drawable.dtitem_balance_cumulative_month));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem("FN3", l, R.drawable.dtitem_datamain));
        navMenuList.add(new NavMenuItem("FN4", l, R.drawable.dtitem_prefs));
        navMenuList.add(new NavMenuDivider());
        navMenuList.add(new NavMenuItem("FN5", l));
        navMenuList.add(new NavMenuItem("FN6", l));
        navMenuList.add(new NavMenuItem("FN7", l));
        navMenuList.add(new NavMenuItem("FN8", l));
        navMenuList.add(new NavMenuItem("FN9", l));
    }
}
