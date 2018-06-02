package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

/**
 * Created by Dennis
 */
public class PieAccountActivity extends ChartBaseActivity{


    @Override
    protected ChartBaseFragment newChartFragment() {
        ChartBaseFragment frag = new PieAccountFragment();
        Bundle args = (Bundle)getIntentExtras().clone();
        //label be cut
//        args.putBoolean(ChartBaseFragment.ARG_MORE_HEIGHT, true);
        frag.setArguments(args);
        return frag;
    }
}
