package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.LineAccountTypeFragment;

/**
 * @author dennis
 */
public class CardLineAccountTypeFragment extends CardChartBaseFragment {

    @Override
    protected ChartBaseFragment newFragment() {
        return new LineAccountTypeFragment();
    }

}