package com.colaorange.dailymoney.core.ui.cards;

import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.FinalVar;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.util.GUIs;

import java.util.Date;
import java.util.List;

/**
 * @author dennis
 */
public class CardInfoExpenseFragment extends CardBaseFragment implements EventQueue.EventListener {

    private TextView vInfoWeeklyExpense;
    private TextView vInfoMonthlyExpense;
    private TextView vInfoCumulativeCash;

    @Override
    protected void initMembers() {
        super.initMembers();

        vInfoWeeklyExpense = rootView.findViewById(R.id.desktop_weekly_expense);
        vInfoMonthlyExpense = rootView.findViewById(R.id.desktop_monthly_expense);
        vInfoCumulativeCash = rootView.findViewById(R.id.desktop_cumulative_cash);

        i18n = Contexts.instance().getI18n();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.card_info_expanse_frag;
    }
    @Override
    protected int getMenuResId() {
        return R.menu.card_info_expense_menu;
    }

    @Override
    protected boolean doReloadContent(boolean editMode) {
        if(editMode){
            return false;
        }

        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            FinalVar<Double> varWE = new FinalVar<>();
            FinalVar<Double> varME = new FinalVar<>();
            FinalVar<Double> varC = new FinalVar<>();

            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();

                Date now = new Date();
                Date start = calHelper.weekStartDate(now);
                Date end = calHelper.weekEndDate(now);

                double weeklyExpense = BalanceHelper.calculateBalance(AccountType.EXPENSE, start, end).getMoney();

                start = calHelper.monthStartDate(now);
                end = calHelper.monthEndDate(now);
                double monthlyExpense = BalanceHelper.calculateBalance(AccountType.EXPENSE, start, end).getMoney();

                IDataProvider idp = Contexts.instance().getDataProvider();
                List<Account> acl = idp.listAccount(AccountType.ASSET);
                double cash = 0;
                for (Account ac : acl) {
                    if (ac.isCashAccount()) {
                        cash += BalanceHelper.calculateBalance(ac, null, calHelper.toDayEnd(now)).getMoney();
                    }
                }

                varWE.value = weeklyExpense;
                varME.value = monthlyExpense;
                varC.value = cash;
            }

            @Override
            public void onAsyncFinish() {
                vInfoWeeklyExpense.setText(i18n.string(R.string.label_weekly_expense, contexts().toFormattedMoneyString(varWE.value)));
                vInfoMonthlyExpense.setText(i18n.string(R.string.label_monthly_expense, contexts().toFormattedMoneyString(varME.value)));
                vInfoCumulativeCash.setText(i18n.string(R.string.label_cumulative_cash, contexts().toFormattedMoneyString(varC.value)));
            }
        });

        return true;
    }
}