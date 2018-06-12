package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.PeriodInfoFragment;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class BalanceMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";
    public static final String ARG_POS = "pos";

    private Date targetDate;
    private PeriodMode periodMode;
    private boolean fromBeginning = false;
    private int pos;

    private DateFormat monthDateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;

    private Date targetStartDate;
    private Date targetEndDate;

    private List<Balance> recyclerDataList;
    private RecyclerView vRecycler;
    private BalanceRecyclerAdapter recyclerAdapter;
    private LayoutInflater inflater;

    private GUIs.Dimen textSize;
    private GUIs.Dimen textSizeMedium;

    private View rootView;

    I18N i18n;

    static Set<PeriodMode> supportPeriod = com.colaorange.commons.util.Collections.asSet(PeriodMode.MONTHLY, PeriodMode.YEARLY);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.balance_mgnt_frag, container, false);
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

        pos = args.getInt(ARG_POS, 0);
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
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Preference pref = preference();
        monthDateFormat = pref.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = pref.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM");
        yearFormat = pref.getYearFormat();//new SimpleDateFormat("yyyy");
        textSize = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSize).data);
        textSizeMedium = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSizeMedium).data);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BalanceRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.balance_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Balance>() {
            @Override
            public void onSelect(Set<Balance> selection) {
                lookupQueue().publish(QEvents.BalanceMgntFrag.ON_SELECT_BALANCE, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Balance selected) {
                lookupQueue().publish(QEvents.BalanceMgntFrag.ON_RESELECT_BALANCE, selected);
                return true;
            }
        });

        String fragTag = "periodInfo";
        if (getChildFragmentManager().findFragmentByTag(fragTag) == null) {
            PeriodInfoFragment f = new PeriodInfoFragment();
            Bundle b = new Bundle();
            b.putBoolean(PeriodInfoFragment.ARG_FROM_BEGINNING, fromBeginning);
            b.putSerializable(PeriodInfoFragment.ARG_PERIOD_MODE, periodMode);
            b.putSerializable(PeriodInfoFragment.ARG_TARGET_DATE, targetDate);
            f.setArguments(b);

            getChildFragmentManager().beginTransaction()
                    .add(R.id.frag_container, f, fragTag)
                    .disallowAddToBackStack()
                    .commit();
        }

    }

    private void reloadData() {
        CalendarHelper cal = calendarHelper();
        targetEndDate = null;
        targetStartDate = null;

        switch (periodMode) {
            case YEARLY:
                targetEndDate = cal.yearEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.yearStartDate(targetDate);
                break;
            default:
                targetEndDate = cal.monthEndDate(targetDate);
                targetStartDate = fromBeginning ? null : cal.monthStartDate(targetDate);
                break;
        }

        GUIs.doBusy(getContextsActivity(), new GUIs.BusyAdapter() {
            List<Balance> all = new ArrayList<Balance>();

            @Override
            public void run() {
                boolean hierarchical = preference().isHierarchicalBalance();

                List<Balance> asset = BalanceHelper.calculateBalanceList(AccountType.ASSET, targetStartDate, targetEndDate);
                List<Balance> income = BalanceHelper.calculateBalanceList(AccountType.INCOME, targetStartDate, targetEndDate);
                List<Balance> expense = BalanceHelper.calculateBalanceList(AccountType.EXPENSE, targetStartDate, targetEndDate);
                List<Balance> liability = BalanceHelper.calculateBalanceList(AccountType.LIABILITY, targetStartDate, targetEndDate);
                List<Balance> other = BalanceHelper.calculateBalanceList(AccountType.OTHER, targetStartDate, targetEndDate);


                if (hierarchical) {
                    asset = BalanceHelper.adjustNestedTotalBalance(AccountType.ASSET, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustNestedTotalBalance(AccountType.INCOME, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.EXPENSE,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.LIABILITY,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustNestedTotalBalance(AccountType.OTHER, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_other)
                            : i18n.string(R.string.label_other), other);

                } else {
                    asset = BalanceHelper.adjustTotalBalance(AccountType.ASSET, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustTotalBalance(AccountType.INCOME, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustTotalBalance(
                            AccountType.EXPENSE,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustTotalBalance(
                            AccountType.LIABILITY,
                            fromBeginning ? i18n.string(R.string.label_balance_from_beginning_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustTotalBalance(AccountType.OTHER, fromBeginning ? i18n.string(R.string.label_balance_from_beginning_other)
                            : i18n.string(R.string.label_other), other);

                }

                if (fromBeginning) {
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(other);
                } else {
                    all.addAll(income);
                    all.addAll(expense);
                    all.addAll(asset);
                    all.addAll(liability);
                    all.addAll(other);
                }
            }

            @Override
            public void onBusyFinish() {
                I18N i18n = i18n();
                CalendarHelper cal = calendarHelper();

                recyclerDataList.clear();
                recyclerDataList.addAll(all);
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_FRAGMENT_START);
        eb.withData(new FragInfo(pos, targetDate, targetStartDate, targetEndDate));
        lookupQueue().publish(eb.build());

    }

    @Override
    public void onStop() {
        super.onStop();
        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_FRAGMENT_STOP);
        eb.withData(pos);
        lookupQueue().publish(eb.build());

        lookupQueue().unsubscribe(this);

    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.BalanceMgntFrag.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.BalanceMgntFrag.ON_RELOAD_FRAGMENT:
                reloadData();
                break;
        }
    }

    public class BalanceRecyclerAdapter extends SelectableRecyclerViewAdaptor<Balance, BalanceViewHolder> {

        public BalanceRecyclerAdapter(ContextsActivity activity, List<Balance> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.balance_mgnt_item, parent, false);
            return new BalanceViewHolder(this, viewItem);
        }

    }

    public class BalanceViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<BalanceRecyclerAdapter, Balance> {

        public BalanceViewHolder(BalanceRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Balance balance) {
            super.bindViewValue(balance);


            ContextsActivity activity = getContextsActivity();

            Map<AccountType, Integer> textColorMap = activity.getAccountTextColorMap();
            Map<AccountType, Integer> bgColorMap = activity.getAccountBgColorMap();
            boolean lightTheme = activity.isLightTheme();
            float dpRatio = activity.getDpRatio();

            LinearLayout vlayout = itemView.findViewById(R.id.balance_item_layout);
            TextView vname = itemView.findViewById(R.id.balance_item_name);
            TextView vmoney = itemView.findViewById(R.id.balance_item_money);

            AccountType at = AccountType.find(balance.getType());
            int indent = balance.getIndent();
            boolean head = indent == 0;

            Integer textColor;

            vname.setText(balance.getName());
            vmoney.setText(contexts().toFormattedMoneyString(balance.getMoney()));

            boolean selected = adaptor.isSelected(balance);

            //transparent mask for selecting ripple effect
            int mask = 0xE0FFFFFF;
            int hpd = (int) (10 * dpRatio);
            int vpd = (int) (6 * dpRatio);

            if (head) {

                vpd += 2;

                int bg = mask & activity.resolveThemeAttrResData(R.attr.balanceHeadBgColor);
                if (selected) {
                    bg = Colors.lighten(bg, 0.15f);
                }
                vlayout.setBackgroundColor(bg);
                if (!lightTheme) {
                    textColor = textColorMap.get(at);
                } else {
                    textColor = bgColorMap.get(at);
                }

                vname.setTextSize(textSizeMedium.unit, textSizeMedium.value);
                vmoney.setTextSize(textSizeMedium.unit, textSizeMedium.value);
            } else {
                int bg = mask & activity.resolveThemeAttrResData(R.attr.balanceItemBgColor);
                if (selected) {
                    bg = Colors.darken(bg, 0.15f);
                }
                vlayout.setBackgroundColor(bg);
                if (lightTheme) {
                    textColor = textColorMap.get(at);
                } else {
                    textColor = bgColorMap.get(at);
                }
                vname.setTextSize(textSize.unit, textSize.value);
                vmoney.setTextSize(textSize.unit, textSize.value);
            }


            vlayout.setPadding((int) ((1 + indent) * hpd), vpd, hpd, vpd);


            vname.setTextColor(textColor);
            vmoney.setTextColor(textColor);

        }
    }

    static public class FragInfo implements Serializable {
        final public int pos;
        final public Date date;
        final public Date startDate;
        final public Date endDate;

        public FragInfo(int pos, Date date, Date startDate, Date endDate) {
            this.pos = pos;
            this.date = date;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}