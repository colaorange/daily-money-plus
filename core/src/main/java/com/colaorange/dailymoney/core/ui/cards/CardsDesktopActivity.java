package com.colaorange.dailymoney.core.ui.cards;

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
import android.text.InputType;
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
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.data.DefaultCardsCreator;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.LocalWebViewActivity;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.StartupActivity;
import com.colaorange.dailymoney.core.ui.legacy.DesktopItem;
import com.colaorange.dailymoney.core.ui.legacy.DesktopMgntFragment;
import com.colaorange.dailymoney.core.ui.legacy.RecordEditorActivity;
import com.colaorange.dailymoney.core.ui.legacy.TestsDesktop;
import com.colaorange.dailymoney.core.ui.nav.NavMenuAdapter;
import com.colaorange.dailymoney.core.ui.nav.NavMenuHelper;
import com.colaorange.dailymoney.core.util.Dialogs;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dennis
 */
@InstanceState
public class CardsDesktopActivity extends ContextsActivity implements EventQueue.EventListener {


    private TabLayout vAppTabs;
    private ViewPager vPager;
    private DrawerLayout vDrawer;
    private TextView vDrawerTitle;
    private ListView vNavMenuList;
    private NavMenuAdapter navMenuAdapter;
    private List<NavMenuAdapter.NavMenuObj> navMenuList;

//    private MenuItem mModeEdit;

    private List<CardCollection> cardsList;

    @InstanceState
    private Integer currentCardsIndex = null;

    @InstanceState
    private Boolean firstTime;

    private static AtomicBoolean globalHandleFirstTime = new AtomicBoolean(false);
//    private static AtomicBoolean editMode = new AtomicBoolean(false);

    private ActionMode actionMode;

    private CardFacade cardFacade;

    public CardsDesktopActivity() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cards_drawer);

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
        cardFacade = new CardFacade(this);
        vAppTabs = findViewById(R.id.appTabs);
        vPager = findViewById(R.id.viewpager);
        //don't preload other page, only load current
        //damn, it can't allow 0, it has to >= 1
