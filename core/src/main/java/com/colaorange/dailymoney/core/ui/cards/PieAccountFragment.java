package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartPieAccountFragment;

/**
 * @author dennis
 */
public class PieAccountFragment extends CardChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new ChartPieAccountFragment();
    }

}