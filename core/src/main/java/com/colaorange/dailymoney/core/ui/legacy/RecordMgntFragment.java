package com.colaorange.dailymoney.core.ui.legacy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class RecordMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final int MODE_DAY = RecordListFragment.MODE_DAY;
    public static final int MODE_WEEK = RecordListFragment.MODE_WEEK;
    public static final int MODE_MONTH = RecordListFragment.MODE_MONTH;
    public static final int MODE_YEAR = RecordListFragment.MODE_YEAR;
    public static final int MODE_ALL = RecordListFragment.MODE_ALL;


    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_MODE = "mode";
    public static final String ARG_POS = "pos";

    private TextView vInfo;
    private TextView vSumIncome;
    private TextView vSumExpense;
    private TextView vSumAsset;
    private TextView vSumLiability;
    private TextView vSumOther;
    private TextView vSumUnknow;

    private Date targetDate;
    private int mode;
    private int pos;

    private DateFormat dateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;
    private DateFormat weekDayFormat;
    private DateFormat nonDigitalMonthFormat;

    private Date targetStartDate;
    private Date targetEndDate;

    private View rootView;

    Map<AccountType, Integer> accountBgColorMap;
    Map<AccountType, Integer> accountTextColorMap;
    I18N i18n;

    boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_mgnt_frag, container, false);
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
        mode = args.getInt(ARG_MODE, MODE_WEEK);
        pos = args.getInt(ARG_POS, 0);
        Object o = args.get(ARG_TARGET_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        Preference preference = preference();

        dateFormat = preference.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
        yearMonthFormat = preference.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM - MMM");
        yearFormat = preference.getYearFormat();//new SimpleDateFormat("yyyy");
        weekDayFormat = preference.getWeekDayFormat();
        nonDigitalMonthFormat = preference.getNonDigitalMonthFormat();

        vInfo = rootView.findViewById(R.id.record_info);
        vSumIncome = rootView.findViewById(R.id.sum_income);
        vSumExpense = rootView.findViewById(R.id.sum_expense);
        vSumAsset = rootView.findViewById(R.id.sum_asset);
        vSumLiability = rootView.findViewById(R.id.sum_liability);
        vSumOther = rootView.findViewById(R.id.sum_other);
        vSumUnknow = rootView.findViewById(R.id.sum_unknow);

        vInfo = rootView.findViewById(R.id.record_info);


        FragmentManager fragmentManager = getChildFragmentManager();
        //clear frag before add frag, it might be android's bug
        String fragTag = getClass().getName() + ":" + pos;
        Fragment f;
        if ((f = fragmentManager.findFragmentByTag(fragTag)) != null) {
            //very strange, why a fragment is here already in create/or create again?
            //I need to read more document
        } else {

            f = new RecordListFragment();
            Bundle b = new Bundle();
            b.putInt(RecordListFragment.ARG_POS, pos);
            b.putInt(RecordListFragment.ARG_MODE, mode);
            f.setArguments(b);

            fragmentManager.beginTransaction()
                    .add(R.id.frag_container, f, fragTag)
                    .disallowAddToBackStack()
                    .commit();
        }


        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        i18n = Contexts.instance().getI18n();

    }

    private void reloadData() {

        final CalendarHelper cal = calendarHelper();
        vInfo.setText("");
        vSumIncome.setVisibility(TextView.GONE);
        vSumExpense.setVisibility(TextView.GONE);
        vSumAsset.setVisibility(TextView.GONE);
        vSumLiability.setVisibility(TextView.GONE);
        vSumOther.setVisibility(TextView.GONE);

        vSumUnknow.setVisibility(TextView.VISIBLE);

        switch (mode) {
            case MODE_ALL:
                targetStartDate = targetEndDate = null;
                break;
            case MODE_MONTH:
                targetStartDate = cal.monthStartDate(targetDate);
                targetEndDate = cal.monthEndDate(targetDate);
                break;
            case MODE_DAY:
                targetStartDate = cal.toDayStart(targetDate);
                targetEndDate = cal.toDayEnd(targetDate);
                break;
            case MODE_YEAR:
                targetStartDate = cal.yearStartDate(targetDate);
                targetEndDate = cal.yearEndDate(targetDate);

                break;
            case MODE_WEEK:
            default:
                targetStartDate = cal.weekStartDate(targetDate);
                targetEndDate = cal.weekEndDate(targetDate);
                break;
        }

        final IDataProvider idp = contexts().getDataProvider();

        final boolean sameYear = cal.isSameYear(targetStartDate, targetEndDate);
        final boolean sameMonth = cal.isSameMonth(targetStartDate, targetEndDate);

        GUIs.doBusy(getContextsActivity(), new GUIs.BusyAdapter() {
            List<Record> data = null;

            double expense;
            double income;
            double asset;
            double liability;
            double other;
            int count;

            @Override
            public void run() {
                data = idp.listRecord(targetStartDate, targetEndDate, preference().getMaxRecords());
                count = idp.countRecord(targetStartDate, targetEndDate);
                income = idp.sumFrom(AccountType.INCOME, targetStartDate, targetEndDate);
                expense = idp.sumTo(AccountType.EXPENSE, targetStartDate, targetEndDate);//nagivate
                asset = idp.sumTo(AccountType.ASSET, targetStartDate, targetEndDate) - idp.sumFrom(AccountType.ASSET, targetStartDate, targetEndDate);
                liability = idp.sumTo(AccountType.LIABILITY, targetStartDate, targetEndDate) - idp.sumFrom(AccountType.LIABILITY, targetStartDate, targetEndDate);
                liability = -liability;
                other = idp.sumTo(AccountType.OTHER, targetStartDate, targetEndDate) - idp.sumFrom(AccountType.OTHER, targetStartDate, targetEndDate);
            }

            @Override
            public void onBusyFinish() {
                CalendarHelper cal = calendarHelper();

                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(data).withArg(RecordListFragment.ARG_POS, pos).build());

                vSumUnknow.setVisibility(TextView.GONE);

                if (income != 0) {
                    vSumIncome.setText(i18n.string(R.string.label_reclist_sum_income, contexts().toFormattedMoneyString((income))));
                    vSumIncome.setVisibility(TextView.VISIBLE);
                }
                if (expense != 0) {
                    vSumExpense.setText(i18n.string(R.string.label_reclist_sum_expense, contexts().toFormattedMoneyString((expense))));
                    vSumExpense.setVisibility(TextView.VISIBLE);
                }
                if (asset != 0) {
                    vSumAsset.setText(i18n.string(R.string.label_reclist_sum_asset, contexts().toFormattedMoneyString((asset))));
                    vSumAsset.setVisibility(TextView.VISIBLE);
                }
                if (liability != 0) {
                    vSumLiability.setText(i18n.string(R.string.label_reclist_sum_liability, contexts().toFormattedMoneyString((liability))));
                    vSumLiability.setVisibility(TextView.VISIBLE);
                }
                if (other != 0) {
                    vSumOther.setText(i18n.string(R.string.label_reclist_sum_other, contexts().toFormattedMoneyString((other))));
                    vSumOther.setVisibility(TextView.VISIBLE);
                }

                StringBuilder sb = new StringBuilder();
                //update info
                switch (mode) {
                    case MODE_ALL:
                        vInfo.setText(i18n.string(R.string.label_all_records, Integer.toString(count)));
                        break;
                    case MODE_MONTH:
                        //<string name="label_month_details">%1$s (%2$s records)</string>
                        if (sameMonth) {
                            sb.append(yearMonthFormat.format(targetStartDate));
                        } else {
                            sb.append(yearFormat.format(targetStartDate));
                            sb.append(" ").append(nonDigitalMonthFormat.format(targetStartDate)).append(", ").append(cal.dayOfMonth(targetStartDate)).append(" - ")
                                    .append(nonDigitalMonthFormat.format(targetEndDate)).append(" ").append(cal.dayOfMonth(targetEndDate));
                        }
                        vInfo.setText(i18n.string(R.string.label_month_records, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_DAY:
                        vInfo.setText(i18n.string(R.string.label_day_records, dateFormat.format(targetDate) + " " + weekDayFormat.format(targetDate), Integer.toString(count)));
                        break;
                    case MODE_YEAR:

                        //<string name="label_year_details">%1$s (%2$s records)</string>
                        if (sameYear) {
                            sb.append(yearFormat.format(targetStartDate));
                        } else {
                            sb.append(yearFormat.format(targetStartDate));
                            sb.append(", ").append(nonDigitalMonthFormat.format(targetStartDate)).append(" ").append(cal.dayOfMonth(targetStartDate)).append(" - ")
                                    .append(yearFormat.format(targetEndDate)).append(" ").append(nonDigitalMonthFormat.format(targetEndDate)).append(" ").append(cal.dayOfMonth(targetEndDate));
                        }

                        vInfo.setText(i18n.string(R.string.label_year_records, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_WEEK:
                    default:
                        //<string name="label_week_details">%5$s %1$s to %2$s - Week %3$s/%4$s (%6$s)</string>
                        vInfo.setText(i18n.string(R.string.label_week_records, nonDigitalMonthFormat.format(targetStartDate) + " " + cal.dayOfMonth(targetStartDate),
                                (!sameMonth ? nonDigitalMonthFormat.format(targetEndDate) + " " : "") + cal.dayOfMonth(targetEndDate),
                                cal.weekOfMonth(targetDate), cal.weekOfYear(targetDate), yearFormat.format(targetStartDate), Integer.toString(count)));
                        break;
                }

            }
        });
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);

        super.onStart();

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordMgntFrag.ON_FRAGMENT_START);
        eb.withData(new FragInfo(pos, targetDate, targetStartDate, targetEndDate));
        lookupQueue().publish(eb.build());


    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordMgntFrag.ON_FRAGMENT_STOP);
        eb.withData(pos);
        lookupQueue().publish(eb.build());


    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.RecordMgntFrag.ON_RELOAD_FRAGMENT:
                reloadData();
                break;
        }
    }


    static class FragInfo implements Serializable {
        final int pos;
        final Date date;
        final Date startDate;
        final Date endDate;

        public FragInfo(int pos, Date date, Date startDate, Date endDate) {
            this.pos = pos;
            this.date = date;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}