package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Collections;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.CalculationMode;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * line chart of individual accounts in an account type
 *
 * @author dennis
 */
public class LineAccountFragment extends PeriodModeChartBaseFragment<LineChart> {

    public static final String ARG_CALCULATION_MODE = "calculationMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_ACCOUNT = "account";

    private CalculationMode calculationMode;
    private AccountType accountType;
    private Account account;

    protected int accountTypeTextColor;
    private XAxisDateFormatter formatter;

    private DateFormat xAxisFormat;

    private static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.MONTHLY, PeriodMode.YEARLY);

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        if (!supportPeriod.contains(periodMode)) {
            throw new IllegalStateException("unsupported period " + periodMode);
        }

        calculationMode = (CalculationMode) args.getSerializable(ARG_CALCULATION_MODE);
        if (calculationMode == null) {
            calculationMode = calculationMode.CUMULATIVE;
        }

        accountType = (AccountType) args.getSerializable(ARG_ACCOUNT_TYPE);

        account = (Account) args.getSerializable(ARG_ACCOUNT);

        if (account == null && accountType == null) {
            throw new IllegalStateException("must have account or account type arg");
        }

        if (account != null) {
            accountType = AccountType.find(account.getType());
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
        return R.layout.chart_line_period_frag;
    }

    @Override
    public void reloadChart() {
        super.reloadChart();
        GUIs.doAsync(getContextsActivity(), new ChartLoading() {

            final Map<Account, TreeMap<Long, Entry>> entrySeries = new LinkedHashMap<>();
            final TreeMap<Long, Entry> unknownEntries = new TreeMap<>();

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
                    entrySeries.put(account, new TreeMap<Long, Entry>());
                }


                List<Record> recordsFrom = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_FROM, start, end, -1));
                List<Record> recordsTo = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_TO, start, end, -1));

                boolean positive = AccountType.isPositive(accountType);

                int i = 0;
                for (List<Record> records : new List[]{recordsFrom, recordsTo}) {
                    boolean from = i == 0;
                    for (Record r : records) {
                        TreeMap<Long, Entry> entries;

                        Account acc = accountMap.get(from ? r.getFrom() : r.getTo());
                        if (acc == null) {
                            entries = unknownEntries;
                        } else {
                            entries = entrySeries.get(acc);
                        }

                        float y = r.getMoney() == null ? 0f : r.getMoney().floatValue();
                        Date x = r.getDate();

                        if (from) {
                            y = -y;
                        }

                        //group by day or month
                        if (periodMode == PeriodMode.MONTHLY) {
                            x = calHelper.toDayStart(x);
                        } else {
                            x = calHelper.monthStartDate(x);
                        }

                        if (!entries.containsKey(x.getTime())) {
                            entries.put(x.getTime(), new Entry(x.getTime(), y));
                        } else {
                            Entry e = entries.get(x.getTime());
                            e.setY(e.getY() + y);
                        }
                    }
                    i++;
                }

                //remove empty entries account
                Iterator<Map.Entry<Account, TreeMap<Long, Entry>>> iter = entrySeries.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Account, TreeMap<Long, Entry>> e = iter.next();
                    TreeMap<Long, Entry> map = e.getValue();
                    int s = map.size();
                    if (s == 0) {
                        //remove empty entry
                        iter.remove();
                    } else {
                        if (calculationMode == CalculationMode.CUMULATIVE) {
                            List<Entry> l = new ArrayList<>(map.values());
                            for (int j = 1; j < s; j++) {
                                Entry entry = l.get(j);
                                entry.setY(entry.getY() + l.get(j - 1).getY());
                            }

                        }
                    }
                }

                if (!positive) {
                    iter = entrySeries.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Account, TreeMap<Long, Entry>> e = iter.next();
                        List<Entry> l = new ArrayList<Entry>(e.getValue().values());
                        int s = l.size();
                        for (int j = 0; j < s; j++) {
                            Entry entry = l.get(j);
                            entry.setY(entry.getY() * -1);
                        }
                    }
                }

                if (unknownEntries.size() > 0) {
                    if (calculationMode == CalculationMode.CUMULATIVE) {
                        List<Entry> l = new ArrayList<Entry>(unknownEntries.values());
                        int s = l.size();
                        for (int j = 1; j < s; j++) {
                            Entry entry = l.get(j);
                            entry.setY(entry.getY() + l.get(j - 1).getY());
                        }
                    }
                    if (!positive) {
                        List<Entry> l = new ArrayList<Entry>(unknownEntries.values());
                        int s = l.size();
                        for (int j = 0; j < s; j++) {
                            Entry entry = l.get(j);
                            entry.setY(entry.getY() * -1);
                        }
                    }
                }
            }

            private int nextColor(int i) {
                return colorTemplate[i % colorTemplate.length];
            }

            @Override
            public void onAsyncFinish() {
                super.onAsyncFinish();

                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                int i = 0;
                for (Account key : entrySeries.keySet()) {
                    TreeMap<Long, Entry> list = entrySeries.get(key);

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

                    LineDataSet set = new LineDataSet(new ArrayList<Entry>(list.values()), label);

                    set.setValueTextSize(labelTextSize - 4);
                    set.setValueTextColor(labelTextColor);

                    int color;

                    color = nextColor(i++);

                    set.setColors(color);
                    set.setCircleColor(lightTheme ? Colors.darken(color, 0.3f) : Colors.lighten(color, 0.3f));
                    set.setCircleColorHole(lightTheme ? Colors.darken(color, 0.2f) : Colors.lighten(color, 0.2f));

                    dataSets.add(set);
                }
                //don't show unknown if account is present
                if (account==null && unknownEntries.size() > 0) {
                    LineDataSet set = new LineDataSet(new ArrayList<Entry>(unknownEntries.values()), i18n.string(R.string.label_unknown));
                    set.setColors(nextColor(i++));
                    set.setValueTextColor(labelTextColor);
                    set.setValueTextSize(labelTextSize - 4);
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