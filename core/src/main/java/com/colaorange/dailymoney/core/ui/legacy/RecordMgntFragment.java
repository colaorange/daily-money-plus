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
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class RecordMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final int MODE_DAY = 0;
    public static final int MODE_WEEK = 1;
    public static final int MODE_MONTH = 2;
    public static final int MODE_YEAR = 3;
    public static final int MODE_ALL = 4;


    public static final String ARG_TARGET_DATE = "targetDate";
    public static final String ARG_MODE = "mode";
    public static final String ARG_POS = "pos";

    private TextView vInfo;
    TextView vSumIncome;
    TextView vSumExpense;
    TextView vSumAsset;
    TextView vSumLiability;
    TextView vSumOther;
    TextView vSumUnknow;

    private Date targetDate;
    private int mode;
    private int pos;

    private DateFormat dateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;
    private DateFormat nonDigitalMonthFormat;
    private DateFormat weekDayFormat;

    private Date targetStartDate;
    private Date targetEndDate;


    private List<Record> recyclerDataList;
    private RecyclerView vRecycler;
    private RecordRecyclerAdapter recyclerAdapter;
    private LayoutInflater inflater;

    private GUIs.Dimen textSize;
    private GUIs.Dimen textSizeMedium;

    private View rootView;

    private int recordListLayout;
    private Map<String, Account> accountMap = new HashMap<String, Account>();
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
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        lightTheme = activity.isLightTheme();

        Preference preference = preference();
        recordListLayout = preference.getRecordListLayout();

        dateFormat = preference.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