//        vPager.setOffscreenPageLimit(0);

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

    protected void stopModeEdit() {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
            //finish will publish it
//            publishReloadFragment(null);
        }
    }

    protected void startModeEdit() {

        if (actionMode == null) {
            actionMode = this.startSupportActionMode(new EditDesktopActionModeCallback());
        } else {
            actionMode.invalidate();
        }
        actionMode.setTitle(i18n().string(R.string.act_arrange_desktop));
        publishReloadFragment(null);
    }

    private void publishReloadFragment(Integer pos) {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.CardsFrag.ON_RELOAD_FRAGMENT)
                .withData(pos)
                .withArg(QEvents.CardsFrag.ARG_MODE_EDIT, actionMode != null)
                .build());
    }

    private void publishClearFragment(Integer pos) {
        lookupQueue().publish(QEvents.CardsFrag.ON_CLEAR_FRAGMENT, pos);
    }

    @Override
    public void onBackPressed() {
        if (vDrawer.isDrawerOpen(GravityCompat.START)) {
            vDrawer.closeDrawer(GravityCompat.START);
            return;
        } else if (actionMode != null) {
            stopModeEdit();
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
        int i = 0;
        for (CardCollection cards : cardsList) {

            View tab = vAppTabs.getTabAt(i).getCustomView();
            if (tab == null) {
                tab = (View) inflater.inflate(R.layout.regular_tab, null);
                vAppTabs.getTabAt(i).setCustomView(tab);
            }
            TextView vtext = tab.findViewById(R.id.tab_text);
            String title = cards.getTitle();
            title = Strings.isBlank(title) ? "" + (i + 1) : title;

            vtext.setText(title);
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

    private void reloadData() {

        List<CardCollection> temp = new LinkedList<>();
        Preference preference = preference();
        int s = preference.getCardsSize();
        for (int i = 0; i < s; i++) {
            if (!preference.isCardsEnabled(i)) {
                continue;
            }
            CardCollection cards = preference.getCards(i);
            temp.add(cards);
        }

        //append if test desktop
        if (preference.isTestsDesktop()) {
            CardCollection cards = new CardCollection();
            cards.withArg(TestsDesktop.NAME, true);
            cards.setTitle(i18n().string(R.string.desktop_tests));
            temp.add(cards);

        }

        if (temp.equals(cardsList)) {
            publishReloadFragment(null);
        } else {
            /*
            I don't know the reason yet, so just clear all when reloading
              Caused by: java.lang.IllegalArgumentException: No view found for id 0x1 (unknown) for fragment CardNavPagesFragment{2d9894f #40 id=0x1 cardsList:0:0}
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1422)
        at android.support.v4.app.FragmentManagerImpl.moveFragmentToExpectedState(FragmentManager.java:1759)
        at android.support.v4.app.FragmentManagerImpl.moveToState(FragmentManager.java:1827)
             */
            publishClearFragment(null);

            cardsList = temp;

            if (currentCardsIndex == null) {
                currentCardsIndex = 0;
            }

            if (currentCardsIndex >= cardsList.size()) {
                currentCardsIndex = cardsList.size() - 1;
            }

            //setupWithViewPager will cause current index reset, need to keep it.
            int tp = currentCardsIndex;

            vPager.setAdapter(new CardsPagerAdapter(getSupportFragmentManager(), cardsList));
            vAppTabs.setupWithViewPager(vPager);
            refreshTab();

            currentCardsIndex = tp;
        }

        if (currentCardsIndex > 0) {
            vPager.setCurrentItem(currentCardsIndex, false);
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
        getMenuInflater().inflate(R.menu.cards_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            doNewRecord();
            return true;
        } else if (item.getItemId() == R.id.menu_mode_edit) {
            if (item.isChecked()) {
                stopModeEdit();
            } else {
                startModeEdit();
            }
            return true;
        } else if (item.getItemId() == R.id.menu_edit_title) {
            doEditTitle();
        } else if (item.getItemId() == R.id.menu_add_card) {
            doAddCard();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isTestDesktop(CardCollection cards) {
        if (cards.getArg(TestsDesktop.NAME, Boolean.FALSE)) {
            return true;
        }
        return false;
    }

    private void doEditTitle() {
        I18N i18n = i18n();

        CardCollection cards = cardsList.get(vPager.getCurrentItem());
        if (isTestDesktop(cards)) {
            GUIs.shortToast(this, i18n.string(R.string.msg_not_available_for, cards.getTitle()));
            return;
        }


        Dialogs.showTextEditor(this, i18n.string(R.string.act_edit_title),
                i18n.string(R.string.msg_edit_desktop_title),
                InputType.TYPE_CLASS_TEXT, cards.getTitle(), new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (which == Dialogs.OK_BUTTON) {
                            int cardsPos = vPager.getCurrentItem();
                            CardCollection cards = preference().getCards(cardsPos);
                            cards.setTitle((String) data);
                            preference().updateCards(cardsPos, cards);

                            cardsList.set(cardsPos, cards);
                            refreshTab();
                        }
                        return true;
                    }
                });
    }

    private void doAddCard() {
        final I18N i18n = i18n();

        CardCollection cards = cardsList.get(vPager.getCurrentItem());
        if (isTestDesktop(cards)) {
            GUIs.shortToast(this, i18n.string(R.string.msg_not_available_for, cards.getTitle()));
            return;
        }


        List<CardType> values = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        Set<CardType> selection = new LinkedHashSet<>();

        for (CardType type : cardFacade.listAvailableType()) {
            values.add(type);
        }
        for (CardType type : values) {
            labels.add(cardFacade.getTypeText(type));
        }

        Dialogs.showSelectionList(this, i18n.string(R.string.act_add_card),
                i18n.string(R.string.msg_select_cards), (List) values, labels, true,
                (Set) selection, new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (Dialogs.OK_BUTTON == which) {
                            Preference preference = Contexts.instance().getPreference();

                            Set<CardType> selection = (Set<CardType>) data;

                            if (selection.isEmpty()) {
                                GUIs.shortToast(CardsDesktopActivity.this, i18n.string(R.string.msg_field_empty_selection, R.string.label_card_type));
                            } else {
                                int cardsPos = vPager.getCurrentItem();
                                CardCollection cards = preference().getCards(cardsPos);

                                for (CardType type : selection) {
                                    Card card = new Card(type, cardFacade.getTypeText(type));
                                    cards.add(card);
                                    Logger.d(">>> new card {} has added to cards {}/{}", card.getTitle(), cards.getTitle(), cards.size());
                                }

                                preference.updateCards(cardsPos, cards);
                                publishReloadFragment(cardsPos);
                            }


                        }
                        return true;
                    }
                });
    }


    private void doNewRecord() {
        Intent intent = null;
        intent = new Intent(this, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        startActivity(intent);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.DesktopMgntFrag.ON_SELECT_DESKTOP_TEIM:
            case QEvents.DesktopMgntFrag.ON_RESELECT_DESKTOP_TEIM:
                //test desktop
                ((DesktopItem) event.getData()).run();
                break;
        }
    }


    public static class CardsPagerAdapter extends FragmentPagerAdapter {
        List<CardCollection> cardsList;

        public CardsPagerAdapter(FragmentManager fm, List<CardCollection> cardsList) {
            super(fm);
            this.cardsList = cardsList;
        }

        @Override
        public int getCount() {
            return cardsList.size();
        }

        @Override
        public Fragment getItem(int position) {

            CardCollection cards = this.cardsList.get(position);
            if (cards.getArg(TestsDesktop.NAME, Boolean.FALSE)) {
                DesktopMgntFragment f = new DesktopMgntFragment();
                Bundle b = new Bundle();
                b.putString(DesktopMgntFragment.ARG_DESKTOP_NAME, TestsDesktop.NAME);
                f.setArguments(b);
                return f;
            } else {
                Fragment f = new CardsFragment();
                Bundle b = new Bundle();
                b.putInt(CardsFragment.ARG_CARDS_POS, position);
                f.setArguments(b);
                return f;
            }
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
                    Intent intent = new Intent(CardsDesktopActivity.this, LocalWebViewActivity.class);
                    intent.putExtra(LocalWebViewActivity.ARG_URI_RES_ID, R.string.path_about);
                    intent.putExtra(LocalWebViewActivity.ARG_TITLE, i18n().string(R.string.app_name));
                    startActivity(intent);
                }
            });
            return true;
        } else if (fvt) {

            if (!Contexts.instance().getPreference().isAnyCards()) {
                new DefaultCardsCreator().createForUpgrade(false);
            }


            GUIs.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(CardsDesktopActivity.this, LocalWebViewActivity.class);
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

    private class EditDesktopActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.cards_edit_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_edit_title) {
                doEditTitle();
                return true;
            } else if (item.getItemId() == R.id.menu_add_card) {
                doAddCard();
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
            publishReloadFragment(null);
        }
    }

}