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
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Misc;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class RecordMgntFragment extends ContextsFragment implements EventQueue.EventListener {


    public static final String ARG_TARGET_DATE = RecordListFragment.ARG_TARGET_DATE;
    public static final String ARG_PERIOD_MODE = RecordListFragment.ARG_PERIOD_MODE;
    public static final String ARG_POS = "pos";

    private TextView vInfo;
    private View vSumIncome;
    private View vSumExpense;
    private View vSumAsset;
    private View vSumLiability;
    private View vSumOther;
    private View vSumUnknown;
    private TextView vSumIncomeMoney;
    private TextView vSumExpenseMoney;
    private TextView vSumAssetMoney;
    private TextView vSumLiabilityMoney;
    private TextView vSumOtherMoney;
    private TextView vSumUnknownMoney;

    private Date targetDate;
    private PeriodMode periodMode;
    private int pos;

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
        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }
        pos = args.getInt(ARG_POS, 0);
        targetDate = (Date) args.get(ARG_TARGET_DATE);
        if (targetDate == null) {
            targetDate = new Date();
        }
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        vInfo = rootView.findViewById(R.id.record_info);

        vSumIncome = rootView.findViewById(R.id.sum_income);
        vSumExpense = rootView.findViewById(R.id.sum_expense);
        vSumAsset = rootView.findViewById(R.id.sum_asset);
        vSumLiability = rootView.findViewById(R.id.sum_liability);
        vSumOther = rootView.findViewById(R.id.sum_other);
        vSumUnknown = rootView.findViewById(R.id.sum_unknown);
        vSumIncomeMoney = rootView.findViewById(R.id.sum_income_money);
        vSumExpenseMoney = rootView.findViewById(R.id.sum_expense_money);
        vSumAssetMoney = rootView.findViewById(R.id.sum_asset_money);
        vSumLiabilityMoney = rootView.findViewById(R.id.sum_liability_money);
        vSumOtherMoney = rootView.findViewById(R.id.sum_other_money);
        vSumUnknownMoney = rootView.findViewById(R.id.sum_unknown_money);

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
            b.putSerializable(RecordListFragment.ARG_PERIOD_MODE, periodMode);
            b.putSerializable(RecordListFragment.ARG_TARGET_DATE, targetDate);
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

        vSumUnknown.setVisibility(TextView.VISIBLE);

        Date[] pDates = Misc.toTargetPeriodDates(periodMode, targetDate);
        targetStartDate = pDates[0];
        targetEndDate = pDates[1];

        final IDataProvider idp = contexts().getDataProvider();

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

                vSumUnknown.setVisibility(TextView.GONE);

                if (income != 0) {
                    vSumIncomeMoney.setText(contexts().toFormattedMoneyString((income)));
                    vSumIncome.setVisibility(TextView.VISIBLE);
                }
                if (expense != 0) {
                    vSumExpenseMoney.setText(contexts().toFormattedMoneyString((expense)));
                    vSumExpense.setVisibility(TextView.VISIBLE);
                }
                if (asset != 0) {
                    vSumAssetMoney.setText(contexts().toFormattedMoneyString((asset)));
                    vSumAsset.setVisibility(TextView.VISIBLE);
                }
                if (liability != 0) {
                    vSumLiabilityMoney.setText(contexts().toFormattedMoneyString((liability)));
                    vSumLiability.setVisibility(TextView.VISIBLE);
                }
                if (other != 0) {
                    vSumOtherMoney.setText(contexts().toFormattedMoneyString((other)));
                    vSumOther.setVisibility(TextView.VISIBLE);
                }

                vInfo.setText(Misc.toRecordPeriodInfo(periodMode, targetDate, count));
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