package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.GUIs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * this activity manages the account (of record) with tab widgets of android,
 *
 * @author dennis
 * @see {@link AccountType}
 */
public class AccountMgntActivity extends ContextsActivity implements OnTabChangeListener, OnItemClickListener {


    private List<Account> listData = new ArrayList<>();

    private ListView vList;

    private AccountListAdapter listAdapter;

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


        listAdapter = new AccountListAdapter(this, listData);

        vList = findViewById(R.id.account_mgnt_list);
        vList.setAdapter(listAdapter);


        vList.setOnItemClickListener(this);

        registerForContextMenu(vList);
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

        AccountType type = AccountType.find(currTabTag);
        listData.clear();
        listData.addAll(idp.listAccount(type));

        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabChanged(String tabId) {
        currTabTag = tabId;
        refreshUI();
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.account_mgnt_list) {
            getMenuInflater().inflate(R.menu.account_mgnt_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.menu_edit) {
            doEditAccount(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            doDeleteAccount(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_copy) {
            doCopyAccount(info.position);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void doDeleteAccount(int pos) {
        final Account acc = listData.get(pos);
        final String name = acc.getName();

        GUIs.confirm(this, i18n().string(R.string.qmsg_delete_account, acc.getName()), new GUIs.OnFinishListener() {
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    boolean r = contexts().getDataProvider().deleteAccount(acc.getId());
                    if(r) {
                        refreshUI();
                        GUIs.shortToast(AccountMgntActivity.this, i18n().string(R.string.msg_account_deleted, name));
                        trackEvent(Contexts.TRACKER_EVT_DELETE);
                    }
                }
                return true;
            }
        });
    }

    private void doEditAccount(int pos) {
        Account acc = listData.get(pos);
        Intent intent = null;
        intent = new Intent(this, AccountEditorActivity.class);
        intent.putExtra(AccountEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(AccountEditorActivity.PARAM_ACCOUNT, acc);
        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
    }

    private void doCopyAccount(int pos) {
        Account acc = listData.get(pos);
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
        if (parent == vList) {
            doEditAccount(pos);
        }
    }

    private class AccountListAdapter extends ArrayAdapter<Account> {

        LayoutInflater inflater;

        public AccountListAdapter(@NonNull Context context, List<Account> list) {
            super(context, R.layout.account_mgnt_item, list);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            AccountViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.account_mgnt_item, null);
                convertView.setTag(holder = new AccountViewHolder());
            } else {
                holder = (AccountViewHolder) convertView.getTag();
            }

            holder.bindViewValue(getItem(position), convertView);

            return convertView;
        }


    }

    private class AccountViewHolder {

        public void bindViewValue(Account account, View convertView) {

            Map<AccountType, Integer> textColorMap = getAccountTextColorMap();

            TextView vname = convertView.findViewById(R.id.account_item_name);
            TextView vid = convertView.findViewById(R.id.account_item_id);
            TextView initvalue = convertView.findViewById(R.id.account_item_initvalue);

            vname.setText(account.getName());
            vid.setText(account.getId());
            initvalue.setText(i18n().string(R.string.label_initial_value) + " : " + Formats.double2String(account.getInitialValue()));

            int textColor = textColorMap.get(AccountType.find(account.getType()));

            vname.setTextColor(textColor);
            vid.setTextColor(textColor);
            initvalue.setTextColor(textColor);
        }
    }

}