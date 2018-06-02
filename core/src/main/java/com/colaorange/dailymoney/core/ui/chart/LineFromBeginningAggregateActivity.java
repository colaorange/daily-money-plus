package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

/**
 * Created by Dennis
 */
public class LineFromBeginningAggregateActivity extends ChartBaseActivity{


    @Override
    protected ChartBaseFragment newChartFragment() {
        ChartBaseFragment frag = new LineFromBeginningAggregateFragment();
        Bundle args = (Bundle)getIntentExtras().clone();
        args.putBoolean(ChartBaseFragment.ARG_MORE_HEIGHT, true);
        frag.setArguments(args);
        return frag;
    }
}
