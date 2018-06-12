package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.helper.PeriodInfoFragment;
import com.colaorange.dailymoney.core.util.I18N;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

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
            periodMode = PeriodMode.WEEKLY;
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