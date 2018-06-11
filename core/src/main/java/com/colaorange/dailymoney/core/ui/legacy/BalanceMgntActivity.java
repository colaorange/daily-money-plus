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
import com.colaorange.dailymoney.core.ui.chart.LineFromBeginningAccountActivity;
import com.colaorange.dailymoney.core.ui.chart.LineFromBeginningAccountFragment;
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
    private DateFormat yearMonthFormat;

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
        yearMonthFormat = pref.getYearMonthFormat();
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

        menu.findItem(R.id.menu_chart_from_beginning_account_aggregate_line).setVisible(fromBeginning);


        menu.findItem(R.id.menu_slide_hint).setVisible(!preference().checkEver(Constants.Hint.BALANCE_SLIDE, false));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_chart_from_beginning_account_aggregate_line) {
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
        } else if (item.getItemId() == R.id.menu_go_today) {
            doGoToday();
        } else if (item.getItemId() == R.id.menu_change_mode) {
            doChangeMode();
        } else if (item.getItemId() == R.id.menu_slide_hint) {
            preference().checkEver(Constants.Hint.BALANCE_SLIDE, true);
            GUIs.shortToast(this, i18n().string(R.string.msg_slide_hint));
            doPrev();
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    doNext();
                }
            }, 400);
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

        if (fromBeginning) {
            intent.putExtra(AccountRecordListActivity.ARG_MODE, AccountRecordListActivity.MODE_ALL);
        } else if (mode == MODE_MONTH) {
            intent.putExtra(AccountRecordListActivity.ARG_MODE, AccountRecordListActivity.MODE_MONTH);
        } else if (mode == MODE_YEAR) {
            intent.putExtra(AccountRecordListActivity.ARG_MODE, AccountRecordListActivity.MODE_YEAR);
        }


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

    private void doAccountLineChart(final Balance balance, boolean cumulative) {
        final BalanceMgntFragment.FragInfo fragInfo = fragInfoMap.get(vPager.getCurrentItem());
        if (fragInfo == null) {
            Logger.w("fragInfo is null on {}", vPager.getCurrentItem());
            return;
        }

        if (fromBeginning) {

            Intent intent = new Intent(this, LineFromBeginningAccountActivity.class);

            AccountType at = AccountType.find(balance.getType());

            if (balance.getTarget() instanceof Account) {
                Account account = (Account) balance.getTarget();
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT, account);
            } else {
                intent.putExtra(LineAccountFragment.ARG_ACCOUNT_TYPE, at);
            }
            intent.putExtra(LineFromBeginningAccountFragment.ARG_BASE_DATE, fragInfo.date);
//            intent.putExtra(LineFromBeginningAccountFragment.ARG_PERIOD_MODE, mode == MODE_MONTH ? ChartBaseFragment.PeriodMode.MONTHLY : ChartBaseFragment.PeriodMode.YEARLY);
            //always use yearly mode, monthly mode is useless. (going mess when having too many data)
            intent.putExtra(LineFromBeginningAccountFragment.ARG_PERIOD_MODE, ChartBaseFragment.PeriodMode.YEARLY);
            intent.putExtra(LineAccountActivity.ARG_TITLE, getTitle());
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
            intent.putExtra(LineAccountActivity.ARG_TITLE, getTitle());
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
        intent.putExtra(LineAccountActivity.ARG_TITLE, i18n.string(R.string.msg_chart_from_beginning_account_aggregate_line, mode == MODE_MONTH ? yearMonthFormat.format(fragInfo.date) :
                yearFormat.format(fragInfo.date)));
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

            mi = menu.findItem(R.id.menu_chart_account_line);
            mi.setVisible(!fromBeginning);

            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_chart_account_pie) {
                doPieChart(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_chart_account_line) {
                doAccountLineChart(actionObj, false);
                return true;
            } else if (item.getItemId() == R.id.menu_chart_account_cumulative_line) {
                doAccountLineChart(actionObj, true);
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
