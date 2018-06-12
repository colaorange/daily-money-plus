package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.LineAccountAggregateFragment;

/**
 * @author dennis
 */
public class CardLineAccountAggregateFragment extends CardPeriodModeChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new LineAccountAggregateFragment();
    }

}