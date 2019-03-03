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
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class RecordSearcherFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_POS = "pos";

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
        rootView = inflater.inflate(R.layout.record_searcher_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
    }


    private void initArgs() {
        Bundle args = getArguments();
        pos = args.getInt(ARG_POS, 0);
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

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
            b.putSerializable(RecordListFragment.ARG_PERIOD_MODE, PeriodMode.ALL);
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

    private void reloadData(List<Record> data) {

        final CalendarHelper cal = calendarHelper();
        vSumIncome.setVisibility(TextView.GONE);
        vSumExpense.setVisibility(TextView.GONE);
        vSumAsset.setVisibility(TextView.GONE);
        vSumLiability.setVisibility(TextView.GONE);
        vSumOther.setVisibility(TextView.GONE);

        vSumUnknown.setVisibility(TextView.VISIBLE);

        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(data).withArg(RecordListFragment.ARG_POS, pos).build());

        vSumUnknown.setVisibility(TextView.GONE);

        double income, expense, asset, liability, other;
        income = expense = asset = liability = other = 0D;
        for(Record r: data){
            switch(AccountType.find(r.getFromType())){
                case INCOME:
                    income -= r.getMoney();
                    break;
                case EXPENSE:
                    expense -= r.getMoney();
                    break;
                case ASSET:
                    asset -= r.getMoney();
                    break;
                case LIABILITY:
                    liability -= r.getMoney();
                    break;
                case OTHER:
                    other -= r.getMoney();
                    break;
            }
            switch(AccountType.find(r.getToType())){
                case INCOME:
                    income += r.getMoney();
                    break;
                case EXPENSE:
                    expense += r.getMoney();
                    break;
                case ASSET:
                    asset += r.getMoney();
                    break;
                case LIABILITY:
                    liability += r.getMoney();
                    break;
                case OTHER:
                    other += r.getMoney();
                    break;
            }
        }
        //negative
        income = - income;
        liability = -liability;




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

    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);

        super.onStart();

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordSearcherFrag.ON_FRAGMENT_START);
        eb.withData(new FragInfo(pos, targetDate, targetStartDate, targetEndDate));
        lookupQueue().publish(eb.build());


    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordSearcherFrag.ON_FRAGMENT_STOP);
        eb.withData(pos);
        lookupQueue().publish(eb.build());


    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.RecordSearcherFrag.ON_RELOAD_FRAGMENT:
                reloadData((List<Record>) event.getData());
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