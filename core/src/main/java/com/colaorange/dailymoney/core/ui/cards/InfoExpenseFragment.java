package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.Date;
import java.util.List;

/**
 * @author dennis
 */
public class InfoExpenseFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_CARDS_POS = "cardsPos";
    public static final String ARG_POS = "pos";

    protected int pos;
    protected int cardPos;

    protected Card card;

    protected View rootView;
    private Toolbar vToolbar;
    private View vContent;

    private TextView vInfoWeeklyExpense;
    private TextView vInfoMonthlyExpense;
    private TextView vInfoCumulativeCash;

    protected I18N i18n;

    protected boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_info_expanse_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadView();
    }


    protected void initArgs() {
        Bundle args = getArguments();
        cardPos = args.getInt(ARG_CARDS_POS, 0);
        pos = args.getInt(ARG_POS, 0);
    }

    protected void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        vToolbar = rootView.findViewById(R.id.card_toolbar);
        vContent = rootView.findViewById(R.id.card_content);

        vInfoWeeklyExpense = rootView.findViewById(R.id.desktop_weekly_expense);
        vInfoMonthlyExpense = rootView.findViewById(R.id.desktop_monthly_expense);
        vInfoCumulativeCash = rootView.findViewById(R.id.desktop_cumulative_cash);

        i18n = Contexts.instance().getI18n();

    }

    protected int getMenuId() {
        return R.menu.card_info_expense_menu;
    }

    protected void reloadView() {
        Preference preference = preference();
        card = preference.getCards(cardPos).get(pos);

        if (vToolbar != null) {
            vToolbar.setTitle(card.getTitle());

            vToolbar.getMenu().clear();
            if (preference.isCardsEditMode()) {
                vToolbar.setBackgroundColor(getContextsActivity().resolveThemeAttrResData(R.attr.appPrimaryColor));
                final int menuId = getMenuId();
                if (menuId > 0) {
                    final Toolbar.OnMenuItemClickListener l = new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return InfoExpenseFragment.this.onMenuItemClick(item);
                        }
                    };

                    vToolbar.inflateMenu(menuId);
                    vToolbar.setOnMenuItemClickListener(l);
                }
            } else {
                vToolbar.setBackgroundColor(getContextsActivity().resolveThemeAttrResData(R.attr.appPrimaryLightColor));
            }
        }

        //don't show content to highlight user , it is edit now
        if (preference.isCardsEditMode()){
            vContent.setVisibility(View.GONE);
            return;
        }
        vContent.setVisibility(View.VISIBLE);

        CalendarHelper calHelper = calendarHelper();

        card = preference.getCards(cardPos).get(pos);


        double b;
        Date now = new Date();
        Date start = calHelper.weekStartDate(now);
        Date end = calHelper.weekEndDate(now);
        AccountType type = AccountType.EXPENSE;

        b = BalanceHelper.calculateBalance(type, start, end).getMoney();
        vInfoWeeklyExpense.setText(i18n.string(R.string.label_weekly_expense, contexts().toFormattedMoneyString(b)));

        start = calHelper.monthStartDate(now);
        end = calHelper.monthEndDate(now);
        b = BalanceHelper.calculateBalance(type, start, end).getMoney();
        vInfoMonthlyExpense.setText(i18n.string(R.string.label_monthly_expense, contexts().toFormattedMoneyString(b)));
        vInfoMonthlyExpense.setVisibility(View.VISIBLE);

        IDataProvider idp = Contexts.instance().getDataProvider();
        List<Account> acl = idp.listAccount(AccountType.ASSET);
        b = 0;
        for (Account ac : acl) {
            if (ac.isCashAccount()) {
                b += BalanceHelper.calculateBalance(ac, null, calHelper.toDayEnd(now)).getMoney();
            }
        }
        vInfoCumulativeCash.setText(i18n.string(R.string.label_cumulative_cash, contexts().toFormattedMoneyString(b)));


    }

    protected boolean onMenuItemClick(MenuItem item) {
        return true;
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.CardFrag.ON_RELOAD_VIEW:
                reloadView();
                break;
        }
    }
}