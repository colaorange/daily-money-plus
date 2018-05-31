package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.helper.XAxisDateFormatter;
import com.colaorange.dailymoney.core.util.GUIs;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class ChartLineAccountTypeFragment extends ChartBaseFragment<LineChart> {

    public static final String ARG_MODE = "mode";
    public static final String ARG_CALCULATION_MODE = "calculationMode";
    public static final String ARG_SERIES_MODE = "seriesMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_BASE_DATE = "baseDate";

    public enum Mode {
        MONTHLY, YEARLY
    }

    public enum CalculationMode {
        INDIVIDUAL, CUMULATIVE
    }

    public enum SeriesMode {
        INDIVIDUAL, INTEGRATED
    }

    private Mode mode;
    private CalculationMode calculationMode;
    private SeriesMode seriesMode;
    private AccountType accountType;
    private Date baseDate;

    protected int accountTypeTextColor;
    private XAxisDateFormatter formatter;

    private DateFormat xAxisFormat;

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        mode = (Mode) args.getSerializable(ARG_MODE);
        if (mode == null) {
            mode = Mode.MONTHLY;
        }

        calculationMode = (CalculationMode) args.getSerializable(ARG_CALCULATION_MODE);
        if (calculationMode == null) {
            calculationMode = calculationMode.INDIVIDUAL;
        }

        seriesMode = (SeriesMode) args.getSerializable(ARG_SERIES_MODE);
        if (seriesMode == null) {
            seriesMode = SeriesMode.INTEGRATED;
        }

        accountType = (AccountType) args.getSerializable(ARG_ACCOUNT_TYPE);
        if (accountType == null) {
            accountType = AccountType.EXPENSE;
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

        if (mode == Mode.MONTHLY) {
            xAxisFormat = Contexts.instance().getPreference().getDayFormat();
        } else {
            xAxisFormat = Contexts.instance().getPreference().getNonDigitalMonthFormat();
        }
        accountTypeTextColor = activity.getAccountTextColorMap().get(accountType);
        Legend legend = vChart.getLegend();
        legend.setTextColor(accountTypeTextColor);
        //x
        XAxis xAxis = vChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(labelTextColor);
        xAxis.setTextSize(labelTextSize - 4);
        if (mode == Mode.MONTHLY) {
            xAxis.setGranularity(Numbers.DAY); // only intervals of 1 day
        } else {
            xAxis.setGranularity(Numbers.DAY * 28); // only intervals of 1 month
        }
        xAxis.setValueFormatter(formatter = new XAxisDateFormatter(xAxisFormat));

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //y
        YAxis leftAxis = vChart.getAxisLeft();
        YAxis rightAxis = vChart.getAxisRight();

        leftAxis.setTextColor(labelTextColor);
        leftAxis.setTextSize(labelTextSize - 3);
        rightAxis.setTextColor(labelTextColor);
        rightAxis.setTextSize(labelTextSize - 3);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.chart_line_account_type_frag;
    }

    @Override
    public void reloadChart() {
        super.reloadChart();
        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            final Map<Object, List<Entry>> entrySeries = new LinkedHashMap<>();
            final List<Entry> unknownEntries = new LinkedList<>();

            @Override
            public void run() {

                CalendarHelper calHelper = calendarHelper();

                Date start;
                Date end;
                if (mode == Mode.MONTHLY) {
                    start = calHelper.monthStartDate(baseDate);
                    end = calHelper.monthEndDate(baseDate);
                } else {
                    start = calHelper.yearStartDate(baseDate);
                    end = calHelper.yearEndDate(baseDate);
                }


                IDataProvider idp = Contexts.instance().getDataProvider();
                Map<String, Account> accountMap = null;

                if (seriesMode == SeriesMode.INDIVIDUAL) {
                    List<Account> accounts = idp.listAccount(accountType);
                    accountMap = new LinkedHashMap<>();
                    for (Account a : accounts) {
                        accountMap.put(a.getId(), a);
                        entrySeries.put(a, new LinkedList<Entry>());
                    }
                } else {
                    entrySeries.put(accountType, new LinkedList<Entry>());
                }

                List<Record> records = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_TO, start, end, -1));

                //sort by time, so we don't need to care seq in following processing
                Collections.sort(records, new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });


                for (Record r : records) {
                    List<Entry> entries;

                    if (seriesMode == SeriesMode.INDIVIDUAL) {
                        String accto = r.getTo();
                        Account acc = accountMap.get(accto);
                        if (acc == null) {
                            entries = unknownEntries;
                        } else {
                            entries = entrySeries.get(acc);
                        }
                    } else {
                        entries = entrySeries.get(accountType);
                    }

                    float y = r.getMoney() == null ? 0f : r.getMoney().floatValue();
                    Date x = r.getDate();

                    //group by day or month
                    if (mode == Mode.MONTHLY) {
                        x = calHelper.toDayStart(x);
                    } else {
                        x = calHelper.monthStartDate(x);
                    }

                    if (entries.size() == 0 || entries.get(entries.size() - 1).getX() != (float) x.getTime()) {
                        entries.add(new Entry(x.getTime(), y));
                    } else {
                        Entry e = entries.get(entries.size() - 1);
                        e.setY(e.getY() + y);
                    }
                }

                //remove empty entries account
                Iterator<Map.Entry<Object, List<Entry>>> iter = entrySeries.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Object, List<Entry>> e = iter.next();
                    List<Entry> l = e.getValue();
                    int s = l.size();
                    if (s == 0) {
                        iter.remove();
                    } else {
                        if (calculationMode == CalculationMode.CUMULATIVE) {
                            for (int i = 1; i < s; i++) {
                                Entry entry = l.get(i);
                                entry.setY(entry.getY() + l.get(i - 1).getY());
                            }

                        }
                    }
                }

                if (calculationMode == CalculationMode.CUMULATIVE && unknownEntries.size() > 0) {
                    int s = unknownEntries.size();
                    for (int i = 1; i < s; i++) {
                        Entry entry = unknownEntries.get(i);
                        entry.setY(entry.getY() + unknownEntries.get(i - 1).getY());
                    }
                }
            }

            private int nextColor(int i) {
                return colorTemplate[i % colorTemplate.length];
            }

            @Override
            public void onAsyncFinish() {


                List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                int i = 0;
                for (Object key : entrySeries.keySet()) {
                    List<Entry> list = entrySeries.get(key);
                    String label;
                    if (key instanceof Account) {
                        label = ((Account) key).getName();
                    } else if (key instanceof AccountType) {
                        label = ((AccountType) key).getDisplay(i18n);
                    } else {
                        label = key.toString();
                    }
                    LineDataSet set = new LineDataSet(list, label);

                    set.setValueTextSize(labelTextSize - 4);
                    set.setValueTextColor(labelTextColor);

                    int color;

                    if (seriesMode == SeriesMode.INDIVIDUAL) {
                        color = nextColor(i++);
                    } else {
                        color = accountTypeTextColor;
                    }
                    set.setColors(color);
                    set.setCircleColor(lightTheme ? Colors.darken(color, 0.3f) : Colors.lighten(color, 0.3f));
                    set.setCircleColorHole(lightTheme ? Colors.darken(color, 0.2f) : Colors.lighten(color, 0.2f));

                    dataSets.add(set);
                }
                if (unknownEntries.size() > 0) {
                    LineDataSet set = new LineDataSet(unknownEntries, i18n.string(R.string.label_unknown));
                    set.setColors(nextColor(i++));
                    set.setValueTextColor(labelTextColor);
                    set.setValueTextSize(labelTextSize - 4);
                    dataSets.add(set);
                }

                LineData data = new LineData(dataSets);

//                data.setValueFormatter(new IValueFormatter() {
//                    @Override
//                    public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
//                        StringBuilder sb = new StringBuilder(Numbers.format(v, "#0.##"));
//                        return sb.toString();
//                    }
//                });


                vChart.setData(data);
                vChart.invalidate(); // refresh
            }
        });
    }

}