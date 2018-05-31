package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountTypeFragment;

/**
 * @author dennis
 */
public class LineAccountTypeFragment extends CardChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new ChartLineAccountTypeFragment();
    }

}