//        monthDateFormat = preference.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = preference.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM - MMM");
        yearFormat = preference.getYearFormat();//new SimpleDateFormat("yyyy");
        nonDigitalMonthFormat = preference.getNonDigitalMonthFormat();
        weekDayFormat = preference.getWeekDayFormat();// Wed.
        textSize = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSize).data);
        textSizeMedium = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSizeMedium).data);

        vInfo = rootView.findViewById(R.id.record_info);
        vSumIncome = rootView.findViewById(R.id.sum_income);
        vSumExpense = rootView.findViewById(R.id.sum_expense);
        vSumAsset = rootView.findViewById(R.id.sum_asset);
        vSumLiability = rootView.findViewById(R.id.sum_liability);
        vSumOther = rootView.findViewById(R.id.sum_other);
        vSumUnknow = rootView.findViewById(R.id.sum_unknow);

        vInfo = rootView.findViewById(R.id.record_info);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new RecordRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.record_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Record>() {
            @Override
            public void onSelect(Set<Record> selection) {
                lookupQueue().publish(QEvents.RecordMgnt.ON_SELECT_RECORD, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Record selected) {
                lookupQueue().publish(QEvents.RecordMgnt.ON_RESELECT_RECORD, selected);
                return true;
            }
        });

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

        accountMap.clear();
        for (Account acc : idp.listAccount(null)) {
            accountMap.put(acc.getId(), acc);
        }

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
                I18N i18n = i18n();

                recyclerDataList.clear();
                recyclerDataList.addAll(data);
                recyclerAdapter.notifyDataSetChanged();

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
                        vInfo.setText(i18n.string(R.string.label_month_details, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_DAY:
                        vInfo.setText(i18n.string(R.string.label_day_records, dateFormat.format(targetDate), Integer.toString(count)));
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

                        vInfo.setText(i18n.string(R.string.label_year_details, sb.toString(), Integer.toString(count)));
                        break;
                    case MODE_WEEK:
                    default:
                        //<string name="label_week_details">%5$s %1$s to %2$s - Week %3$s/%4$s (%6$s)</string>
                        vInfo.setText(i18n.string(R.string.label_week_details, nonDigitalMonthFormat.format(targetStartDate) + " " + cal.dayOfMonth(targetStartDate),
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

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordMgnt.ON_FRAGMENT_START);
        eb.withData(new FragInfo(pos, targetDate, targetStartDate, targetEndDate));
        lookupQueue().publish(eb.build());


    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);

        EventQueue.EventBuilder eb = new EventQueue.EventBuilder(QEvents.RecordMgnt.ON_FRAGMENT_STOP);
        eb.withData(pos);
        lookupQueue().publish(eb.build());



    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.RecordMgnt.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.RecordMgnt.ON_RELOAD_FRAGMENT:
                reloadData();
                break;
        }
    }

    public class RecordRecyclerAdapter extends SelectableRecyclerViewAdaptor<Record, RecordViewHolder> {

        public RecordRecyclerAdapter(ContextsActivity activity, List<Record> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int itemLayout;
            switch (recordListLayout) {
                case 2:
                    itemLayout = R.layout.record_list_item2;
                    break;
                case 3:
                    itemLayout = R.layout.record_list_item3;
                    break;
                case 4:
                    itemLayout = R.layout.record_list_item4;
                    break;
                case 1:
                default:
                    itemLayout = R.layout.record_list_item1;
            }

            View viewItem = inflater.inflate(R.layout.record_mgnt_item, parent, false);
            inflater.inflate(itemLayout, (ViewGroup) viewItem.findViewById(R.id.layout_select), true);
            return new RecordViewHolder(this, viewItem);
        }

    }

    public class RecordViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, Record> {

        public RecordViewHolder(RecordRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Record record) {
            super.bindViewValue(record);


//            ContextsActivity activity = getContextsActivity();


//            float dpRatio = activity.getDpRatio();

            LinearLayout vlayout = itemView.findViewById(R.id.record_item_layout);
            LinearLayout vfromborder = itemView.findViewById(R.id.record_item_from_border);
            LinearLayout vtoborder = itemView.findViewById(R.id.record_item_to_border);
            TextView vfrom = itemView.findViewById(R.id.detail_mgnt_item_from);
            TextView vto = itemView.findViewById(R.id.detail_mgnt_item_to);
            TextView vmoney = itemView.findViewById(R.id.detail_mgnt_item_money);
            TextView vnote = itemView.findViewById(R.id.detail_mgnt_item_note);
            TextView vdate = itemView.findViewById(R.id.detail_mgnt_item_date);


            Account fromAcc = accountMap.get(record.getFrom());
            Account toAcc = accountMap.get(record.getTo());

            AccountType fromAccType = fromAcc == null ? AccountType.UNKONW : AccountType.find(fromAcc.getType());
            AccountType toAccType = toAcc == null ? AccountType.UNKONW : AccountType.find(toAcc.getType());

            //transparent mask for selecting ripple effect
            int mask = 0xE0FFFFFF;
            boolean selected = adaptor.isSelected(record);

            int bgcolor;
            bgcolor = mask & accountBgColorMap.get(toAccType);
            if(selected){
                if(lightTheme){
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                }else{
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }

            }
            vlayout.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(toAccType);
            if(selected){
                if(lightTheme){
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                }else{
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vtoborder.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(fromAccType);
            if(selected){
                if(lightTheme){
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                }else{
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vfromborder.setBackgroundColor(bgcolor);

            vto.setTextColor(accountTextColorMap.get(toAccType));
            vfrom.setTextColor(accountTextColorMap.get(fromAccType));
            vnote.setTextColor(accountTextColorMap.get(toAccType));


            //
            String from = fromAcc == null ? record.getFrom() : (i18n.string(R.string.label_reclist_from, fromAcc.getName(), AccountType.getDisplay(i18n, fromAcc.getType())));
            String to = toAcc == null ? record.getTo() : (i18n.string(R.string.label_reclist_to, toAcc.getName(), AccountType.getDisplay(i18n, toAcc.getType())));
            String money = Contexts.instance().toFormattedMoneyString(record.getMoney());
            String date = dateFormat.format(record.getDate()) + " " + weekDayFormat.format(record.getDate());

            vfrom.setText(from);
            vto.setText(to);
            vmoney.setText(money);
            vnote.setText(record.getNote());
            vdate.setText(date);

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