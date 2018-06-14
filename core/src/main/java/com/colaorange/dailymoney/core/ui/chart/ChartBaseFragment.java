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
import com.colaorange.dailymoney.core.ui.GUIs;
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
    public static final String ARG_MORE_HEIGHT = "moreHeight";

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

    protected boolean moreHeight;

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
        trackEvent(Contexts.TE.CHART + "" + getClass().getSimpleName());
    }

    @CallSuper
    protected void initArgs() {
        Bundle args = getArguments();
        titlePadding = args.getBoolean(ARG_TITLE_PADDING, false);
        moreHeight = args.getBoolean(ARG_MORE_HEIGHT, false);
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
        vChart = rootView.findViewById(R.id.chart);
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

        vChart.setNoDataTextColor(labelTextColor);
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
            pTop = (int) (titlePadding ? 10 * dp : 0);
            pLeft = pRight = (int) (40 * dp);
        }

        vContainer.setPadding(pLeft, pTop, pRight, pBottom);

        float size = Math.max(w, h) / (moreHeight ? 1.5f : 2f);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) vChart.getLayoutParams();
        lp.height = (int) (size * dp);
        vChart.setLayoutParams(lp);

    }


    public abstract class ChartLoading extends GUIs.AsyncAdapter {

        public ChartLoading(){
            vChart.setNoDataText(i18n.string(R.string.msg_data_loading));
        }

        @CallSuper
        @Override
        public void onAsyncFinish() {
            vChart.setNoDataText(i18n.string(R.string.msg_no_data));
        }

        @CallSuper
        @Override
        public void onAsyncError(Throwable t) {
            vChart.setNoDataText(i18n.string(R.string.msg_no_data));
            super.onAsyncError(t);
        }
    }
}