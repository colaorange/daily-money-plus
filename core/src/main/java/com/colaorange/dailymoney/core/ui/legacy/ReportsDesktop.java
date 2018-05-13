package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;

/**
 * @author dennis
 */
public class ReportsDesktop extends AbstractDesktop {

    public ReportsDesktop(Activity activity) {
        super(activity);
    }

    @Override
    protected void init() {
        I18N i18n = Contexts.instance().getI18n();

        label = i18n.string(R.string.dt_reports);

        Intent intent = null;

        intent = new Intent(activity, BalanceActivity.class);
        intent.putExtra(BalanceActivity.ARG_TOTAL_MODE, false);
        intent.putExtra(BalanceActivity.ARG_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem monthBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_monthly_balance), R.drawable.dtitem_balance_month, true, false, 199);
        addItem(monthBalance);

        intent = new Intent(activity, BalanceActivity.class);
        intent.putExtra(BalanceActivity.ARG_TOTAL_MODE, false);
        intent.putExtra(BalanceActivity.ARG_MODE, BalanceActivity.MODE_YEAR);
        DesktopItem yearBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_yearly_balance), R.drawable.dtitem_balance_year, true, false, 199);
        addItem(yearBalance);

        intent = new Intent(activity, BalanceActivity.class);
        intent.putExtra(BalanceActivity.ARG_TOTAL_MODE, true);
        intent.putExtra(BalanceActivity.ARG_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem totalBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_cumulative_balance), R.drawable.dtitem_balance_cumulative_month, true,true, 197);
        addItem(totalBalance);
    }

}
