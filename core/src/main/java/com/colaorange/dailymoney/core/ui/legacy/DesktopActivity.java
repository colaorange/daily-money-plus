package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.StartupActivity;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author dennis
 */
@InstanceState
public class DesktopActivity extends ContextsActivity implements OnTabChangeListener, OnItemClickListener {

    @InstanceState
    private Boolean firstTime;

    private String selectedTab = null;

    private GridView vGrid;

    private DesktopItemAdapter gridAdapter;

    private List<Desktop> desktops = new ArrayList<Desktop>();

    private DesktopItem lastClickedItem;

    private TextView vInfoWeeklyExpense;
    private TextView vInfoMonthlyExpense;
    private TextView vInfoCumulativeCash;
    private TabHost vTabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop);

        initArgs();
        initMembers();
        initDesktopItems();
        initTabs();

        refreshDesktop();

        handleFirstTime();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();

    }

    private void initArgs() {
        if (firstTime == null) {
            Bundle bundle = getIntentExtras();
            firstTime = bundle.getBoolean(StartupActivity.PARAM_FIRST_TIME, false);
        }
    }


    private void initDesktopItems() {

        Desktop[] dts = new Desktop[]{new MainDesktop(this), new ReportsDesktop(this), new TestsDesktop(this)};

        for (Desktop dt : dts) {
            if (dt.isAvailable()) {
                desktops.add(dt);
            }
        }
    }

    private void initTabs() {

        vTabs.setup();


        for (Desktop d : desktops) {
            TabSpec tab = vTabs.newTabSpec(d.getLabel());
            if (d.getIcon() < 0) {
                tab.setIndicator(d.getLabel());
            } else {
                tab.setIndicator(d.getLabel(), getResources().getDrawable(d.getIcon()));
            }
            tab.setContent(R.id.desktop_grid);
            vTabs.addTab(tab);
            if (selectedTab == null) {
                selectedTab = tab.getTag();
            }
        }

        if (desktops.size() > 1) {
            //workaround, force refresh
            vTabs.setCurrentTab(1);
            vTabs.setCurrentTab(0);
        }

        vTabs.setOnTabChangedListener(this);

    }


    private void initMembers() {
        vTabs = findViewById(R.id.desktop_tabs);

        vInfoWeeklyExpense = findViewById(R.id.desktop_weekly_expense);
        vInfoMonthlyExpense = findViewById(R.id.desktop_monthly_expense);
        vInfoCumulativeCash = findViewById(R.id.desktop_cumulative_cash);


        gridAdapter = new DesktopItemAdapter();
        vGrid = findViewById(R.id.desktop_grid);
        vGrid.setAdapter(gridAdapter);
        vGrid.setOnItemClickListener(this);

        TypedValue textSize = this.resolveThemeAttr(R.attr.textSize);
        int width = (int)(TypedValue.complexToDimensionPixelSize(textSize.data, getResources().getDisplayMetrics()) * 5.5);

        vGrid.setColumnWidth(width);

    }

    private void refreshDesktop() {
        for (Desktop d : desktops) {
            if (d.getLabel().equals(selectedTab)) {
                d.refresh();
                break;
            }
        }
        gridAdapter.notifyDataSetChanged();
    }

    private boolean handleFirstTime() {
        boolean fvt = contexts().getAndSetFirstVersionTime();
        if (firstTime) {
            //TODO minor bug, firstTime to false is no usage when onStop be called, have to save to savedInstanceState
            firstTime = false;
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_about);
                    intent.putExtra(LocalWebViewActivity.PARAM_TITLE, i18n().string(R.string.app_name));
                    startActivity(intent);
                }
            });
            return true;
        } else if (fvt) {
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_what_is_new);
                    intent.putExtra(LocalWebViewActivity.PARAM_TITLE, Contexts.instance().getAppVerName());
                    startActivity(intent);
                }
            });
            return true;
        }
        return false;
    }

    private void refreshUI() {

        CalendarHelper calHelper = calendarHelper();
        I18N i18n = i18n();

        IMasterDataProvider imdp = Contexts.instance().getMasterDataProvider();
        Book book = imdp.findBook(Contexts.instance().getWorkingBookId());
        String symbol = book.getSymbol();
        if (symbol == null || "".equals(symbol)) {
            setTitle(i18n.string(R.string.title_book) + " : " + book.getName());
        } else {
            setTitle(i18n.string(R.string.title_book) + " : " + book.getName() + " ( " + symbol + " )");
        }

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
    }

    @Override
    public void onTabChanged(String tabId) {
        selectedTab = tabId;
        refreshDesktop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        List<DesktopItem> menuItems = new ArrayList<DesktopItem>();
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
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        //item clicked in grid view
        if (parent == vGrid) {
            Object obj = view.getTag();
            if (obj instanceof DesktopItem) {
                lastClickedItem = (DesktopItem) obj;
                lastClickedItem.run();
            }
        }
    }


    @SuppressWarnings("unchecked")
    List<DesktopItem> getCurrentDesktopItems() {
        for (Desktop d : desktops) {
            if (d.getLabel().equals(selectedTab)) {
                return d.getDesktopItems();
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        if (lastClickedItem != null) {
            lastClickedItem.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class DesktopItemClickListener implements OnMenuItemClickListener {

        DesktopItem dtitem;

        public DesktopItemClickListener(DesktopItem dtitem) {
            this.dtitem = dtitem;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            lastClickedItem = dtitem;
            lastClickedItem.run();
            return true;
        }

    }

    public class DesktopItemAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public DesktopItemAdapter() {
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return getCurrentDesktopItems().size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout;
            ImageView vicon;
            TextView vtext;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                convertView = inflater.inflate(R.layout.desktop_item, parent, false);
            } else {
                convertView = (LinearLayout) convertView;
            }

//            layout = convertView.findViewById(R.id.desktop_layout);
            vicon = convertView.findViewById(R.id.desktop_icon);
            vtext = convertView.findViewById(R.id.desktop_label);


//            layout.setLayoutParams(new LinearLayout.LayoutParams((int)(textSize.value * 6 * getDpRatio()), LinearLayout.LayoutParams.WRAP_CONTENT));

            DesktopItem item = getCurrentDesktopItems().get(position);
            vicon.setImageResource(item.getIcon());
            vtext.setText(item.getLabel());

            convertView.setTag(item);
            return convertView;
        }

    }


}
