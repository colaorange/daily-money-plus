package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

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
public class BalanceActivity extends ContextsActivity {

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;

    public static final String ARG_BALANCE_DATE = "balanceDate";
    public static final String ARG_MODE = "mode";
    //    public static final String ARG_TARGET_DATE = "target";
    public static final String ARG_TOTAL_MODE = "modeTotal";

    private TextView vInfo;

    private Date targetDate;
    private Date currentDate;
    private int mode = MODE_MONTH;
    private boolean totalMode = false;

    private DateFormat monthDateFormat;
    private DateFormat yearMonthFormat;
    private DateFormat yearFormat;

    private Date currentStartDate;
    private Date currentEndDate;

    private ActionMode actionMode;
    private Balance actionObj;

    private List<Balance> recyclerDataList;
    private RecyclerView vRecycler;
    private BalanceRecyclerAdapter recyclerAdapter;
    private LayoutInflater inflater;

    private GUIs.Dimen textSize;
    private GUIs.Dimen textSizeMedium;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance);
        initArgs();
        initMembers();
        refreshUI();
    }

    private void initArgs() {
        Bundle b = getIntentExtras();
        mode = b.getInt(ARG_MODE, MODE_MONTH);
        totalMode = b.getBoolean(ARG_TOTAL_MODE, true);
        Object o = b.get(ARG_BALANCE_DATE);
        if (o instanceof Date) {
            targetDate = (Date) o;
        } else {
            targetDate = new Date();
        }
        currentDate = targetDate;
    }

    private void initMembers() {
        Preference pref = preference();
        monthDateFormat = pref.getMonthDateFormat();//new SimpleDateFormat("MM/dd");
        yearMonthFormat = pref.getYearMonthFormat();//new SimpleDateFormat("yyyy/MM");
        yearFormat = pref.getYearFormat();//new SimpleDateFormat("yyyy");
        textSize = GUIs.toDimen(resolveThemeAttr(R.attr.textSize).data);
        textSizeMedium = GUIs.toDimen(resolveThemeAttr(R.attr.textSizeMedium).data);

        vInfo = findViewById(R.id.balance_info);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BalanceRecyclerAdapter(this, recyclerDataList);
        vRecycler = findViewById(R.id.balance_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(this));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Balance>() {
            @Override
            public void onSelect(Set<Balance> selection) {
                doSelectBalance(selection.size() == 0 ? null : selection.iterator().next());
            }
        });
    }

    private void doSelectBalance(Balance balance) {
        if (balance == null && actionMode != null) {
            actionMode.finish();
            return;
        }

        if (balance != null) {
            actionObj = balance;
            if (actionMode == null) {
                actionMode = this.startSupportActionMode(new BalanceActionModeCallback());
            } else {
                actionMode.invalidate();
            }
            actionMode.setTitle(balance.getName());
        }

    }

    private void refreshUI() {

        refreshToolbar();

        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                reloadData();
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


    private void reloadData() {
        CalendarHelper cal = calendarHelper();
        currentEndDate = null;
        currentStartDate = null;
        vInfo.setText("");
        refreshToolbar();

        trackEvent(TE.BALANCE + mode);

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

                recyclerDataList.clear();
                recyclerDataList.addAll(all);
                recyclerAdapter.notifyDataSetChanged();


                // update info
                if (totalMode) {
                    if (mode == MODE_MONTH) {
                        Date monthStart = cal.monthStartDate(currentDate);
                        Date monthEnd = cal.monthEndDate(currentDate);
                        vInfo.setText(i18n.string(R.string.label_balance_mode_month_total, yearMonthFormat.format(monthStart),
                                monthDateFormat.format(monthEnd)));
                    } else if (mode == MODE_YEAR) {
                        Date yearStart = cal.yearStartDate(currentDate);
                        Date yearEnd = cal.yearEndDate(currentDate);

                        vInfo.setText(i18n.string(R.string.label_balance_mode_year_total, yearFormat.format(yearStart),
                                yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
                    }
                } else {
                    if (mode == MODE_MONTH) {
                        Date monthStart = cal.monthStartDate(currentDate);
                        Date monthEnd = cal.monthEndDate(currentDate);
                        vInfo.setText(i18n.string(R.string.label_balance_mode_month, yearMonthFormat.format(monthStart),
                                monthDateFormat.format(monthStart), monthDateFormat.format(monthEnd)));
                    } else if (mode == MODE_YEAR) {
                        Date yearStart = cal.yearStartDate(currentDate);
                        Date yearEnd = cal.yearEndDate(currentDate);
                        vInfo.setText(i18n.string(R.string.label_balance_mode_year, yearFormat.format(yearStart),
                                monthDateFormat.format(yearStart), yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd)));
                    }
                }
            }
        });

    }

    private void doChangeMode() {
        switch (mode) {
            case MODE_MONTH:
                mode = MODE_YEAR;
                reloadData();
                break;
            case MODE_YEAR:
                mode = MODE_MONTH;
                reloadData();
                break;
        }
    }

    private void doNext() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_MONTH:
                currentDate = cal.monthAfter(currentDate, 1);
                reloadData();
                break;
            case MODE_YEAR:
                currentDate = cal.yearAfter(currentDate, 1);
                reloadData();
                break;
        }
    }

    private void doPrev() {
        CalendarHelper cal = calendarHelper();
        switch (mode) {
            case MODE_MONTH:
                currentDate = cal.monthBefore(currentDate, 1);
                reloadData();
                break;
            case MODE_YEAR:
                currentDate = cal.yearBefore(currentDate, 1);
                reloadData();
                break;
        }
    }

    private void doGoToday() {
        switch (mode) {
            case MODE_MONTH:
            case MODE_YEAR:
                currentDate = targetDate;
                reloadData();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.balance_menu, menu);
        menu.findItem(R.id.menu_hierarchy).setChecked(preference().isHierarchicalBalance());

        MenuItem menuItem = menu.findItem(R.id.menu_operations);
        ActionMenuView amView = (ActionMenuView) menuItem.getActionView();

        //don't have a way to set align right of buttons
//        ActionMenuView.LayoutParams lp = new ActionMenuView.LayoutParams(0, ActionMenuView.LayoutParams.WRAP_CONTENT);
//        amView.setLayoutParams(lp);
//        amView.setGravity(Gravity.RIGHT);
//        amView.setBackgroundColor(Color.RED);

        Menu menuObject = amView.getMenu();
        inflater.inflate(R.menu.balance_operations_menu, menuObject);

        amView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_prev) {
                    doPrev();
                } else if (item.getItemId() == R.id.menu_next) {
                    doNext();
                } else if (item.getItemId() == R.id.menu_go_today) {
                    doGoToday();
                } else if (item.getItemId() == R.id.menu_change_mode) {
                    doChangeMode();
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_yearly_runchart) {
            doYearlyRunChart();
            return true;
        } else if (item.getItemId() == R.id.menu_hierarchy) {
            item.setChecked(!item.isChecked());
            preference().setHierarchicalBalance(!preference().isHierarchicalBalance());
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void doRecordList(Balance balance) {
        if (balance.getTarget() == null) {
            //TODO some message
            return;
        }

        Intent intent = null;
        intent = new Intent(this, AccountRecordListActivity.class);
        if (currentStartDate != null) {
            intent.putExtra(AccountRecordListActivity.ARG_START, currentStartDate);
        }
        if (currentEndDate != null) {
            intent.putExtra(AccountRecordListActivity.ARG_END, currentEndDate);
        }
        intent.putExtra(AccountRecordListActivity.ARG_TARGET, balance.getTarget());
        intent.putExtra(AccountRecordListActivity.ARG_TARGET_INFO, balance.getName());
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
        }
    }


    private void doPieChart(final Balance b) {


        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
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
                trackEvent(TE.CHART + "pie");
                Intent intent = new BalancePieChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(at, list);
                startActivity(intent);
            }
        });
    }

    private void doYearlyTimeChart(final Balance b) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();
                I18N i18n = i18n();
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
                trackEvent(TE.CHART + "yt");
                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(
                        i18n.string(R.string.label_balance_yearly_timechart, at.getDisplay(i18n), yearFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }

    private void doYearlyCumulativeTimeChart(final Balance b) {
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @Override
            public void run() {
                CalendarHelper calHelper = calendarHelper();
                I18N i18n = i18n();

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
                trackEvent(TE.CHART + "yct");
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
                trackEvent(TE.CHART + "yr");
                Intent intent = new BalanceTimeChart(BalanceActivity.this, GUIs.getOrientation(BalanceActivity.this), GUIs.getDPRatio(BalanceActivity.this)).createIntent(
                        i18n.string(R.string.label_balance_yearly_runchart, yearFormat.format(currentDate)), balances);
                startActivity(intent);
            }
        });
    }


    public class BalanceRecyclerAdapter extends SelectableRecyclerViewAdaptor<Balance, BalanceViewHolder> {

        public BalanceRecyclerAdapter(ContextsActivity activity, List<Balance> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.balance_item, parent, false);
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

            Map<AccountType, Integer> textColorMap = getAccountTextColorMap();
            Map<AccountType, Integer> bgColorMap = getAccountBgColorMap();
            boolean lightTheme = isLightTheme();
            float dpRatio = getDpRatio();

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
            if (head) {

                int bg = mask & resolveThemeAttrResData(R.attr.balanceHeadBgColor);
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
                int bg = mask & resolveThemeAttrResData(R.attr.balanceItemBgColor);
                if (selected) {
                    bg = Colors.darken(bg, 0.07f);
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

            int gpd = (int) (10 * dpRatio);
            vlayout.setPadding((int) ((1 + indent) * gpd), gpd, gpd, gpd);


            vname.setTextColor(textColor);
            vmoney.setTextColor(textColor);

        }
    }

    private class BalanceActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.balance_item_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels
            MenuItem mi = menu.findItem(R.id.menu_reclist);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (null == actionObj.getTarget()) {
                mi.setEnabled(false);
            } else {
                mi.setEnabled(true);
            }
            mi.setIcon(buildDisabledIcon(resolveThemeAttrResId(R.attr.ic_list), mi.isEnabled()));


            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_piechart) {
                doPieChart(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_yearly_serieschart) {
                doYearlyTimeChart(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_yearly_cumulative_serieschart) {
                doYearlyCumulativeTimeChart(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_reclist) {
                doRecordList(actionObj);
                return true;
            }
            return false;
        }

        //onDestroyActionMode(ActionMode) when the action mode is closed.
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            actionMode = null;
            actionObj = null;
            recyclerAdapter.clearSelection();
        }


    }
}
