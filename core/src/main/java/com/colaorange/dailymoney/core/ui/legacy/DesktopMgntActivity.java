package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.StartupActivity;
import com.colaorange.dailymoney.core.ui.helper.NavMenuAdapter;
import com.colaorange.dailymoney.core.ui.helper.NavMenuHelper;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dennis
 */
public class DesktopMgntActivity extends ContextsActivity implements EventQueue.EventListener {


    private TabLayout vAppTabs;
    private View vInfoPannel;
    private ViewPager vPager;
    private DrawerLayout vDrawer;
    private TextView vDrawerTitle;
    private ListView vNavMenuList;
    private NavMenuAdapter navMenuAdapter;
    private List<NavMenuAdapter.NavMenuObj> navMenuList;


    private ActionMode actionMode;
    private DesktopItem actionObj;

    @InstanceState
    private String currentDesktopName = null;

    private List<Desktop> desktops;


    Map<String, Desktop> supportedDesktops;

    private TextView vInfoWeeklyExpense;
    private TextView vInfoMonthlyExpense;
    private TextView vInfoCumulativeCash;

    @InstanceState
    private Boolean firstTime;

    private boolean infoPanelFix = false;

    private static AtomicBoolean globalHandleFisrtTime = new AtomicBoolean(false);


    public DesktopMgntActivity() {
    }


    public Map<String, Desktop> getSupportedDesktops() {
        return java.util.Collections.unmodifiableMap(supportedDesktops);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop_drawer);

        initArgs();
        initMembers();
        initMemberDrawer();

        handleFirstTime();

        //a hard fix for cutted vParger with largeTextSize infoPanel.
        ViewTreeObserver vto = vInfoPannel.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        if (infoPanelFix) {
                            infoPanelFix = false;
                            int height = vInfoPannel.getHeight();
                            vPager.setPadding(vPager.getPaddingLeft(), vPager.getPaddingTop(), vPager.getPaddingRight(), height);
                        }
                    } catch (Exception x) {
                        infoPanelFix = false;
                        //just in case
                        Logger.w(x.getMessage(), x);
                    }

                }
            });
        }

        supportedDesktops = new LinkedHashMap<>();
        for (Desktop dt : new Desktop[]{new MainDesktop(this), new ReportsDesktop(this), new TestsDesktop(this)}) {
            supportedDesktops.put(dt.getName(), dt);
        }
    }

    private void initArgs() {
        if (firstTime == null) {
            Bundle bundle = getIntentExtras();
            firstTime = bundle.getBoolean(StartupActivity.ARG_FIRST_TIME, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadData();
    }

    private void initMembers() {
        vInfoWeeklyExpense = findViewById(R.id.desktop_weekly_expense);
        vInfoMonthlyExpense = findViewById(R.id.desktop_monthly_expense);
        vInfoCumulativeCash = findViewById(R.id.desktop_cumulative_cash);

        vAppTabs = findViewById(R.id.appTabs);
        vInfoPannel = findViewById(R.id.info_panel);
        vPager = findViewById(R.id.viewpager);

        vAppTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentDesktopName = desktops.get(tab.getPosition()).getLabel();
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.DesktopMgntFrag.ON_CLEAR_SELECTION).build());
                refreshTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //is it possible?
                currentDesktopName = null;
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.DesktopMgntFrag.ON_CLEAR_SELECTION).build());

                //don't refresh it, there must be a selected.
