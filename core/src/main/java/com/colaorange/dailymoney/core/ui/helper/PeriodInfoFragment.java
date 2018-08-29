package com.colaorange.dailymoney.core.ui.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Misc;

import java.util.Date;
import java.util.Set;

/**
 * @author dennis
 */
public class PeriodInfoFragment extends ContextsFragment {

    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";

    private TextView vInfo;

    private Date targetDate;
    private PeriodMode periodMode;
    private boolean fromBeginning = false;

    private View rootView;

    I18N i18n;

    static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.WEEKLY, PeriodMode.MONTHLY, PeriodMode.YEARLY);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.period_info_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadData();
    }


    private void initArgs() {
        Bundle args = getArguments();
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }

        if (!supportPeriod.contains(periodMode)) {
            throw new IllegalStateException("unsupported period " + periodMode);
        }

        fromBeginning = args.getBoolean(ARG_FROM_BEGINNING, false);
        Object o = args.get(ARG_TARGET_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
    }

    private void initMembers() {
        vInfo = rootView.findViewById(R.id.period_info);
    }

    public void reloadData() {
        vInfo.setText(Misc.toPeriodInfo(periodMode, targetDate, fromBeginning));
    }
}