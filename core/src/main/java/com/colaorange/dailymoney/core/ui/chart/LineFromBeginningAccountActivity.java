package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;

import java.util.Date;

/**
 * Created by Dennis
 */
public class LineFromBeginningAccountActivity extends PeriodModeChartBaseActivity {


    @Override
    protected PeriodModeChartBaseFragment newChartFragment(Date targetDate) {
        PeriodModeChartBaseFragment frag = new LineFromBeginningAccountFragment();
        Bundle args = (Bundle)getIntentExtras().clone();
        args.putBoolean(PeriodModeChartBaseFragment.ARG_MORE_HEIGHT, true);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_BASE_DATE, targetDate);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_PERIOD_MODE, periodMode);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_FROM_BEGINNING, fromBeginning);
        frag.setArguments(args);
        return frag;
    }
}
