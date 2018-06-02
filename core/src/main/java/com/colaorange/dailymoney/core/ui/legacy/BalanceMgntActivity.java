package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.LineAccountActivity;
import com.colaorange.dailymoney.core.ui.chart.LineFromBeginningAggregateActivity;
import com.colaorange.dailymoney.core.ui.chart.LineAccountFragment;
import com.colaorange.dailymoney.core.ui.chart.LineFromBeginningAggregateFragment;
import com.colaorange.dailymoney.core.ui.chart.PieAccountFragment;
import com.colaorange.dailymoney.core.ui.chart.PieAccountActivity;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dennis
 */
public class BalanceMgntActivity extends ContextsActivity implements EventQueue.EventListener {

    public static final int MODE_MONTH = 0;
    public static final int MODE_YEAR = 1;

    public static final String ARG_BASE_DATE = "baseDate";
    public static final String ARG_MODE = "mode";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";

    private ViewPager vPager;
    BalancePagerAdapter adapter;

    private Date baseDate;

    private int mode = MODE_MONTH;
    private boolean fromBeginning = false;

    private DateFormat yearFormat;

    private Map<Integer, BalanceMgntFragment.FragInfo> fragInfoMap;

    private ActionMode actionMode;
    private Balance actionObj;
    I18N i18n;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_mgnt);
        initArgs();
        initMembers();
        refreshToolbar();
        resetPager();
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
    }

    private void initArgs() {
        Bundle b = getIntentExtras();
        mode = b.getInt(ARG_MODE, MODE_MONTH);
        fromBeginning = b.getBoolean(ARG_FROM_BEGINNING, false);
        Object o = b.get(ARG_BASE_DATE);
        if (o instanceof Date) {
            baseDate = (Date) o;
        } else {
            baseDate = new Date();
        }
    }

    private void initMembers() {
        i18n = i18n();
        Preference pref = preference();
        yearFormat = pref.getYearFormat();//new SimpleDateFormat("yyyy");
        vPager = findViewById(R.id.viewpager);
        vPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_CLEAR_SELECTION).build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        fragInfoMap = new LinkedHashMap<>();
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


    private void refreshToolbar() {
        if (fromBeginning) {
            setTitle(R.string.nav_pg_report_from_beginning_balance);
        } else {
            switch (mode) {
                case MODE_YEAR:
                    setTitle(R.string.nav_pg_report_yearly_balance);
                    break;
                case MODE_MONTH:
                default:
                    setTitle(R.string.nav_pg_report_monthly_balance);
                    break;
            }
        }
    }

    private void resetPager() {
        fragInfoMap.clear();
        adapter = new BalancePagerAdapter(getSupportFragmentManager());
        vPager.setAdapter(adapter);
        vPager.setCurrentItem(adapter.getBasePos());

        trackEvent(ContextsActivity.TE.BALANCE + mode);
    }

    private void doChangeMode() {
        switch (mode) {
            case MODE_MONTH:
                mode = MODE_YEAR;
                break;
            case MODE_YEAR:
                mode = MODE_MONTH;
                break;
        }
        refreshToolbar();
        resetPager();
    }

    private void doNext() {
        int c = vPager.getCurrentItem();
        if (c < adapter.getCount() - 1) {
            vPager.setCurrentItem(c + 1);
        }
    }

    private void doPrev() {
        int c = vPager.getCurrentItem();
        if (c > 0) {
            vPager.setCurrentItem(c - 1);
        }
    }

    private void doGoToday() {
        vPager.setCurrentItem(adapter.getBasePos(), true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.balance_mgnt_menu, menu);
        menu.findItem(R.id.menu_hierarchy).setChecked(preference().isHierarchicalBalance());

//        MenuItem menuItem = menu.findItem(R.id.menu_operations);
//        ActionMenuView amView = (ActionMenuView) menuItem.getActionView();

        //don't have a way to set align right of buttons
//        ActionMenuView.LayoutParams lp = new ActionMenuView.LayoutParams(0, ActionMenuView.LayoutParams.WRAP_CONTENT);
//        amView.setLayoutParams(lp);
//        amView.setGravity(Gravity.RIGHT);
//        amView.setBackgroundColor(Color.RED);

//        Menu menuObject = amView.getMenu();
//        inflater.inflate(R.menu.balance_mgnt_operations_menu, menuObject);
//
//        amView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == R.id.menu_prev) {
//                    doPrev();
//                } else if (item.getItemId() == R.id.menu_next) {
//                    doNext();
//                } else if (item.getItemId() == R.id.menu_go_today) {
//                    doGoToday();
//                } else if (item.getItemId() == R.id.menu_change_mode) {
//                    doChangeMode();
//                }
//                return true;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_yearly_from_beginning_aggregate_line_chart) {
            doYearlyFromBeginningAggregateLineChart();
            return true;
        } else if (item.getItemId() == R.id.menu_hierarchy) {
            item.setChecked(!item.isChecked());
            preference().setHierarchicalBalance(!preference().isHierarchicalBalance());
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    lookupQueue().publish(new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_RELOAD_FRAGMENT).build());
                }
            });
            return true;
        } else if (item.getItemId() == R.id.menu_prev) {
            doPrev();
        } else if (item.getItemId() == R.id.menu_next) {
            doNext();
        } else if (item.getItemId() == R.id.menu_go_today) {
            doGoToday();
        } else if (item.getItemId() == R.id.menu_change_mode) {
            doChangeMode();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.BalanceMgntFrag.ON_SELECT_BALANCE:
                doSelectBalance((Balance) event.getData());
                break;
            case QEvents.BalanceMgntFrag.ON_RESELECT_BALANCE:
                doRecordList((Balance) event.getData());
                break;
            case QEvents.BalanceMgntFrag.ON_FRAGMENT_START:
                BalanceMgntFragment.FragInfo info = (BalanceMgntFragment.FragInfo) event.getData();
                fragInfoMap.put(info.pos, info);
                break;
            case QEvents.BalanceMgntFrag.ON_FRAGMENT_STOP:
                fragInfoMap.remove(event.getData());
                break;
        }
    }


    private void doRecordList(Balance balance) {
        if (balance.getTarget() == null) {//just in case?
            Logger.w("balance_mgnt target is null");
            return;
        }

        BalanceMgntFragment.FragInfo fragInfo = fragInfoMap.get(vPager.getCurrentItem());
        if (fragInfo == null) {
            Logger.w("fragInfo is null on {}", vPager.getCurrentItem());
            return;
        }


        Intent intent = null;
        intent = new Intent(this, AccountRecordListActivity.class);
        if (fragInfo.startDate != null) {
            intent.putExtra(AccountRecordListActivity.ARG_START, fragInfo.startDate);
        }
        if (fragInfo.endDate != null) {
            intent.putExtra(AccountRecordListActivity.ARG_END, fragInfo.endDate);
        }
        intent.putExtra(AccountRecordListActivity.ARG_CONDITION, balance.getTarget());
        intent.putExtra(AccountRecordListActivity.ARG_CONDITION_INFO, balance.getName());
        this.startActivityForResult(intent, Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_ACCOUNT_RECORD_LIST_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    //user might add record, reload it.
                    lookupQueue().publish(new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_RELOAD_FRAGMENT).build());
                }
            });
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void doPieChart(final Balance balance) {
        final BalanceMgntFragment.FragInfo fragInfo = fragInfoMap.get(vPager.getCurrentItem());
        if (fragInfo == null) {
            Logger.w("fragInfo is null on {}", vPager.getCurrentItem());
            return;
        }

        Intent intent = new Intent(this, PieAccountActivity.class);
        if (balance.getTarget() instanceof Account) {
            Account account = (Account) balance.getTarget();
            intent.putExtra(PieAccountFragment.ARG_ACCOUNT, account);
        } else {
            AccountType at = AccountType.find(balance.getType());
            intent.putExtra(PieAccountFragment.ARG_ACCOUNT_TYPE, at);
        }
        intent.putExtra(PieAccountFragment.ARG_BASE_DATE, fragInfo.date);
        intent.putExtra(PieAccountFragment.ARG_PERIOD_MODE, mode == MODE_MONTH ? ChartBaseFragment.PeriodMode.MONTHLY : ChartBaseFragment.PeriodMode.YEARLY);
        intent.putExtra(PieAccountFragment.ARG_FROM_BEGINNING, fromBeginning);
        intent.putExtra(PieAccountActivity.ARG_TITLE, getTitle());
        startActivity(intent);

    }

    private void doYearlyLineChart(final Balance balance, boolean cumulative) {
        final BalanceMgntFragment.FragInfo fragInfo = fragInfoMap.get(vPager.getCurrentItem());
        if (fragInfo == null) {
            Logger.w("fragInfo is null on {}", vPager.getCurrentItem());
            return;
        }

        if (fromBeginning) {

            Intent intent = new Intent(this, LineFromBeginningAggregateActivity.class);

            AccountType at = AccountType.find(balance.getType());

            if (balance.getTarget() instanceof Account) {
                Account account = (Account) balance.getTarget();
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT, account);
            } else {
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT_TYPE, at);
            }
            intent.putExtra(LineFromBeginningAggregateFragment.ARG_BASE_DATE, fragInfo.date);
            intent.putExtra(LineFromBeginningAggregateFragment.ARG_PERIOD_MODE, mode == MODE_MONTH ? ChartBaseFragment.PeriodMode.MONTHLY : ChartBaseFragment.PeriodMode.YEARLY);
            intent.putExtra(LineAccountActivity.ARG_TITLE, i18n.string(R.string.act_balance_yearly_aggregate_line_chart, at.getDisplay(i18n)));
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LineAccountActivity.class);

            AccountType at = AccountType.find(balance.getType());

            if (balance.getTarget() instanceof Account) {
                Account account = (Account) balance.getTarget();
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT, account);
            } else {
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT_TYPE, at);
            }
            intent.putExtra(LineAccountFragment.ARG_BASE_DATE, fragInfo.date);
            intent.putExtra(LineAccountFragment.ARG_PERIOD_MODE, mode == MODE_MONTH ? ChartBaseFragment.PeriodMode.MONTHLY : ChartBaseFragment.PeriodMode.YEARLY);
            intent.putExtra(LineAccountFragment.ARG_CALCULATION_MODE, cumulative ? ChartBaseFragment.CalculationMode.CUMULATIVE : ChartBaseFragment.CalculationMode.INDIVIDUAL);
            intent.putExtra(LineAccountActivity.ARG_TITLE,
                    cumulative ? i18n.string(R.string.label_balance_yearly_linechart_cumulative, at.getDisplay(i18n), yearFormat.format(fragInfo.date)) :
                            i18n.string(R.string.label_balance_yearly_linechart, at.getDisplay(i18n), yearFormat.format(fragInfo.date)));
            startActivity(intent);
        }
    }


    private void doYearlyFromBeginningAggregateLineChart() {
        final BalanceMgntFragment.FragInfo fragInfo = fragInfoMap.get(vPager.getCurrentItem());
        if (fragInfo == null) {
            Logger.w("fragInfo is null on {}", vPager.getCurrentItem());
            return;
        }

        Intent intent = new Intent(this, LineFromBeginningAggregateActivity.class);

        intent.putExtra(LineFromBeginningAggregateFragment.ARG_BASE_DATE, fragInfo.date);
        intent.putExtra(LineFromBeginningAggregateFragment.ARG_PERIOD_MODE, mode == MODE_MONTH ? ChartBaseFragment.PeriodMode.MONTHLY : ChartBaseFragment.PeriodMode.YEARLY);
        intent.putExtra(LineAccountActivity.ARG_TITLE, i18n.string(R.string.act_balance_yearly_aggregate_line_chart));
        startActivity(intent);
    }

    public class BalancePagerAdapter extends FragmentPagerAdapter {

        int basePos;
        int maxPos;

        public BalancePagerAdapter(FragmentManager fm) {
            super(fm);
            calculatePos();
        }

        private void calculatePos() {
            CalendarHelper calHelper = calendarHelper();

            Calendar cal0 = calHelper.calendar(new Date(0));
            Calendar calbase = calHelper.calendar(baseDate);

            int diffYear = calbase.get(Calendar.YEAR) - cal0.get(Calendar.YEAR);

            if (mode == MODE_MONTH) {
                basePos = diffYear * 12 + calbase.get(Calendar.MONTH) - cal0.get(Calendar.MONTH);
            } else {
                basePos = diffYear;
            }
            basePos -= 1;//just for prvent hit boundary

            maxPos = basePos + (mode == MODE_MONTH ? Constants.MONTH_LOOK_AFTER : Constants.YEAR_LOOK_AFTER);
        }

        public int getBasePos() {
            return basePos;
        }

        @Override
        public int getCount() {
            return maxPos;
        }

        @Override
        public Fragment getItem(int position) {
            CalendarHelper calHelper = calendarHelper();
            Date targetDate;

            int diff = position - basePos;
            if (mode == MODE_MONTH) {
                targetDate = calHelper.monthAfter(baseDate, diff);
            } else {
                targetDate = calHelper.yearAfter(baseDate, diff);
            }

            BalanceMgntFragment f = new BalanceMgntFragment();
            Bundle b = new Bundle();
            b.putInt(BalanceMgntFragment.ARG_POS, position);
            b.putInt(BalanceMgntFragment.ARG_MODE, mode);
            b.putBoolean(BalanceMgntFragment.ARG_FROM_BEGINNING, fromBeginning);
            b.putSerializable(BalanceMgntFragment.ARG_TARGET_DATE, targetDate);
            f.setArguments(b);
            return f;
        }

    }


    private class BalanceActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.balance_mgnt_item_menu, menu);//Inflate the menu over action mode
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

            mi = menu.findItem(R.id.menu_yearly_linechart);
            mi.setVisible(!fromBeginning);

            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_piechart) {
                doPieChart(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_yearly_linechart) {
                doYearlyLineChart(actionObj, false);
                return true;
            } else if (item.getItemId() == R.id.menu_yearly_linechart_cumulative) {
                doYearlyLineChart(actionObj, true);
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
            lookupQueue().publish(new EventQueue.EventBuilder(QEvents.BalanceMgntFrag.ON_CLEAR_SELECTION).build());
        }
    }
}
