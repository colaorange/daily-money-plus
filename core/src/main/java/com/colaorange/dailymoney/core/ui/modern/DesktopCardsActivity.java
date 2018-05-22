package com.colaorange.dailymoney.core.ui.modern;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.CardCollection;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.StartupActivity;
import com.colaorange.dailymoney.core.ui.cards.CardsFragment;
import com.colaorange.dailymoney.core.ui.cards.DefaultCardsCreator;
import com.colaorange.dailymoney.core.ui.legacy.DesktopMgntFragment;
import com.colaorange.dailymoney.core.ui.legacy.RecordEditorActivity;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter;
import com.colaorange.dailymoney.core.ui.nav.NavMenuHelper;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dennis
 */
public class DesktopCardsActivity extends ContextsActivity implements EventQueue.EventListener {


    private TabLayout vAppTabs;
    private ViewPager vPager;
    private DrawerLayout vDrawer;
    private TextView vDrawerTitle;
    private ListView vNavMenuList;
    private NavMenuAdapter navMenuAdapter;
    private List<NavMenuAdapter.NavMenuObj> navMenuList;

    private List<CardCollection> cardsList;

    @InstanceState
    private Integer currentCardsIndex = null;

    @InstanceState
    private Boolean firstTime;

    private static AtomicBoolean globalHandleFirstTime = new AtomicBoolean(false);


    public DesktopCardsActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desktop_mgnt_drawer);

        initArgs();
        initMembers();
        initMemberDrawer();

        handleFirstTime();
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
        vAppTabs = findViewById(R.id.appTabs);
        vPager = findViewById(R.id.viewpager);

        vAppTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCardsIndex = tab.getPosition();
                refreshTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //is it possible?
                currentCardsIndex = null;
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.DesktopMgntFrag.ON_CLEAR_SELECTION).build());

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentCardsIndex = tab.getPosition();
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
        Preference preference = preference();
        int s = preference.getCardsSize();
        for (int i = 0; i < s; i++) {
            CardCollection cards = preference.getCards(i);

            View tab = vAppTabs.getTabAt(i).getCustomView();
            if (tab == null) {
                tab = (View) inflater.inflate(R.layout.regular_tab, null);
                vAppTabs.getTabAt(i).setCustomView(tab);
            }
            TextView vtext = tab.findViewById(R.id.tab_text);
            //follow original tab design
            vtext.setText(Strings.isBlank(cards.getTitle()) ? "" + (i + 1) : cards.getTitle());
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

        List<CardCollection> temp = new LinkedList<>();
        Preference preference = preference();
        int s = preference.getCardsSize();
        for (int i = 0; i < s; i++) {
            CardCollection cards = preference.getCards(i);
            temp.add(cards);
        }

        if (temp.equals(cardsList)) {
            lookupQueue().publish(QEvents.DesktopMgntFrag.ON_RELOAD_FRAGMENT, null);
        } else {
            cardsList = temp;

            if (currentCardsIndex >= cardsList.size()) {
                currentCardsIndex = cardsList.size() - 1;
            }


            vPager.setAdapter(new CardsPagerAdapter(getSupportFragmentManager(), cardsList));

            vAppTabs.setupWithViewPager(vPager);
            if (currentCardsIndex >= 0) {
                vAppTabs.getTabAt(currentCardsIndex).select();
            }
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.desktop_cards_menu, menu);
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
        }
    }


    public static class CardsPagerAdapter extends FragmentPagerAdapter {
        List<CardCollection> cards;

        public CardsPagerAdapter(FragmentManager fm, List<CardCollection> cards) {
            super(fm);
            this.cards = cards;
        }

        @Override
        public int getCount() {
            return cards.size();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new DesktopMgntFragment();
            Bundle b = new Bundle();
            b.putInt(CardsFragment.ARG_CARDS_INDEX, position);
            f.setArguments(b);
            return f;
        }
    }


    private boolean handleFirstTime() {

        //#24 always popped up about page
        //after recreate the firstime state is still lost, use a extra flag to control this case
        if (!globalHandleFirstTime.compareAndSet(false, true)) {
            return false;
        }

        boolean fvt = contexts().getAndSetFirstVersionTime();
        if (firstTime) {
            firstTime = false;
            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopCardsActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_about);
                    intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n().string(R.string.app_name));
                    startActivity(intent);
                }
            });
            return true;
        } else if (fvt) {

            if (Contexts.instance().getPreference().getCards(0).size() == 0) {
                new DefaultCardsCreator(this).create();
            }


            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(DesktopCardsActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_what_is_new);
                    intent.putExtra(LocalWebViewActivity.ARG_TITLE, Contexts.instance().getAppVerName());
                    startActivity(intent);
                }
            });
            return true;
        }
        return false;
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