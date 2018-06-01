package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

/**
 * Created by Dennis
 */
public class PieAccountActivity extends ChartBaseActivity{


    @Override
    protected ChartBaseFragment newChartFragment() {
        ChartBaseFragment frag = new ChartPieAccountFragment();
        Bundle args = (Bundle)getIntentExtras().clone();
        frag.setArguments(args);
        return frag;
    }
}
