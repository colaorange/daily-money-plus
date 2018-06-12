package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import java.util.Date;

/**
 * Created by Dennis
 */
public class LineFromBeginningAggregateActivity extends PeriodModeChartBaseActivity {


    @Override
    protected PeriodModeChartBaseFragment newChartFragment(Date targetDate) {
        PeriodModeChartBaseFragment frag = new LineFromBeginningAggregateFragment();
        Bundle args = (Bundle)getIntentExtras().clone();
        args.putBoolean(ChartBaseFragment.ARG_MORE_HEIGHT, true);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_BASE_DATE, targetDate);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_PERIOD_MODE, periodMode);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_FROM_BEGINNING, fromBeginning);
        frag.setArguments(args);
        return frag;
    }
}
