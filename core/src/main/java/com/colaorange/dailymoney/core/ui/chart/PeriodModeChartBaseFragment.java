package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.helper.PeriodInfoFragment;
import com.colaorange.dailymoney.core.util.Logger;
import com.github.mikephil.charting.charts.Chart;

import java.util.Date;

/**
 * @author dennis
 */
public abstract class PeriodModeChartBaseFragment<C extends Chart> extends ChartBaseFragment<C> {


    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_BASE_DATE = "baseDate";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";


    protected PeriodMode periodMode;
    protected Date baseDate;
    protected boolean fromBeginning;

    @Override
    protected void initArgs() {
        super.initArgs();
        Bundle args = getArguments();
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }

        baseDate = (Date) args.getSerializable(ARG_BASE_DATE);
        if (baseDate == null) {
            baseDate = new Date();
        }

        fromBeginning = args.getBoolean(ARG_FROM_BEGINNING, false);
    }

    @Override
    protected void initMembers() {
        super.initMembers();

        if (rootView.findViewById(R.id.period_info_frag_container) != null) {
            String fragTag = "periodInfo";
            if (getChildFragmentManager().findFragmentByTag(fragTag) == null) {
                PeriodInfoFragment f = new PeriodInfoFragment();
                Bundle b = new Bundle();
                b.putBoolean(PeriodInfoFragment.ARG_FROM_BEGINNING, fromBeginning);
                b.putSerializable(PeriodInfoFragment.ARG_PERIOD_MODE, periodMode);
                b.putSerializable(PeriodInfoFragment.ARG_TARGET_DATE, baseDate);
                f.setArguments(b);

                getChildFragmentManager().beginTransaction()
                        .add(R.id.period_info_frag_container, f, fragTag)
                        .disallowAddToBackStack()
                        .commit();
            }
        }

    }

}