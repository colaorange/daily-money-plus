package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountFragment;

/**
 * @author dennis
 */
public class LineAccountFragment extends CardChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new ChartLineAccountFragment();
    }

}