package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.LineAccountFragment;

/**
 * @author dennis
 */
public class CardLineAccountFragment extends CardPeriodModeChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new LineAccountFragment();
    }

}