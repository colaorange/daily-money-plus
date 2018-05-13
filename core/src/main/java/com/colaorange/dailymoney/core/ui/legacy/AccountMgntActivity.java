package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.InstanceState;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.GUIs;

/**
 * this activity manages the account (of record) with tab widgets of android,
 *
 * @author dennis
 * @see {@link AccountType}
 */
public class AccountMgntActivity extends ContextsActivity {


    private TabLayout vAppTabs;

    private ViewPager vPager;

    @InstanceState
    private String currentAccountType = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_mgnt);
        initMembers();
    }

    private void initMembers() {

        vAppTabs = findViewById(R.id.appTabs);
        vPager = findViewById(R.id.viewpager);

        final AccountType[] supportedType = AccountType.getSupportedType();

        //just in case filtering
        int selpos = 0;
        int i = 0;
        for (AccountType a : supportedType) {
            if (a.getType().equals(currentAccountType)) {
                selpos = i;
                break;
            }
            i++;
        }
        currentAccountType = supportedType[selpos].getType();

        vPager.setAdapter(new AccountTypePagerAdapter(getSupportFragmentManager(), supportedType));

        vAppTabs.setupWithViewPager(vPager);
        vAppTabs.getTabAt(selpos).select();


        vAppTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentAccountType = supportedType[tab.getPosition()].getType();
//                System.out.println(">onTabSelected>>>>>>>>>>>" + currentAccountType);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                currentAccountType = null;
//                System.out.println(">onTabUnselected>>>>>>>>>>>" + currentAccountType);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                currentAccountType = supportedType[tab.getPosition()].getType();
//                System.out.println(">onTabReselected>>>>>>>>>>>" + currentAccountType);
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
//                    vAppTabs.invalidate();
                }
            });

        }
    }

    private void reloadData() {

        //notify tab change
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
//            doNewAccount();
//            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        if (v.getId() == R.id.account_mgnt_list) {
//            getMenuInflater().inflate(R.menu.account_mgnt_ctxmenu, menu);
//        }
//
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//        if (item.getItemId() == R.id.menu_edit) {
//            doEditAccount(info.position);
//            return true;
//        } else if (item.getItemId() == R.id.menu_delete) {
//            doDeleteAccount(info.position);
//            return true;
//        } else if (item.getItemId() == R.id.menu_copy) {
//            doCopyAccount(info.position);
//            return true;
//        } else {
//            return super.onContextItemSelected(item);
//        }
//    }

//    private void doDeleteAccount(int pos) {
//        final Account acc = listData.get(pos);
//        final String name = acc.getName();
//
//        GUIs.confirm(this, i18n().string(R.string.qmsg_delete_account, acc.getName()), new GUIs.OnFinishListener() {
//            public boolean onFinish(Object data) {
//                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
//                    boolean r = contexts().getDataProvider().deleteAccount(acc.getId());
//                    if (r) {
//                        GUIs.shortToast(AccountMgntActivity.this, i18n().string(R.string.msg_account_deleted, name));
//                        reloadData();
//                        trackEvent(TE.DELETE_ACCOUNT);
//                    }
//                }
//                return true;
//            }
//        });
//    }
//
//    private void doEditAccount(int pos) {
//        Account acc = listData.get(pos);
//        Intent intent = null;
//        intent = new Intent(this, AccountEditorActivity.class);
//        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, false);
//        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }
//
//    private void doCopyAccount(int pos) {
//        Account acc = listData.get(pos);
//        Intent intent = null;
//        intent = new Intent(this, AccountEditorActivity.class);
//        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, true);
//        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }

//    private void doNewAccount() {
//        Account acc = new Account(currentAccountType, "", 0D);
//        Intent intent = null;
//        intent = new Intent(this, AccountEditorActivity.class);
//        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, true);
//        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//        if (parent == vList) {
//            doEditAccount(pos);
//        }
//    }

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
            b.putString(AccountMgntFragment.PARAM_ACCOUNT_TYPE, types[position].getType());
            f.setArguments(b);
            return f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return types[position].getDisplay(Contexts.instance().getI18n());
        }
    }
}