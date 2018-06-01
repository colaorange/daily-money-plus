package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Numbers;
import com.colaorange.commons.util.Var;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.util.GUIs;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author dennis
 */
public class PieAccountFragment extends ChartBaseFragment<PieChart> {

    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_BASE_DATE = "baseDate";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";

    protected PeriodMode periodMode;
    protected AccountType accountType;
    protected Date baseDate;
    boolean fromBeginning;

    protected int accountTypeTextColor;

    private static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.WEEKLY, PeriodMode.MONTHLY, PeriodMode.YEARLY);

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.WEEKLY;
        }

        if (!supportPeriod.contains(periodMode)) {
            throw new IllegalStateException("unsupported period " + periodMode);
        }

        accountType = (AccountType) args.getSerializable(ARG_ACCOUNT_TYPE);
        if (accountType == null) {
            accountType = AccountType.EXPENSE;
        }

        baseDate = (Date) args.getSerializable(ARG_BASE_DATE);
        if (baseDate == null) {
            baseDate = new Date();
        }

        fromBeginning = args.getBoolean(ARG_FROM_BEGINNING, false);
    }

    @Override
    protected void initMembers() {
        super.initMembers();
        ContextsActivity activity = getContextsActivity();

        accountTypeTextColor = activity.getAccountTextColorMap().get(accountType);
        vChart.getLegend().setTextColor(accountTypeTextColor);

        vChart.setEntryLabelColor(labelTextColor);
        vChart.setEntryLabelTextSize(labelTextSize - 2);
        vChart.setCenterTextSize(labelTextSize);
        vChart.setCenterTextColor(accountTypeTextColor);
        vChart.setHoleColor(backgroundColor);
        vChart.setHoleRadius(45);
        vChart.setTransparentCircleRadius(55);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.chart_pie_account_type_frag;
    }

    @Override
    public void reloadChart() {
        super.reloadChart();

        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            Var<Double> varBalance = new Var<>();
            List<PieEntry> entries = new LinkedList<>();

            @Override
            public void run() {

                CalendarHelper calHelper = calendarHelper();

                Date start;
                Date end;

                switch (periodMode) {
                    case YEARLY:
                        start = calHelper.yearStartDate(baseDate);
                        end = calHelper.yearEndDate(baseDate);
                        break;
                    case MONTHLY:
                        start = calHelper.monthStartDate(baseDate);
                        end = calHelper.monthEndDate(baseDate);
                        break;
                    case WEEKLY:
                    default:
                        start = calHelper.weekStartDate(baseDate);
                        end = calHelper.weekEndDate(baseDate);
                        break;
                }
                varBalance.value = BalanceHelper.calculateBalance(accountType, fromBeginning ? null : start, end).getMoney();


                List<Balance> list = new ArrayList<>();

                IDataProvider idp = Contexts.instance().getDataProvider();

                for (Account acc : idp.listAccount(accountType)) {
                    Balance balance = BalanceHelper.calculateBalance(acc, fromBeginning ? null : start, end);
                    list.add(balance);
                }

                //remove value is zero
                Iterator<Balance> iter = list.iterator();
                while (iter.hasNext()) {
                    Balance b = iter.next();
                    if (b.getMoney() == 0) {
                        iter.remove();
                    }
                }
                //sort by money
                Collections.sort(list, new Comparator<Balance>() {
                    @Override
                    public int compare(Balance o1, Balance o2) {
                        return Double.compare(o2.getMoney(), o1.getMoney());
                    }
                });

                for (Balance g : list) {
                    entries.add(new PieEntry(new Double(g.getMoney()).floatValue(), g.getName()));
                }

            }

            @Override
            public void onAsyncFinish() {
                String description = "";
                PieDataSet set;

                switch (periodMode) {
                    case YEARLY:
                        description = i18n.string(R.string.label_yearly_value, contexts().toFormattedMoneyString(varBalance.value));
                        break;
                    case MONTHLY:
                        description = i18n.string(R.string.label_monthly_value, contexts().toFormattedMoneyString(varBalance.value));
                        break;
                    case WEEKLY:
                    default:
                        description = i18n.string(R.string.label_weekly_value, contexts().toFormattedMoneyString(varBalance.value));
                        break;
                }
                set = new PieDataSet(entries, "");

                set.setColors(colorTemplate);

                set.setSliceSpace(1f);//space between entry
                set.setValueTextSize(labelTextSize - 4);
                set.setValueTextColor(labelTextColor);
                set.setSelectionShift(10f);//size shift after highlight
                set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                set.setValueLineColor(labelTextColor);


                PieData data = new PieData(set);
                data.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                        StringBuilder sb = new StringBuilder(Numbers.format(v, "#0.##"));
                        if (varBalance.value > 0) {
                            v = (float) (v * 100 / varBalance.value);
                            if (v >= 10) {//only show that > 10
                                sb.append("(").append(Numbers.format(v, "#0.#")).append("%)");
                            }
                        }
                        return sb.toString();
                    }
                });

                if (entries.size() > 0) {
                    vChart.setData(data);
                    vChart.setCenterText(description);
                } else {
                    vChart.setData(null);
                }
                vChart.invalidate(); // refresh
            }
        });

    }

}