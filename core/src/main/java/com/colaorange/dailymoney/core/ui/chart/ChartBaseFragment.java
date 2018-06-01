package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardDesktop;
import com.colaorange.dailymoney.core.ui.cards.CardBaseFragment;
import com.colaorange.dailymoney.core.ui.cards.CardFacade;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * @author dennis
 */
public abstract class ChartBaseFragment<C extends Chart> extends ContextsFragment {


    public static final String ARG_TITLE_PADDING = "titlePadding";

    public enum PeriodMode {
        WEEKLY, MONTHLY, YEARLY
    }

    public enum CalculationMode {
        INDIVIDUAL, CUMULATIVE
    }


    protected View rootView;

    protected View vContainer;
    protected C vChart;

    protected I18N i18n;
    protected float labelTextSize;
    protected int labelTextColor;
    protected int backgroundColor;
    protected int[] colorTemplate;
    protected boolean lightTheme;
    protected boolean titlePadding;

    protected abstract int getLayoutResId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutResId(), container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadChart();
    }

    @CallSuper
    protected void initArgs() {
        Bundle args = getArguments();
        titlePadding = args.getBoolean(ARG_TITLE_PADDING, true);
    }

    @CallSuper
    protected void initMembers() {
        i18n = Contexts.instance().getI18n();

        ContextsActivity activity = getContextsActivity();

        labelTextSize = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSizeSmall).data).value;//we always set it as dp
        labelTextColor = activity.resolveThemeAttrResData(R.attr.appPrimaryTextColor);

        backgroundColor = activity.resolveThemeAttrResData(R.attr.appCardColor);

        if (lightTheme) {
            backgroundColor = Colors.darken(backgroundColor, 0.01f);
        } else {
            backgroundColor = Colors.lighten(backgroundColor, 0.01f);
        }

        colorTemplate = activity.getChartColorTemplate();

        vContainer = rootView.findViewById(R.id.chart_container);
        //general vChart
        vChart = rootView.findViewById(R.id.chart_chart);
        vChart.setBackgroundColor(backgroundColor);
        Legend legend = vChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setTextColor(labelTextColor);
        legend.setTextSize(labelTextSize - 1);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

        vChart.getDescription().setEnabled(false);
    }

    @CallSuper
    public void reloadChart() {
        ContextsActivity activity = getContextsActivity();
        //calculate better padding for slide over vChart(give more space for sliding)
        float dp = GUIs.getDPRatio(activity);
        float w = GUIs.getDPWidth(activity);
        float h = GUIs.getDPHeight(activity);

        int pTop, pBottom, pLeft, pRight;

        pBottom = (int) (10 * dp);
        if (h >= w) {
            pTop = (int) (titlePadding ? 40 * dp : 0);
            pLeft = pRight = (int) (4 * dp);
        } else {
            pTop = (int) (titlePadding ? 4 * dp : 0);
            pLeft = pRight = (int) (40 * dp);
        }

        vContainer.setPadding(pLeft, pTop, pRight, pBottom);

        float size = Math.max(w, h) / 2f;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) vChart.getLayoutParams();
        lp.height = (int) (size * dp);
        vChart.setLayoutParams(lp);
    }


    public static class MoneyFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return Numbers.format(v, "#0.##");
        }
    }
}