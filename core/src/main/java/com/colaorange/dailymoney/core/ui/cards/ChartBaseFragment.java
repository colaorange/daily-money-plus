package com.colaorange.dailymoney.core.ui.cards;

import android.widget.LinearLayout;

import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.util.GUIs;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;

/**
 * @author dennis
 */
public abstract class ChartBaseFragment<C extends Chart> extends CardBaseFragment implements EventQueue.EventListener {

    protected C vChart;

    protected float labelTextSize;
    protected int labelTextColor;
    protected int backgroundColor;
    protected int[] colorTemplate;


    @Override
    protected void initMembers() {
        super.initMembers();
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

        //general vChart
        vChart = rootView.findViewById(R.id.card_chart);
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

    @Override
    protected void reloadView() {
        super.reloadView();

        ContextsActivity activity = getContextsActivity();
        //calculate better padding for slide over vChart(give more space for sliding)
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

}