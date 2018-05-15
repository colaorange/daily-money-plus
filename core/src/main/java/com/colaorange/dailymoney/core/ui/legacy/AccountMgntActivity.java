package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.GUIs;

import java.util.Map;

/**
 * this activity manages the account (of record) with tab widgets of android,
 *
 * @author dennis
 * @see {@link AccountType}
 */
public class AccountMgntActivity extends ContextsActivity implements EventQueue.EventListener {


    private TabLayout vAppTabs;

    private ViewPager vPager;

    private ActionMode actionMode;
    private Account actionObj;

    @InstanceState
    private String currentAccountType = null;

    private AccountType[] supportedTypes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_mgnt);
        initMembers();
    }

    private void initMembers() {

        vAppTabs = findViewById(R.id.appTabs);
        vPager = findViewById(R.id.viewpager);

        supportedTypes = AccountType.getSupportedType();

        //just in case filtering
        int selpos = 0;
        int i = 0;
        for (AccountType a : supportedTypes) {
            if (a.getType().equals(currentAccountType)) {
                selpos = i;
                break;
            }
            i++;
        }
        currentAccountType = supportedTypes[selpos].getType();

        vPager.setAdapter(new AccountTypePagerAdapter(getSupportFragmentManager(), supportedTypes));

        vAppTabs.setupWithViewPager(vPager);
        vAppTabs.getTabAt(selpos).select();

        refreshTab(true);

        vAppTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentAccountType = supportedTypes[tab.getPosition()].getType();
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.AccountMgnt.ON_CLEAR_SELECTION).build());
                refreshTab(false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //is it possible?
                currentAccountType = null;
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.AccountMgnt.ON_CLEAR_SELECTION).build());

                //don't refresh it, there must be a selected.
//                refreshTab();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentAccountType = supportedTypes[tab.getPosition()].getType();
            }
        });
    }

    private void refreshTab(boolean init){
        Map<AccountType,Integer> textColorMap = getAccountTextColorMap();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int i=0;
        for(AccountType a: supportedTypes){
            int icon;
            boolean selected = a.getType().equals(currentAccountType);
            switch(a){
                case INCOME:
                    icon = R.drawable.tab_income;
                    break;
                case EXPENSE:
                    icon = R.drawable.tab_expense;
                    break;
                case ASSET:
                    icon = R.drawable.tab_asset;
                    break;
                case LIABILITY:
                    icon = R.drawable.tab_liability;
                    break;
                case OTHER:
                    icon = R.drawable.tab_other;
                    break;
                case UNKONW:
                default:
                    icon = R.drawable.tab_unknow;
                    break;
            }
            View tab;
            if(init) {
                tab = (View) inflater.inflate(R.layout.regular_tab, null);
                vAppTabs.getTabAt(i).setCustomView(tab);
            }else{
                tab = vAppTabs.getTabAt(i).getCustomView();
            }
            TextView vtext = tab.findViewById(R.id.tab_text);
            ImageView vicon = tab.findViewById(R.id.tab_icon);
            //follow original tab design
            vtext.setText(a.getDisplay(i18n()).toUpperCase());
            //ugly when set color
            if(selected) {
                vtext.setTextColor(textColorMap.get(a));
            }else{
                vtext.setTextColor(resolveThemeAttrResData(R.attr.appPrimaryTextColor));
            }
            vicon.setImageDrawable(buildNonSelectedIcon(icon, selected));
            i++;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        lookupQueue().subscribe(this);
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
        lookupQueue().publish(QEvents.AccountMgnt.ON_RELOAD_FRAGMENT, null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_mgnt_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            doNewAccount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.AccountMgnt.ON_SELECT_ACCOUNT:
                doSelectAccount((Account)event.getData());
                break;
            case QEvents.AccountMgnt.ON_RESELECT_ACCOUNT:
                Account account = event.getData();
                doEditAccount((Account)event.getData());
                break;
        }
    }

    private void doDeleteAccount(final Account account) {
        final String name = account.getName();

        GUIs.confirm(this, i18n().string(R.string.qmsg_delete_account, account.getName()), new GUIs.OnFinishListener() {
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    boolean r = contexts().getDataProvider().deleteAccount(account.getId());
                    if (r) {
                        if (account.equals(actionObj)) {
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }

                        GUIs.shortToast(AccountMgntActivity.this, i18n().string(R.string.msg_account_deleted, name));
                        reloadData();
                        trackEvent(TE.DELETE_ACCOUNT);
                    }
                }
                return true;
            }
        });
    }

    private void doSelectAccount(Account account) {
        if (account == null && actionMode != null) {
            actionMode.finish();
            return;
        }

        if (account != null) {
            actionObj = account;
            if (actionMode == null) {
                actionMode = this.startSupportActionMode(new AccountActionModeCallback());
            } else {
                actionMode.invalidate();
            }
            actionMode.setTitle(account.getName());
        }

    }


    private void doEditAccount(Account account) {
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, false);
        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, account);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doCopyAccount(Account account) {
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, true);
        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, account);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doNewAccount() {
        Account acc = new Account(currentAccountType, "", 0D);
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, true);
        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, acc);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }


    public static class AccountTypePagerAdapter extends FragmentPagerAdapter {
        AccountType[] types;

        public AccountTypePagerAdapter(FragmentManager fm, AccountType[] types) {
            super(fm);
            this.types = types;
        }

        @Override
        public int getCount() {
            return types.length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = new AccountMgntFragment();
            Bundle b = new Bundle();
            b.putString(AccountMgntFragment.ARG_ACCOUNT_TYPE, types[position].getType());
            f.setArguments(b);
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return types[position].getDisplay(Contexts.instance().getI18n());
        }
    }


    private class AccountActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.account_mgnt_item_menu, menu);//Inflate the menu over action mode
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
            if (item.getItemId() == R.id.menu_edit) {
                doEditAccount(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                doDeleteAccount(actionObj);
                mode.finish();//Finish action mode
                return true;
            } else if (item.getItemId() == R.id.menu_copy) {
                doCopyAccount(actionObj);
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
            lookupQueue().publish(new EventQueue.EventBuilder(QEvents.AccountMgnt.ON_CLEAR_SELECTION).build());
        }


    }
}