//                refreshTab();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentDesktopName = desktops.get(tab.getPosition()).getLabel();
            }
        });
    }

    protected void initMemberDrawer() {

        vDrawer = findViewById(R.id.app_drawer);
        vNavMenuList = findViewById(R.id.app_drawer_menu_list);

        NavigationView vnav = (NavigationView) findViewById(R.id.app_drawer_nav);

        //weird, wtf, it is in a unknow scope
        vDrawerTitle = vnav.getHeaderView(0).findViewById(R.id.app_drawer_title);

        //parpare data
        navMenuList = new LinkedList<>();

        new NavMenuHelper(this).reload(navMenuList);


        navMenuAdapter = new NavMenuAdapter(this, navMenuList);
        vNavMenuList.setAdapter(navMenuAdapter);

        vNavMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                vDrawer.closeDrawer(GravityCompat.START);
                //let drawer close first
                GUIs.delayPost(new Runnable() {
                    @Override
                    public void run() {
                        NavMenuAdapter.NavMenuObj obj = (NavMenuAdapter.NavMenuObj) navMenuAdapter.getItem(position);
                        if (obj instanceof NavMenuAdapter.NavMenuItem && ((NavMenuAdapter.NavMenuItem) obj).getListener() != null) {
                            ((NavMenuAdapter.NavMenuItem) obj).getListener().onClick(view);
                        }
                    }
                }, 100);

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (vDrawer.isDrawerOpen(GravityCompat.START)) {
            vDrawer.closeDrawer(GravityCompat.START);
            return;
        } else if (vPager.getCurrentItem() > 0) {
            vPager.setCurrentItem(0);
            return;
        }
        super.onBackPressed();
    }


    private void refreshTab() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int i = 0;
        for (Desktop a : desktops) {
            View tab = vAppTabs.getTabAt(i).getCustomView();
            if (tab == null) {
                tab = (View) inflater.inflate(R.layout.regular_tab, null);
                vAppTabs.getTabAt(i).setCustomView(tab);
            }
            TextView vtext = tab.findViewById(R.id.tab_text);
            //follow original tab design
            vtext.setText(a.getLabel());
            i++;
        }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });

        }
    }

    private void reloadData() {

        List<Desktop> temp = new LinkedList<>();
        for (Desktop dt : supportedDesktops.values()) {
            if (dt.isAvailable()) {
                temp.add(dt);
            }
        }

        if (temp.equals(desktops)) {
            lookupQueue().publish(QEvents.DesktopMgntFrag.ON_RELOAD_FRAGMENT, null);
        } else {
            desktops = temp;

            int selpos = 0;
            int i = 0;
            for (Desktop d : desktops) {
                if (d.getLabel().equals(currentDesktopName)) {
                    selpos = i;
                    break;
                }
                i++;
            }
            currentDesktopName = desktops.get(selpos).getLabel();

            vPager.setAdapter(new DesktopTypePagerAdapter(getSupportFragmentManager(), desktops));

            vAppTabs.setupWithViewPager(vPager);
            vAppTabs.getTabAt(selpos).select();

            refreshTab();
        }

        CalendarHelper calHelper = calendarHelper();
        I18N i18n = i18n();

        IMasterDataProvider imdp = Contexts.instance().getMasterDataProvider();
        Book book = imdp.findBook(Contexts.instance().getWorkingBookId());
        String symbol = book.getSymbol();
        String title;
        if (symbol == null || "".equals(symbol)) {
            title = book.getName();
        } else {
            title = book.getName() + " ( " + symbol + " )";
        }

        setTitle(title);
        vDrawerTitle.setText(title);

//        infoBook.setVisibility(imdp.listAllBook().size()<=1?TextView.GONE:TextView.VISIBLE);

        Date now = new Date();
        Date start = calHelper.weekStartDate(now);
        Date end = calHelper.weekEndDate(now);
        AccountType type = AccountType.EXPENSE;
        double b = BalanceHelper.calculateBalance(type, start, end).getMoney();
        vInfoWeeklyExpense.setText(i18n.string(R.string.label_weekly_expense, contexts().toFormattedMoneyString(b)));

        start = calHelper.monthStartDate(now);
        end = calHelper.monthEndDate(now);
        b = BalanceHelper.calculateBalance(type, start, end).getMoney();
        vInfoMonthlyExpense.setText(i18n.string(R.string.label_monthly_expense, contexts().toFormattedMoneyString(b)));


        IDataProvider idp = Contexts.instance().getDataProvider();
        List<Account> acl = idp.listAccount(AccountType.ASSET);
        b = 0;
        for (Account ac : acl) {
            if (ac.isCashAccount()) {
                b += BalanceHelper.calculateBalance(ac, null, calHelper.toDayEnd(now)).getMoney();
            }
        }
        vInfoCumulativeCash.setText(i18n.string(R.string.label_cumulative_cash, contexts().toFormattedMoneyString(b)));

        infoPanelFix = true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.desktop_mgnt_menu, menu);

        List<DesktopItem> menuItems = new ArrayList<>();
        for (Desktop d : desktops) {
            for (DesktopItem item : d.getMenuItems()) {
                menuItems.add(item);
            }
        }

        Collections.sort(menuItems, new Comparator<DesktopItem>() {
            public int compare(DesktopItem item1, DesktopItem item2) {
                return Integer.valueOf(item2.getPriority()).compareTo(Integer.valueOf(item1.getPriority()));
            }
        });

        for (DesktopItem item : menuItems) {
            MenuItem mi = menu.add(item.getLabel());
            mi.setOnMenuItemClickListener(new DesktopItemClickListener(item));
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            doNewRecord();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doNewRecord() {
        Intent intent = null;
        intent = new Intent(this, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.DesktopMgntFrag.ON_SELECT_DESKTOP_TEIM:
                doSelectDesktopItem((DesktopItem) event.getData());
                break;
            case QEvents.DesktopMgntFrag.ON_RESELECT_DESKTOP_TEIM:
                doRunDesktopItem((DesktopItem) event.getData());
                break;
        }
    }


    private void doSelectDesktopItem(DesktopItem item) {
        if (item == null && actionMode != null) {
            actionMode.finish();
            return;
        }

        if (item != null) {
            actionObj = item;
            if (actionMode == null) {
                actionMode = this.startSupportActionMode(new DesktopActionModeCallback());
            } else {
                actionMode.invalidate();
            }
            actionMode.setTitle(item.getLabel());

            actionObj.run();
        }

    }

    private void doRunDesktopItem(DesktopItem item) {
        actionObj.run();
    }


    public static class DesktopTypePagerAdapter extends FragmentPagerAdapter {
        List<Desktop> desktops;

        public DesktopTypePagerAdapter(FragmentManager fm, List<Desktop> desktops) {
            super(fm);
            this.desktops = desktops;
        }

        @Override
        public int getCount() {
            return desktops.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new DesktopMgntFragment();
            Bundle b = new Bundle();
            b.putString(DesktopMgntFragment.ARG_DESKTOP_NAME, desktops.get(position).getName());
            f.setArguments(b);
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return desktops.get(position).getLabel();
        }
    }


    private class DesktopActionModeCallback implements ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.desktop_mgnt_item_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels

            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_run) {
                actionObj.run();
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
            lookupQueue().publish(new EventQueue.EventBuilder(QEvents.DesktopMgntFrag.ON_CLEAR_SELECTION).build());
        }
    }

    private boolean handleFirstTime() {

        //#24 always popped up about page
        //after recreate the firstime state is still lost, use a extra flag to control this case
        if (!globalHandleFisrtTime.compareAndSet(false, true)) {
            return false;
        }

        boolean fvt = contexts().getAndSetFirstVersionTime();
        if (firstTime) {
            firstTime = false;
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopMgntActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_about);
                    intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n().string(R.string.app_name));
                    startActivity(intent);
                }
            });
            return true;
        } else if (fvt) {
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopMgntActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_what_is_new);
                    intent.putExtra(LocalWebViewActivity.ARG_TITLE, Contexts.instance().getAppVerName());
                    startActivity(intent);
                }
            });
            return true;
        }
        return false;
    }

    public class DesktopItemClickListener implements MenuItem.OnMenuItemClickListener {

        DesktopItem dtitem;

        public DesktopItemClickListener(DesktopItem dtitem) {
            this.dtitem = dtitem;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            dtitem.run();
            return true;
        }

    }

    @Override
    public int getActionBarHomeAsUp() {
        return homeAsUpAppId;
    }

    @Override
    public void onActionBarHomeAsUp(int resId) {
        if (resId == homeAsUpAppId) {
            vDrawer.openDrawer(GravityCompat.START);
            return;
        }
        super.onActionBarHomeAsUp(resId);
    }


}