package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Date;

/**
 * Created by Dennis
 */
public class PieAccountActivity extends PeriodModeChartBaseActivity {

    @Override
    protected FragNewerBase newFragNewer(Date targetDate) {
        Bundle args = (Bundle)getIntentExtras().clone();
        //label be cut
//        args.putBoolean(ChartBaseFragment.ARG_MORE_HEIGHT, true);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_BASE_DATE, targetDate);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_PERIOD_MODE, periodMode);
        args.putSerializable(PeriodModeChartBaseFragment.ARG_FROM_BEGINNING, fromBeginning);
        return new FragNewerImpl(args);
    }

    public static class FragNewerImpl extends FragNewerBase {
        public FragNewerImpl(Bundle args){
            super(args);
        }

        @Override
        public Fragment newFragment() {
            PeriodModeChartBaseFragment frag = new PieAccountFragment();
            frag.setArguments(args);
            return frag;
        }
    }
}
