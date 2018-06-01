package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.commons.util.Var;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.util.GUIs;
import com.github.mikephil.charting.charts.LineChart;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * aggregate account chart that compare current and preious period of an account type
 *
 * @author dennis
 */
public class ChartLineAccountTypeFragment extends ChartBaseFragment<LineChart> {

    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_CALCULATION_MODE = "calculationMode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_BASE_DATE = "baseDate";
    public static final String ARG_PREVIOUS_PERIOD = "previousPeriod";

    PeriodMode periodMode;
    private CalculationMode calculationMode;
    private AccountType accountType;
    private Date baseDate;
    private boolean previousPeriod = true;

    protected int accountTypeTextColor;
    private XAxisDateFormatter formatter;

    private DateFormat xAxisFormat;

    private static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.MONTHLY, PeriodMode.YEARLY);

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

        calculationMode = (CalculationMode) args.getSerializable(ARG_CALCULATION_MODE);
        if (calculationMode == null) {
            calculationMode = calculationMode.CUMULATIVE;
        }

        accountType = (AccountType) args.getSerializable(ARG_ACCOUNT_TYPE);
        if (accountType == null) {
            accountType = AccountType.EXPENSE;
        }

        baseDate = (Date) args.getSerializable(ARG_BASE_DATE);
        if (baseDate == null) {
            baseDate = new Date();
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
//        rightAxis.setTextColor(accountTypeTextColor);
//        rightAxis.setTextSize(labelTextSize - 3);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.chart_line_account_type_frag;
    }

    @Override
    public void reloadChart() {
        super.reloadChart();
        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            final Var<Boolean> noPrimary = new Var<>(false);
            final Map<Object, List<Entry>> entrySeries = new LinkedHashMap<>();

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


                List<Entry> primaryEntries = buildSeries(calHelper, baseDate, 0);
                List<Entry> previousEntries = buildSeries(calHelper, previousDate, timeshift);

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
                    entrySeries.put(accountType.getDisplay(i18n), primaryEntries);
                } else {
                    noPrimary.value = true;
                }
                if (previousEntries.size() > 0) {
                    entrySeries.put(i18n.string(R.string.label_previous_period), previousEntries);
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

    private LinkedList<Entry> buildSeries(CalendarHelper calHelper, Date shiftData, long shiftBackTime) {
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

        LinkedList<Entry> entries = new LinkedList<Entry>();

        List<Record> records = new ArrayList<>(idp.listRecord(accountType, IDataProvider.LIST_RECORD_MODE_TO, start, end, -1));

        //sort by time, so we don't need to care seq in following processing
        Collections.sort(records, new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });


        for (Record r : records) {

            float y = r.getMoney() == null ? 0f : r.getMoney().floatValue();
            Date x = r.getDate();

            //group by day or month
            if (periodMode == PeriodMode.MONTHLY) {
                x = calHelper.toDayStart(x);
            } else {
                x = calHelper.monthStartDate(x);
            }

            x.setTime(x.getTime() + shiftBackTime);

            if (entries.size() == 0 || entries.get(entries.size() - 1).getX() != (float) x.getTime()) {
                entries.add(new Entry(x.getTime(), y));
            } else {
                Entry e = entries.get(entries.size() - 1);
                e.setY(e.getY() + y);
            }
        }

        //remove empty entries account
        if (calculationMode == CalculationMode.CUMULATIVE) {
            int s = entries.size();
            for (int i = 1; i < s; i++) {
                Entry entry = entries.get(i);
                entry.setY(entry.getY() + entries.get(i - 1).getY());
            }
        }

        return entries;
    }
}
