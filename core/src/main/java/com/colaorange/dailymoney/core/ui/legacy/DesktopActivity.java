package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Intent;
import android.os.Bundle;
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
public class DesktopActivity extends ContextsActivity implements OnTabChangeListener, OnItemClickListener {

    private boolean firstTime;

    private String currTab = null;

    private GridView gridView;

    private DesktopItemAdapter gridViewAdapter;

    private List<Desktop> desktops = new ArrayList<Desktop>();

    private DesktopItem lastClickedItem;

    private TextView infoWeeklyExpense;
    private TextView infoMonthlyExpense;
    private TextView infoCumulativeCash;
    private TabHost tabs;

    private HashMap<Object, DesktopItem> dtHashMap = new HashMap<Object, DesktopItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop);

        initParams();
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

    private void initParams() {
        Bundle bundle = getIntentExtras();
        firstTime = bundle.getBoolean(StartupActivity.PARAM_FIRST_TIME,false);
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

        tabs.setup();


        for (Desktop d : desktops) {
            TabSpec tab = tabs.newTabSpec(d.getLabel());
            if (d.getIcon() < 0) {
                tab.setIndicator(d.getLabel());
            } else {
                tab.setIndicator(d.getLabel(), getResources().getDrawable(d.getIcon()));
            }
            tab.setContent(R.id.desktop_grid);
            tabs.addTab(tab);
            if (currTab == null) {
                currTab = tab.getTag();
            }
        }

        if (desktops.size() > 1) {
            //workaround, force refresh
            tabs.setCurrentTab(1);
            tabs.setCurrentTab(0);
        }

        tabs.setOnTabChangedListener(this);

    }


    private void initMembers() {
        tabs = findViewById(R.id.desktop_tabs);

        infoWeeklyExpense = findViewById(R.id.desktop_weekly_expense);
        infoMonthlyExpense = findViewById(R.id.desktop_monthly_expense);
        infoCumulativeCash = findViewById(R.id.desktop_cumulative_cash);


        gridViewAdapter = new DesktopItemAdapter();
        gridView = findViewById(R.id.desktop_grid);
        gridView.setAdapter(gridViewAdapter);
        gridView.setOnItemClickListener(this);

    }

    private void refreshDesktop() {
        for (Desktop d : desktops) {
            if (d.getLabel().equals(currTab)) {
                d.refresh();
                break;
            }
        }
        gridViewAdapter.notifyDataSetChanged();
    }

    private boolean handleFirstTime() {
        boolean fvt = contexts().getAndSetFirstVersionTime();
        if (firstTime){
            firstTime = false;
            GUIs.post(new Runnable(){
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.PARAM_URI_RES_ID, R.string.path_about);
                    intent.putExtra(LocalWebViewActivity.PARAM_TITLE, i18n().string(R.string.app_name));
                    startActivity(intent);
                }
            });
            return true;
        }else if(fvt){
            GUIs.post(new Runnable(){
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
        infoWeeklyExpense.setText(i18n.string(R.string.label_weekly_expense, contexts().toFormattedMoneyString(b)));

        start = calHelper.monthStartDate(now);
        end = calHelper.monthEndDate(now);
        b = BalanceHelper.calculateBalance(type, start, end).getMoney();
        infoMonthlyExpense.setText(i18n.string(R.string.label_monthly_expense, contexts().toFormattedMoneyString(b)));


        IDataProvider idp = Contexts.instance().getDataProvider();
        List<Account> acl = idp.listAccount(AccountType.ASSET);
        b = 0;
        for (Account ac : acl) {
            if (ac.isCashAccount()) {
                b += BalanceHelper.calculateBalance(ac, null, calHelper.toDayEnd(now)).getMoney();
            }
        }
        infoCumulativeCash.setText(i18n.string(R.string.label_cumulative_cash, contexts().toFormattedMoneyString(b)));
    }

    @Override
    public void onTabChanged(String tabId) {
        currTab = tabId;
        refreshDesktop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        List<DesktopItem> importants = new ArrayList<DesktopItem>();
        for (Desktop d : desktops) {
            for (DesktopItem item : d.getItems()) {
                if (item.getImportance() >= 0) {
                    importants.add(item);
                }
            }
        }
        //sort
        Collections.sort(importants, new Comparator<DesktopItem>() {
            public int compare(DesktopItem item1, DesktopItem item2) {
                return Integer.valueOf(item2.getImportance()).compareTo(Integer.valueOf(item1.getImportance()));
            }
        });
        for (DesktopItem item : importants) {
            MenuItem mi = menu.add(item.getLabel());
            mi.setOnMenuItemClickListener(new DesktopItemClickListener(item));
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        //item clicked in grid view
        if (parent == gridView) {
            DesktopItem di = dtHashMap.get(view);
            if (di != null) {
                lastClickedItem = di;
                lastClickedItem.run();
            }
        }
    }


    @SuppressWarnings("unchecked")
    List<DesktopItem> getCurrentVisibleDesktopItems() {
        for (Desktop d : desktops) {
            if (d.getLabel().equals(currTab)) {
                return d.getVisibleItems();
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

        public int getCount() {
            return getCurrentVisibleDesktopItems().size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            TextView tv;
            LinearLayout view;
            if (convertView == null) {  // if it's not recycled, initialize some attributes

                view = new LinearLayout(DesktopActivity.this);
                view.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
                GUIs.inflateView(DesktopActivity.this, view, R.layout.desktop_item);
            } else {
                view = (LinearLayout) convertView;
            }

            iv = view.findViewById(R.id.desktop_icon);
            tv = view.findViewById(R.id.desktop_label);

            DesktopItem item = getCurrentVisibleDesktopItems().get(position);
            iv.setImageResource(item.getIcon());
            tv.setText(item.getLabel());
            dtHashMap.put(view, item);
            return view;
        }

    }


}
