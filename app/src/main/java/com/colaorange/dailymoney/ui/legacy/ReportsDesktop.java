package com.colaorange.dailymoney.ui.legacy;

import android.app.Activity;
import android.content.Intent;

import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;

/**
 * 
 * @author dennis
 *
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
        intent.putExtra(BalanceActivity.PARAM_TOTAL_MODE, false);
        intent.putExtra(BalanceActivity.PARAM_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem monthBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_monthly_balance), R.drawable.dtitem_balance_month);
        addItem(monthBalance);
        
        intent = new Intent(activity, BalanceActivity.class);
        intent.putExtra(BalanceActivity.PARAM_TOTAL_MODE, false);
        intent.putExtra(BalanceActivity.PARAM_MODE, BalanceActivity.MODE_YEAR);
        DesktopItem yearBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_yearly_balance), R.drawable.dtitem_balance_year);
        addItem(yearBalance);
        
        intent = new Intent(activity, BalanceActivity.class);
        intent.putExtra(BalanceActivity.PARAM_TOTAL_MODE, true);
        intent.putExtra(BalanceActivity.PARAM_MODE, BalanceActivity.MODE_MONTH);
        DesktopItem totalBalance = new DesktopItem(new IntentRun(activity, intent),
                i18n.string(R.string.dtitem_report_cumulative_balance), R.drawable.dtitem_balance_cumulative_month,99);
        addItem(totalBalance);
    }

}
