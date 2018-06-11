package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Collections;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * line chart of individual accounts in an account type
 *
 * @author dennis
 */
public class LineFromBeginningAccountFragment extends ChartBaseFragment<LineChart> {

    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_ACCOUNT = "account";
    public static final String ARG_BASE_DATE = "baseDate";

    private PeriodMode periodMode;
    private AccountType accountType;
    private Account account;
    private Date baseDate;

    protected int accountTypeTextColor;
    private XAxisDateFormatter formatter;

    private DateFormat xAxisFormat;

    private static Set<PeriodMode> supportPeriod = Collections.asSet(PeriodMode.MONTHLY, PeriodMode.YEARLY);

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }

        if (!supportPeriod.contains(periodMode)) {
            throw new IllegalStateException("unsupported period " + periodMode);
        }

        accountType = (AccountType) args.getSerializable(ARG_ACCOUNT_TYPE);

        account = (Account) args.getSerializable(ARG_ACCOUNT);

        if (account == null && accountType == null) {
            throw new IllegalStateException("must have account or account type arg");
        }

        if (account != null) {
            accountType = AccountType.find(account.getType());
        }

        baseDate = (Date) args.getSerializable(ARG_BASE_DATE);
        if (baseDate == null) {
            baseDate = new Date();
        }

    }

    @Override
    protected void initMembers() {
        super.initMembers();
        ContextsActivity activity = getContextsActivity();
        accountTypeTextColor = activity.getAccountTextColorMap().get(accountType);
        if (periodMode == PeriodMode.MONTHLY) {
            xAxisFormat = Contexts.instance().getPreference().getDayFormat();
        } else {
            xAxisFormat = Contexts.instance().getPreference().getNonDigitalMonthFormat();
        }

        vChart.getLegend().setTextColor(accountTypeTextColor);

        //x
        XAxis xAxis = vChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(labelTextColor);
        xAxis.setTextSize(labelTextSize - 4);
        if (periodMode == PeriodMode.MONTHLY) {
            xAxis.setGranularity(Numbers.DAY); // only intervals of 1 day
        } else {
            xAxis.setGranularity(Numbers.DAY * 30); // only intervals of 1 month
        }
        xAxis.setValueFormatter(formatter = new XAxisDateFormatter(xAxisFormat));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //y
        YAxis leftAxis = vChart.getAxisLeft();
        YAxis rightAxis = vChart.getAxisRight();

        leftAxis.setTextColor(accountTypeTextColor);
        leftAxis.setTextSize(labelTextSize - 3);

        rightAxis.setEnabled(false);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.chart_line_account_type_frag;
    }

    @Override
    public void reloadChart() {
        super.reloadChart();
        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            final Map<Account, List<Entry>> entrySeries = new LinkedHashMap<>();

            @Override
            public void run() {

                CalendarHelper calHelper = calendarHelper();

                Date start;
                Date end;
                if (periodMode == PeriodMode.MONTHLY) {
                    start = calHelper.monthStartDate(baseDate);
                    end = calHelper.monthEndDate(baseDate);
                } else {
                    start = calHelper.yearStartDate(baseDate);
                    end = calHelper.yearEndDate(baseDate);
                }

                vChart.getXAxis().setAxisMinimum(start.getTime());
                vChart.getXAxis().setAxisMaximum(end.getTime());

                IDataProvider idp = Contexts.instance().getDataProvider();
                Map<String, Account> accountMap = null;

                List<Account> accounts;
                if (account == null) {
                    accounts = idp.listAccount(accountType);
                } else {
                    accounts = Collections.asList(account);
                }
                accountMap = new LinkedHashMap<>();
                for (Account account : accounts) {
                    accountMap.put(account.getId(), account);
                    entrySeries.put(account, new LinkedList<Entry>());
                }

                for (Account account : accounts) {
                    Date localStart = start;
                    List<Entry> entries = new LinkedList<>();
                    while (localStart.getTime() < end.getTime()) {
                        Date localEnd;
                        if (periodMode == PeriodMode.MONTHLY) {
                            localEnd = calHelper.toDayEnd(localStart);
                        } else {
                            localEnd = calHelper.monthEndDate(localStart);
                        }
                        Balance b = BalanceHelper.calculateBalance(account, null, localEnd);
                        entries.add(new Entry(localStart.getTime(), (float) b.getMoney()));

                        if (periodMode == PeriodMode.MONTHLY) {
                            localStart = calendarHelper().dateAfter(localStart, 1);
                        } else {
                            localStart = calendarHelper().monthAfter(localStart, 1);
                        }
                    }
                    entrySeries.put(account, entries);
                }
            }

            private int nextColor(int i) {
                return colorTemplate[i % colorTemplate.length];
            }

            @Override
            public void onAsyncFinish() {


                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                int i = 0;
                for (Account key : entrySeries.keySet()) {
                    List<Entry> list = entrySeries.get(key);

                    /**
                     * java.lang.IndexOutOfBoundsException
                     at java.util.LinkedList.get(LinkedList.java:519)
                     at com.github.mikephil.charting.data.DataSet.getEntryForIndex(DataSet.java:286)
                     at com.github.mikephil.charting.utils.Transformer.generateTransformedValuesLine(Transformer.java:184)
                     at com.github.mikephil.charting.renderer.LineChartRenderer.drawValues(LineChartRenderer.java:547)
                     at com.github.mikephil.charting.charts.BarLineChartBase.onDraw(BarLineChartBase.java:264)
                     at android.view.View.draw(View.java:15114)
                     */
                    if (list.size() <= 0) {
                        continue;
                    }

                    String label = key.getName();

                    LineDataSet set = new LineDataSet(list, label);

                    set.setValueTextSize(labelTextSize - 4);
                    set.setValueTextColor(labelTextColor);

                    int color;

                    color = nextColor(i++);

                    set.setColors(color);
                    set.setCircleColor(lightTheme ? Colors.darken(color, 0.3f) : Colors.lighten(color, 0.3f));
                    set.setCircleColorHole(lightTheme ? Colors.darken(color, 0.2f) : Colors.lighten(color, 0.2f));

                    dataSets.add(set);
                }

                if (dataSets.size() > 0) {
                    LineData data = new LineData(dataSets);
                    data.setValueFormatter(new MoneyFormatter());
                    vChart.setData(data);
                } else {
                    vChart.setData(null);
                }
                vChart.invalidate(); // refresh
            }
        });
    }

}