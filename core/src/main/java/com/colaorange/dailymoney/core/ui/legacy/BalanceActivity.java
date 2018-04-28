package com.colaorange.dailymoney.core.ui.legacy;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * @author dennis
 */
public class BalanceActivity extends ContextsActivity implements OnClickListener, OnItemClickListener {

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;

    public static final String PARAM_BALANCE_DATE = "balance.balanceDate";
    public static final String PARAM_MODE = "balance.mode";
    //    public static final String PARAM_TARGET_DATE = "target";
    public static final String PARAM_TOTAL_MODE = "balance.modeTotal";

    TextView infoView;
    View toolbarView;

    private Date targetDate;
    private Date currentDate;
    private int mode = MODE_MONTH;
    private boolean totalMode = false;

    private DateFormat monthDateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;

    private Date currentStartDate;
    private Date currentEndDate;

    ImageButton modeBtn;


    private List<Balance> listViewData = new ArrayList<Balance>();

    private ListView listView;

    private BalanceAdapter listViewAdapter;

    private float dpRatio;

    private boolean lightTheme;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance);
        initParams();
        initMembers();
        refreshUI();

    }

    private void initParams() {
        Bundle b = getIntentExtras();
        mode = b.getInt(PARAM_MODE, MODE_MONTH);
        totalMode = b.getBoolean(PARAM_TOTAL_MODE, true);
        Object o = b.get(PARAM_BALANCE_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
        currentDate = targetDate;
    }

    private void initMembers() {
        dpRatio = GUIs.getDPRatio(BalanceActivity.this);
        Preference pref = preference();
        monthDateFormat = pref.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = pref.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM");
        yearFormat = pref.getYearFormat();//new SimpleDateFormat("yyyy");

        infoView = findViewById(R.id.balance_info);
        toolbarView = findViewById(R.id.balance_toolbar);

        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_today).setOnClickListener(this);
        modeBtn = findViewById(R.id.btn_mode);
        modeBtn.setOnClickListener(this);


        listViewAdapter = new BalanceAdapter(this, listViewData);

        listView = findViewById(R.id.balance_list);
        listView.setAdapter(listViewAdapter);

        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        lightTheme = isLightTheme();

    }

    private void refreshUI() {

        refreshToolbar();

        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        }, 25);

    }


    private void refreshToolbar() {
        if (totalMode) {
            setTitle(R.string.dtitem_report_cumulative_balance);
        } else {
            switch (mode) {
                case MODE_YEAR:
                    setTitle(R.string.dtitem_report_yearly_balance);
                    break;
                case MODE_MONTH:
                default:
                    setTitle(R.string.dtitem_report_monthly_balance);
                    break;
            }
        }
    }


    private void refreshData() {
        CalendarHelper cal = calendarHelper();
        currentEndDate = null;
        currentStartDate = null;
        infoView.setText("");
        refreshToolbar();
        switch (mode) {
            case MODE_YEAR:
                currentEndDate = cal.yearEndDate(currentDate);
                currentStartDate = totalMode ? null : cal.yearStartDate(currentDate);
                break;
            default:
                currentEndDate = cal.monthEndDate(currentDate);
                currentStartDate = totalMode ? null : cal.monthStartDate(currentDate);
                break;
        }
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Balance> all = new ArrayList<Balance>();

            @Override
            public void run() {
                I18N i18n = i18n();
                boolean hierarchical = preference().isHierarchicalBalance();

                List<Balance> asset = BalanceHelper.calculateBalanceList(AccountType.ASSET, currentStartDate, currentEndDate);
                List<Balance> income = BalanceHelper.calculateBalanceList(AccountType.INCOME, currentStartDate, currentEndDate);
                List<Balance> expense = BalanceHelper.calculateBalanceList(AccountType.EXPENSE, currentStartDate, currentEndDate);
                List<Balance> liability = BalanceHelper.calculateBalanceList(AccountType.LIABILITY, currentStartDate, currentEndDate);
                List<Balance> other = BalanceHelper.calculateBalanceList(AccountType.OTHER, currentStartDate, currentEndDate);


                if (hierarchical) {
                    asset = BalanceHelper.adjustNestedTotalBalance(AccountType.ASSET, totalMode ? i18n.string(R.string.label_balance_total_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustNestedTotalBalance(AccountType.INCOME, totalMode ? i18n.string(R.string.label_balance_total_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.EXPENSE,
                            totalMode ? i18n.string(R.string.label_balance_total_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustNestedTotalBalance(
                            AccountType.LIABILITY,
                            totalMode ? i18n.string(R.string.label_balance_total_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustNestedTotalBalance(AccountType.OTHER, totalMode ? i18n.string(R.string.label_balance_total_other)
                            : i18n.string(R.string.label_other), other);

                } else {
                    asset = BalanceHelper.adjustTotalBalance(AccountType.ASSET, totalMode ? i18n.string(R.string.label_balance_total_asset)
                            : i18n.string(R.string.label_asset), asset);
                    income = BalanceHelper.adjustTotalBalance(AccountType.INCOME, totalMode ? i18n.string(R.string.label_balance_total_income)
                            : i18n.string(R.string.label_income), income);
                    expense = BalanceHelper.adjustTotalBalance(
                            AccountType.EXPENSE,
                            totalMode ? i18n.string(R.string.label_balance_total_expense) : i18n
                                    .string(R.string.label_expense), expense);
                    liability = BalanceHelper.adjustTotalBalance(
                            AccountType.LIABILITY,
                            totalMode ? i18n.string(R.string.label_balance_total_liability) : i18n
                                    .string(R.string.label_liability), liability);
                    other = BalanceHelper.adjustTotalBalance(AccountType.OTHER, totalMode ? i18n.string(R.string.label_balance_total_other)
                            : i18n.string(R.string.label_other), other);

                }

                if (totalMode) {
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

                listViewData.clear();
                listViewData.addAll(all);

                listViewAdapter.notifyDataSetChanged();


                // update info
                if (totalMode) {
                    if (mode == MODE_MONTH) {
                        Date monthStart = cal.monthStartDate(currentDate);
                        Date monthEnd = cal.monthEndDate(currentDate);
                        infoView.setText(i18n.string(R.string.label_balance_mode_month_total, yearMonthFormat.format(monthStart),
                                monthDateFormat.format(monthEnd)));
                    } else if (mode == MODE_YEAR) {
                        Date yearStart = cal.yearStartDate(currentDate);
                        Date yearEnd = cal.yearEndDate(currentDate);

                        infoView.setText(i18n.string(R.string.label_balance_mode_year_total, yearFormat.format(yearStart),
                                yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
                    }
                } else {
                    if (mode == MODE_MONTH) {
                        Date monthStart = cal.monthStartDate(currentDate);
                        Date monthEnd = cal.monthEndDate(currentDate);
                        infoView.setText(i18n.string(R.string.label_balance_mode_month, yearMonthFormat.format(monthStart),
                                monthDateFormat.format(monthStart), monthDateFormat.format(monthEnd)));
                    } else if (mode == MODE_YEAR) {
                        Date yearStart = cal.yearStartDate(currentDate);
                        Date yearEnd = cal.yearEndDate(currentDate);
                        infoView.setText(i18n.string(R.string.label_balance_mode_year, yearFormat.format(yearStart),
                                monthDateFormat.format(yearStart), yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_prev) {
            onPrev();
        } else if (v.getId() == R.id.btn_next) {
            onNext();
        } else if (v.getId() == R.id.btn_today) {
            onToday();
        } else if (v.getId() == R.id.btn_mode) {
            onMode();
        }
    }

    private void onMode() {
        switch (mode) {
            case MODE_MONTH:
                mode = MODE_YEAR;
                refreshData();
                break;
            case MODE_YEAR:
                mode = MODE_MONTH;
                refreshData();
                break;
        }
    }

    private void onNext() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_MONTH:
                currentDate = cal.monthAfter(currentDate, 1);
                refreshData();
                break;
            case MODE_YEAR:
                currentDate = cal.yearAfter(currentDate, 1);
                refreshData();
                break;
        }
    }

    private void onPrev() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_MONTH:
                currentDate = cal.monthBefore(currentDate, 1);
                refreshData();
                break;
            case MODE_YEAR:
                currentDate = cal.yearBefore(currentDate, 1);
                refreshData();
                break;
        }
    }

    private void onToday() {
        switch (mode) {
            case MODE_MONTH:
            case MODE_YEAR:
                currentDate = targetDate;
                refreshData();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.balance_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_yearly_runchart) {
            doYearlyRunChart();
            return true;
        } else if (item.getItemId() == R.id.menu_toggle_hierarchy) {
            preference().setHierarchicalBalance(!preference().isHierarchicalBalance());
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == listView) {
            doRecordList(position);
//            doPieChart(position);
        }
    }

    private void doRecordList(int position) {
        Balance b = listViewData.get(position);
        if (b.getTarget() == null) {
            //TODO some message
            return;
        }

        Intent intent = null;
        intent = new Intent(this, AccountRecordListActivity.class);
        if (currentStartDate != null) {
            intent.putExtra(AccountRecordListActivity.PARAM_START, currentStartDate);
        }
        if (currentEndDate != null) {
            intent.putExtra(AccountRecordListActivity.PARAM_END, currentEndDate);
        }
        intent.putExtra(AccountRecordListActivity.PARAM_TARGET, b.getTarget());
        intent.putExtra(AccountRecordListActivity.PARAM_TARGET_INFO, b.getName());
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    refreshData();
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.balance_list) {
//            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;            
            getMenuInflater().inflate(R.menu.balance_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.menu_piechart) {
            doPieChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_yearly_serieschart) {
            doYearlyTimeChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_yearly_cumulative_serieschart) {
            doYearlyCumulativeTimeChart(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_yearly_runchart) {
            doYearlyRunChart();
            return true;
        } else if (item.getItemId() == R.id.menu_reclist) {
            doRecordList(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void doPieChart(final int pos) {


        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }
                List<Balance> list = new ArrayList<Balance>();
                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    Balance balance = BalanceHelper.calculateBalance(acc, currentStartDate, currentEndDate);
                    list.add(balance);
                }
                Intent intent = new BalancePieChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(at, list);
                startActivity(intent);
            }
        });
    }

    private void doYearlyTimeChart(final int pos) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();
                I18N i18n = i18n();
                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }

                List<List<Balance>> balances = new ArrayList<List<Balance>>();


                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = calHelper.yearStartDate(g.getDate());
                    for (int i = 0; i < 12; i++) {
                        Balance balance = BalanceHelper.calculateBalance(acc, calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                        blist.add(balance);
                        d = calHelper.monthAfter(d, 1);
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(
                        i18n.string(R.string.label_balance_yearly_timechart, at.getDisplay(i18n), yearFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }

    private void doYearlyCumulativeTimeChart(final int pos) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();
                I18N i18n = i18n();

                Balance b = listViewData.get(pos);
                AccountType at;
                List<Balance> group = b.getGroup();
                if (b.getTarget() instanceof Account) {
                    group = new ArrayList<Balance>(group);
                    group.remove(b);
                    group.add(0, b);
                    at = AccountType.find(((Account) b.getTarget()).getType());
                } else {
                    at = AccountType.find(b.getType());
                }

                List<List<Balance>> balances = new ArrayList<List<Balance>>();


                for (Balance g : group) {
                    if (!(g.getTarget() instanceof Account)) {
                        continue;
                    }
                    Account acc = (Account) g.getTarget();
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = calHelper.yearStartDate(g.getDate());
                    double total = 0;
                    for (int i = 0; i < 12; i++) {
                        Balance balance = BalanceHelper.calculateBalance(acc, i == 0 ? null : calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                        total += balance.getMoney();
                        balance.setMoney(total);
                        blist.add(balance);
                        d = calHelper.monthAfter(d, 1);
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(
                        i18n.string(R.string.label_balance_yearly_cumulative_timechart, at.getDisplay(i18n), yearFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }


    private void doYearlyRunChart() {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();
                I18N i18n = i18n();

                boolean[] yearly = new boolean[]{false, false, true, true, false};
                AccountType[] ats = new AccountType[]{AccountType.ASSET, AccountType.LIABILITY, AccountType.INCOME, AccountType.EXPENSE, AccountType.OTHER};
                List<List<Balance>> balances = new ArrayList<List<Balance>>();
                Date yearstart = calHelper.yearStartDate(currentDate);
                for (int j = 0; j < ats.length; j++) {
                    AccountType at = ats[j];
                    List<Balance> blist = new ArrayList<Balance>();
                    balances.add(blist);
                    Date d = yearstart;
                    if (yearly[j]) {
                        for (int i = 0; i < 12; i++) {
                            Balance balance = BalanceHelper.calculateBalance(at, yearstart, calHelper.monthEndDate(d));
                            blist.add(balance);
                            d = calHelper.monthAfter(d, 1);
                        }
                    } else {
                        double total = 0;
                        for (int i = 0; i < 12; i++) {
                            Balance balance = BalanceHelper.calculateBalance(at, i == 0 ? null : calHelper.monthStartDate(d), calHelper.monthEndDate(d));
                            total += balance.getMoney();
                            balance.setMoney(total);
                            blist.add(balance);
                            d = calHelper.monthAfter(d, 1);
                        }
                    }
                }

                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(
                        i18n.string(R.string.label_balance_yearly_runchart, yearFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }


    private class BalanceAdapter extends ArrayAdapter<Balance> {

        LayoutInflater inflater;

        public BalanceAdapter(@NonNull Context context, List<Balance> balances) {
            super(context, R.layout.balance_item, balances);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BalanceViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.balance_item, null);
                convertView.setTag(holder = new BalanceViewHolder());
            } else {
                holder = (BalanceViewHolder) convertView.getTag();
            }

            holder.bindViewValue(getItem(position), convertView);

            return convertView;
        }


    }

    public class BalanceViewHolder {

        public void bindViewValue(Balance balance, View convertView) {

            Map<AccountType,Integer> textColorMap = getAccountTextColorMap();
            Map<AccountType,Integer> bgColorMap = getAccountBgColorMap();

            LinearLayout vlayout = convertView.findViewById(R.id.balance_item_layout);
            TextView vname = convertView.findViewById(R.id.balance_item_name);
            TextView vmoney = convertView.findViewById(R.id.balance_item_money);

            AccountType at = AccountType.find(balance.getType());
            int indent = balance.getIndent();
            boolean head = indent == 0;

            Integer textColor;

            vname.setText(balance.getName());
            vmoney.setText(contexts().toFormattedMoneyString(balance.getMoney()));

            if (head) {
                vlayout.setBackgroundColor(resolveThemeAttrResData(R.attr.balanceHeadBgColor));
                if(!lightTheme){
                    textColor = textColorMap.get(at);
                }else {
                    textColor = bgColorMap.get(at);
                }
            }else{
                vlayout.setBackgroundColor(resolveThemeAttrResData(R.attr.balanceItemBgColor));
                if(lightTheme){
                    textColor = textColorMap.get(at);
                }else {
                    textColor = bgColorMap.get(at);
                }
            }

            vlayout.setPadding((int) ((1 + indent) * 10 * dpRatio), (int) (5 * dpRatio), vlayout.getPaddingRight(), (int) (5 * dpRatio));


            vname.setTextColor(textColor);
            vmoney.setTextColor(textColor);
        }
    }
}
