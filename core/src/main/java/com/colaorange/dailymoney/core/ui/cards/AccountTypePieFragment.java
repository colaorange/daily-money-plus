package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.commons.util.Var;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.util.GUIs;
import com.github.mikephil.charting.charts.Chart;
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

/**
 * @author dennis
 */
public class AccountTypePieFragment extends CardBaseFragment implements EventQueue.EventListener {

    public static final String ARG_MODE = "mode";
    public static final String ARG_ACCOUNT_TYPE = "accountType";
    public static final String ARG_BASE_DATE = "baseDate";

    public enum Mode {
        WEEKLY, MONTHLY
    }


    Mode mode;
    AccountType accountType;
    Date baseDate;

    Chart vChart;

    float labelTextSize;
    int labelTextColor;
    int backgroupdColor;
    int[] colorTemplate;

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        mode = (Mode) args.getSerializable(ARG_MODE);
        if (mode == null) {
            mode = Mode.WEEKLY;
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
        i18n = Contexts.instance().getI18n();

        ContextsActivity activity = getContextsActivity();

        labelTextSize = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSizeSmall).data).value;//we always set it as dp
        labelTextColor = activity.getAccountTextColorMap().get(accountType);

        backgroupdColor = activity.resolveThemeAttrResData(R.attr.appCardColor);

        if (activity.isLightTheme()) {
            backgroupdColor = Colors.darken(backgroupdColor, 0.01f);
        } else {
            backgroupdColor = Colors.lighten(backgroupdColor, 0.01f);
        }

        colorTemplate = activity.getChartColorTemplate();

        //general vChart
        vChart = rootView.findViewById(R.id.card_chart);
        vChart.setBackgroundColor(backgroupdColor);
        vChart.getLegend().setWordWrapEnabled(true);
        vChart.getLegend().setTextColor(labelTextColor);
        vChart.getLegend().setTextSize(labelTextSize - 1);
        vChart.getDescription().setEnabled(false);


        //i can't slide over char in my real phone if I enable the the highlight feature
//        vChart.setTouchEnabled(false);

        //pie vChart
        PieChart pieChart = ((PieChart) vChart);
        pieChart.setEntryLabelColor(labelTextColor);
        pieChart.setEntryLabelTextSize(labelTextSize - 2);
        pieChart.setCenterTextSize(labelTextSize);
        pieChart.setCenterTextColor(labelTextColor);
        pieChart.setHoleColor(backgroupdColor);
        pieChart.setHoleRadius(45);
        pieChart.setTransparentCircleRadius(55);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.card_account_type_pie_frag;
    }

    protected void reloadView() {
        super.reloadView();

        ContextsActivity activity = getContextsActivity();
        //calculate better padding for slide over vChart(give more space for slide)
        float dp = GUIs.getDPRatio(activity);
        float w = GUIs.getDPWidth(activity);
        float h = GUIs.getDPHeight(activity);

        int pTop, pBottom, pLeft, pRight;

        pBottom = (int) (10 * dp);
        if (h >= w) {
            pTop = (int) (showTitle ? 0 : 40 * dp);
            pLeft = pRight = (int) (4 * dp);
        } else {
            pTop = (int) (showTitle ? 0 : 4 * dp);
            pLeft = pRight = (int) (40 * dp);
        }

        vContent.setPadding(pLeft, pTop, pRight, pBottom);

        float size = Math.max(w, h) / 2f;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vChart.getLayoutParams();
        lp.height = (int) (size * dp);
        vChart.setLayoutParams(lp);
    }

    @Override
    protected boolean doReloadContent() {

        GUIs.doAsync(getContextsActivity(), new GUIs.AsyncAdapter() {

            Var<Double> varExp = new Var<>();
            List<PieEntry> entries = new LinkedList<>();

            @Override
            public void run() {

                CalendarHelper calHelper = calendarHelper();

                Date start;
                Date end;

                switch (mode) {
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
                varExp.value = BalanceHelper.calculateBalance(accountType, start, end).getMoney();


                List<Balance> list = new ArrayList<>();

                IDataProvider idp = Contexts.instance().getDataProvider();

                for (Account acc : idp.listAccount(accountType)) {
                    Balance balance = BalanceHelper.calculateBalance(acc, start, end);
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
                if (entries.size() == 0) {
                    entries.add(new PieEntry(100, i18n.string(R.string.msg_no_data)));
                    set = new PieDataSet(entries, "");
                } else {

                    switch (mode) {
                        case MONTHLY:
                            description = i18n.string(R.string.label_monthly_expense, contexts().toFormattedMoneyString(varExp.value));
                            break;
                        case WEEKLY:
                        default:
                            description = i18n.string(R.string.label_weekly_expense, contexts().toFormattedMoneyString(varExp.value));
                            break;
                    }
                    set = new PieDataSet(entries, "");
                }
                set.setColors(colorTemplate);

                set.setSliceSpace(1f);//space between entry
                set.setValueTextSize(labelTextSize - 4);
                set.setValueTextColor(labelTextColor);
                set.setSelectionShift(10f);//size shift after highlight
//                set.setValueLinePart1OffsetPercentage(90.f);//begin position of value line
//                set.setValueLinePart1Length(1f);//length of value line
//                set.setValueLinePart2Length(2f);//length of value line
                set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                set.setValueLineColor(labelTextColor);


                PieData data = new PieData(set);
                data.setValueFormatter(new IValueFormatter() {
                    @Override
                    public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
                        StringBuilder sb = new StringBuilder(Numbers.format(v, "#0.##"));
                        if (varExp.value > 0) {
                            v = (float) (v * 100 / varExp.value);
                            if (v >= 10) {//only show that > 10
                                sb.append("(").append(Numbers.format(v, "#0.#")).append("%)");
                            }
                        }
                        return sb.toString();
                    }
                });

                vChart.setData(data);

                PieChart pieChart = ((PieChart) vChart);

                pieChart.setCenterText(description);
                vChart.invalidate(); // refresh
            }
        });

        return true;
    }

}