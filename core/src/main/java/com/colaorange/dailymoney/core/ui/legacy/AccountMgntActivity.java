package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * this activity manages the account (of detail) with tab widgets of android,
 * there are 4 type of account, income, expense, asset and liability.
 *
 * @author dennis
 * @see {@link AccountType}
 */
public class AccountMgntActivity extends ContextsActivity implements OnTabChangeListener, OnItemClickListener {


    private static String KEY_NAME = "NAME";
    private static String KEY_INITVAL = "initval";
    private static String KEY_ID = "NAME";

    private static String[] mappingKeys = new String[]{KEY_NAME, KEY_INITVAL, KEY_ID};

    private static int[] mappingResIds = new int[]{R.id.account_item_name, R.id.account_item_initvalue, R.id.account_item_id};

    private List<Account> listViewData = new ArrayList<Account>();

    private List<Map<String, Object>> listViewMapList = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private SimpleAdapter listViewAdapter;

    private String currTabTag = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_mgnt);
        initMembers();

        refreshUI();
    }

    private void initMembers() {

        //tabs
        TabHost tabs = findViewById(R.id.account_mgnt_tabs);
        tabs.setup();

        AccountType[] supportedType = AccountType.getSupportedType();
        Resources r = getResources();
        for (AccountType at : supportedType) {
            TabSpec tab = tabs.newTabSpec(at.getType());
            tab.setIndicator(AccountType.getDisplay(i18n(), at.getType()));
            tab.setContent(R.id.account_mgnt_list);
            tabs.addTab(tab);
            if (currTabTag == null) {
                //it is account type
                currTabTag = tab.getTag();
            }
        }
        // workaround, force refresh
        if (supportedType.length > 1) {
            tabs.setCurrentTab(1);
            tabs.setCurrentTab(0);
        }

        tabs.setOnTabChangedListener(this);


        //
        listViewAdapter = new SimpleAdapter(this, listViewMapList, R.layout.account_mgnt_item, mappingKeys, mappingResIds);
        listViewAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data, String text) {
                NamedItem item = (NamedItem) data;
                String name = item.getName();
                Account acc = (Account) item.getValue();
                //not textview, not initval
                if (!(view instanceof TextView)) {
                    return false;
                }
                AccountType at = AccountType.find(acc.getType());
                TextView tv = (TextView) view;

                if (at == AccountType.INCOME) {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountIncomeTextColor));
                } else if (at == AccountType.EXPENSE) {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountExpenseTextColor));
                } else if (at == AccountType.ASSET) {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountAssetTextColor));
                } else if (at == AccountType.LIABILITY) {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountLiabilityTextColor));
                } else if (at == AccountType.OTHER) {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountOtherTextColor));
                } else {
                    tv.setTextColor(resolveThemeAttrResData(R.attr.accountUnknownTextColor));
                }

                if (KEY_INITVAL.equals(name)) {
                    text = i18n().string(R.string.label_initial_value) + " : " + data.toString();
                    tv.setText(text);
                    return true;
                }
                return false;
            }
        });

        listView = findViewById(R.id.account_mgnt_list);
        listView.setAdapter(listViewAdapter);


        listView.setOnItemClickListener(this);

        registerForContextMenu(listView);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ACCOUNT_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    refreshUI();
                }
            });

        }
    }

    private void refreshUI() {
        IDataProvider idp = contexts().getDataProvider();
        listViewData = null;

        AccountType type = AccountType.find(currTabTag);
        listViewData = idp.listAccount(type);
        listViewMapList.clear();

        for (Account acc : listViewData) {
            Map<String, Object> row = new HashMap<String, Object>();
            listViewMapList.add(row);
            row.put(mappingKeys[0], new NamedItem(mappingKeys[0], acc, acc.getName()));
            row.put(mappingKeys[1], new NamedItem(mappingKeys[1], acc, Formats.double2String(acc.getInitialValue())));
            row.put(mappingKeys[2], new NamedItem(mappingKeys[2], acc, acc.getId()));
        }

        listViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabChanged(String tabId) {
        currTabTag = tabId;
        refreshUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_mgnt_optmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.account_mgnt_menu_new) {
            doNewAccount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.account_mgnt_list) {
            getMenuInflater().inflate(R.menu.account_mgnt_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.account_mgnt_menu_edit) {
            doEditAccount(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_mgnt_menu_delete) {
            doDeleteAccount(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_mgnt_menu_copy) {
            doCopyAccount(info.position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void doDeleteAccount(int pos) {
        Account acc = listViewData.get(pos);
        String name = acc.getName();

        contexts().getDataProvider().deleteAccount(acc.getId());
        refreshUI();
        GUIs.shortToast(this, i18n().string(R.string.msg_account_deleted, name));
        trackEvent(Contexts.TRACKER_EVT_DELETE);

    }

    private void doEditAccount(int pos) {
        Account acc = listViewData.get(pos);
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doCopyAccount(int pos) {
        Account acc = listViewData.get(pos);
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, true);
        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doNewAccount() {
        Account acc = new Account(currTabTag, "", 0D);
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, true);
        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == listView) {
            doEditAccount(pos);
        }
    }

}