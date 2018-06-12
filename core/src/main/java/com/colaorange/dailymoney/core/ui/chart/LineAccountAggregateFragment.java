package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.commons.util.Var;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * aggregate account chart that compare current and preious period of an account type
 *
 * @author dennis
 */
public class LineAccountAggregateFragment extends PeriodModeChartBaseFragment<LineChart> {

    public static final String ARG_CALCULATION_MODE = "calculationMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_PREVIOUS_PERIOD = "previousPeriod";

    private CalculationMode calculationMode;
    private AccountType accountType;
    private boolean previousPeriod = true;

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
        if (accountType == null) {
            accountType = AccountType.EXPENSE;
        }

        previousPeriod = args.getBoolean(ARG_PREVIOUS_PERIOD, previousPeriod);

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
        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            final Var<Boolean> noPrimary = new Var<>(false);
            final Map<String, List<Entry>> entrySeries = new LinkedHashMap<>();

            @Override
            public void run() {

                CalendarHelper calHelper = calendarHelper();

                Date start;
                Date end;
                Date previousDate;
                long timeshift;
                if (periodMode == PeriodMode.MONTHLY) {
                    start = calHelper.monthStartDate(baseDate);
                    end = calHelper.monthEndDate(baseDate);
                    previousDate = calHelper.monthBefore(baseDate, 1);
                    timeshift = start.getTime() - calHelper.monthStartDate(previousDate).getTime();
                } else {
                    start = calHelper.yearStartDate(baseDate);
                    end = calHelper.yearEndDate(baseDate);
                    previousDate = calHelper.yearBefore(baseDate, 1);
                    timeshift = start.getTime() - calHelper.yearStartDate(previousDate).getTime();
                }

                vChart.getXAxis().setAxisMinimum(start.getTime());
                vChart.getXAxis().setAxisMaximum(end.getTime());


                TreeMap<Long, Entry> primaryEntries = buildSeries(calHelper, baseDate, 0);
                TreeMap<Long, Entry> previousEntries = buildSeries(calHelper, previousDate, timeshift);

                /**
                 * java.lang.IndexOutOfBoundsException
                 at java.util.LinkedList.get(LinkedList.java:519)
                 at com.github.mikephil.charting.data.DataSet.getEntryForIndex(DataSet.java:286)
                 at com.github.mikephil.charting.utils.Transformer.generateTransformedValuesLine(Transformer.java:184)
                 at com.github.mikephil.charting.renderer.LineChartRenderer.drawValues(LineChartRenderer.java:547)
                 at com.github.mikephil.charting.charts.BarLineChartBase.onDraw(BarLineChartBase.java:264)
                 at android.view.View.draw(View.java:15114)
                 */
                if (primaryEntries.size() > 0) {
                    entrySeries.put(accountType.getDisplay(i18n), new LinkedList<Entry>(primaryEntries.values()));
                } else {
                    noPrimary.value = true;
                }
                if (previousEntries.size() > 0) {
                    entrySeries.put(i18n.string(R.string.label_previous_period), new LinkedList<Entry>(previousEntries.values()));
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
                    String label = key.toString();

                    LineDataSet set = new LineDataSet(list, label);


                    int color;

                    color = nextColor(i);

                    set.setColors(color);

                    if (i == 0 && !noPrimary.value) {
                        set.setValueTextSize(labelTextSize - 4);
                        set.setValueTextColor(labelTextColor);
                        set.setCircleColor(lightTheme ? Colors.darken(color, 0.3f) : Colors.lighten(color, 0.3f));
                        set.setCircleColorHole(lightTheme ? Colors.darken(color, 0.2f) : Colors.lighten(color, 0.2f));
                    } else {
                        set.setDrawValues(false);
                        set.setDrawCircles(false);
                        set.setDrawCircleHole(false);
                        set.enableDashedLine(10f, 10f, 0);
                    }
                    dataSets.add(set);

                    i++;
                }

                /**
                 * java.lang.IndexOutOfBoundsException
                 at java.util.LinkedList.get(LinkedList.java:519)
                 at com.github.mikephil.charting.data.DataSet.getEntryForIndex(DataSet.java:286)
                 at com.github.mikephil.charting.utils.Transformer.generateTransformedValuesLine(Transformer.java:184)
                 at com.github.mikephil.charting.renderer.LineChartRenderer.drawValues(LineChartRenderer.java:547)
                 at com.github.mikephil.charting.charts.BarLineChartBase.onDraw(BarLineChartBase.java:264)
                 at android.view.View.draw(View.java:15114)
                 */
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

    private TreeMap<Long, Entry> buildSeries(CalendarHelper calHelper, Date shiftData, long shiftBackTime) {
        Date start;
        Date end;
        if (periodMode == PeriodMode.MONTHLY) {
            start = calHelper.monthStartDate(shiftData);
            end = calHelper.monthEndDate(shiftData);
        } else {
            start = calHelper.yearStartDate(shiftData);
            end = calHelper.yearEndDate(shiftData);
        }

        IDataProvider idp = Contexts.instance().getDataProvider();
        Map<String, Account> accountMap = null;

        TreeMap<Long, Entry> entries = new TreeMap<>();

        List<Record> recordsFrom = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_FROM, start, end, -1));
        List<Record> recordsTo = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_TO, start, end, -1));

        boolean positive;
        switch (accountType) {
            case INCOME:
            case LIABILITY:
                positive = false;
                break;
            case UNKONW:
            case EXPENSE:
            case ASSET:
            case OTHER:
            default:
                positive = true;
                break;
        }

        int i = 0;
        for (List<Record> records : new List[]{recordsFrom, recordsTo}) {
            boolean from = i == 0;
            for (Record r : records) {

                float y = r.getMoney() == null ? 0f : r.getMoney().floatValue();

                if (from) {
                    y = -y;
                }

                Date x = r.getDate();

                //group by day or month
                if (periodMode == PeriodMode.MONTHLY) {
                    x = calHelper.toDayStart(x);
                } else {
                    x = calHelper.monthStartDate(x);
                }

                x.setTime(x.getTime() + shiftBackTime);


                if (!entries.containsKey(x.getTime())) {
                    entries.put(x.getTime(), new Entry(x.getTime(), y));
                } else {
                    Entry e = entries.get(x.getTime());
                    e.setY(e.getY() + y);
                }
            }
            i++;
        }

        if (calculationMode == CalculationMode.CUMULATIVE) {
            List<Entry> l = new ArrayList<>(entries.values());
            int s = l.size();
            for (int j = 1; j < s; j++) {
                Entry entry = l.get(j);
                entry.setY(entry.getY() + l.get(j - 1).getY());
            }
        }

        if (!positive) {
            List<Entry> l = new ArrayList<Entry>(entries.values());
            int s = l.size();
            for (int j = 0; j < s; j++) {
                Entry entry = l.get(j);
                entry.setY(entry.getY() * -1);
            }
        }

        return entries;
    }
}
