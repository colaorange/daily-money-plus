package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.PieAccountFragment;

/**
 * @author dennis
 */
public class CardPieAccountFragment extends CardPeriodModeChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new PieAccountFragment();
    }

}