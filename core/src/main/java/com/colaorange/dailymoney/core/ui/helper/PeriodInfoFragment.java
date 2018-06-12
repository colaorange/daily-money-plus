package com.colaorange.dailymoney.core.ui.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.util.I18N;

import java.text.DateFormat;
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

    private DateFormat monthDateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;

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
        i18n = i18n();
        ContextsActivity activity = getContextsActivity();

        Preference pref = preference();
        monthDateFormat = pref.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = pref.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM");
        yearFormat = pref.getYearFormat();//new SimpleDateFormat("yyyy");

        vInfo = rootView.findViewById(R.id.period_info);
    }

    public void reloadData() {
        CalendarHelper cal = calendarHelper();
        Date targetEndDate = null;
        Date targetStartDate = null;
        vInfo.setText("");

        switch (periodMode) {
            case YEARLY:
                targetEndDate = cal.yearEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.yearStartDate(targetDate);
                break;
            case WEEKLY:
                targetEndDate = cal.weekEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.weekStartDate(targetDate);
                break;
            default:
                targetEndDate = cal.monthEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.monthStartDate(targetDate);
                break;
        }

        // update info
        if (fromBeginning) {
            if (PeriodMode.MONTHLY.equals(periodMode)) {
                Date monthStart = cal.monthStartDate(targetDate);
                Date monthEnd = cal.monthEndDate(targetDate);
                vInfo.setText(i18n.string(R.string.label_until_time, yearMonthFormat.format(monthStart),
                        monthDateFormat.format(monthEnd)));
            } else if (PeriodMode.YEARLY.equals(periodMode)) {
                Date yearStart = cal.yearStartDate(targetDate);
                Date yearEnd = cal.yearEndDate(targetDate);

                vInfo.setText(i18n.string(R.string.label_until_time, yearFormat.format(yearStart),
                        yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
            } else if (PeriodMode.WEEKLY.equals(periodMode)) {
                Date weekStart = cal.weekStartDate(targetDate);
                Date weekEnd = cal.weekEndDate(targetDate);

                vInfo.setText(i18n.string(R.string.label_until_time, yearFormat.format(weekStart),
                        monthDateFormat.format(weekEnd)));
            }
        } else {
            if (PeriodMode.MONTHLY.equals(periodMode)) {
                Date monthStart = cal.monthStartDate(targetDate);
                Date monthEnd = cal.monthEndDate(targetDate);
                vInfo.setText(i18n.string(R.string.label_in_time, yearMonthFormat.format(monthStart),
                        monthDateFormat.format(monthStart), monthDateFormat.format(monthEnd)));
            } else if (PeriodMode.YEARLY.equals(periodMode)) {
                Date yearStart = cal.yearStartDate(targetDate);
                Date yearEnd = cal.yearEndDate(targetDate);
                vInfo.setText(i18n.string(R.string.label_in_time, yearFormat.format(yearStart),
                        monthDateFormat.format(yearStart), yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
            } else if (PeriodMode.WEEKLY.equals(periodMode)) {
                Date weekStart = cal.weekStartDate(targetDate);
                Date weekEnd = cal.weekEndDate(targetDate);
                vInfo.setText(i18n.string(R.string.label_in_time, yearFormat.format(weekStart),
                        monthDateFormat.format(weekStart), monthDateFormat.format(weekEnd)));
            }
        }
    }
}