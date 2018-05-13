package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * this activity manages the account (of record) with tab widgets of android,
 *
 * @author dennis
 * @see {@link AccountType}
 */
public class AccountMgntFragment extends ContextsFragment implements EventQueue.EventListener{

    public static final String ARG_ACCOUNT_TYPE = "accountType";

    private AccountType accountType = null;

    private List<Account> recyclerDataList;

    private RecyclerView vRecycler;

    private AccountRecyclerAdapter recyclerAdapter;

//    private OnAccountListener listener;

    private LayoutInflater inflater;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.account_mgnt_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadData();
    }


    private void initArgs() {
        Bundle args = getArguments();
        String type = args.getString(ARG_ACCOUNT_TYPE);
        accountType = AccountType.find(type);

    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new AccountRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.account_mgnt_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Account>() {
            @Override
            public void onSelect(Set<Account> selection) {
//                listener.onAccountSelected(selection.size() == 0 ? null : selection.iterator().next());
            }
        });
    }

//
//
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Constants.REQUEST_ACCOUNT_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
//            GUIs.delayPost(new Runnable() {
//                @Override
//                public void run() {
//                    reloadData();
//                }
//            });
//
//        }
//    }

    private void reloadData() {
        IDataProvider idp = contexts().getDataProvider();
        recyclerDataList.clear();
        recyclerDataList.addAll(idp.listAccount(accountType));
        recyclerAdapter.notifyDataSetChanged();
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
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()){
            case Constants.EVTQ_ON_CLEAR_ACCOUNT_SELECTION:
                recyclerAdapter.clearSelection();
                break;
        }
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        if (v.getId() == R.id.account_mgnt_list) {
//            getMenuInflater().inflate(R.menu.account_mgnt_ctxmenu, menu);
//        }
//
//    }
//
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
//
//    private void doDeleteAccount(int pos) {
//        final Account acc = listData.get(pos);
//        final String name = acc.getName();
//
//        GUIs.confirm(this, i18n().string(R.string.qmsg_delete_account, acc.getName()), new GUIs.OnFinishListener() {
//            public boolean onFinish(Object data) {
//                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
//                    boolean r = contexts().getDataProvider().deleteAccount(acc.getId());
//                    if(r) {
//                        reloadData();
//                        GUIs.shortToast(AccountMgntFragment.this, i18n().string(R.string.msg_account_deleted, name));
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
//        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, false);
//        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }
//
//    private void doCopyAccount(int pos) {
//        Account acc = listData.get(pos);
//        Intent intent = null;
//        intent = new Intent(this, AccountEditorActivity.class);
//        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, true);
//        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }
//
//    private void doNewAccount() {
//        Account acc = new Account(currTabTag, "", 0D);
//        Intent intent = null;
//        intent = new Intent(this, AccountEditorActivity.class);
//        intent.putExtra(AccountEditorActivity.ARG_MODE_CREATE, true);
//        intent.putExtra(AccountEditorActivity.ARG_ACCOUNT, acc);
//        startActivityForResult(intent, Constants.REQUEST_ACCOUNT_EDITOR_CODE);
//    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//        if (parent == vList) {
//            doEditAccount(pos);
//        }
//    }


    public class AccountRecyclerAdapter extends SelectableRecyclerViewAdaptor<Account, AccountViewHolder> {

        public AccountRecyclerAdapter(ContextsActivity activity, List<Account> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.account_mgnt_item, parent, false);
            return new AccountViewHolder(this, viewItem);
        }
    }

    public class AccountViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<AccountRecyclerAdapter, Account> {

        public AccountViewHolder(AccountRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Account account) {
            super.bindViewValue(account);

            Map<AccountType, Integer> textColorMap = getContextsActivity().getAccountTextColorMap();

            TextView vname = itemView.findViewById(R.id.account_item_name);
            TextView vid = itemView.findViewById(R.id.account_item_id);
            TextView initvalue = itemView.findViewById(R.id.account_item_initvalue);

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