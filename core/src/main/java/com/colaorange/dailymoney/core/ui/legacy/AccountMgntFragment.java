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
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class AccountMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_ACCOUNT_TYPE = "accountType";

    private AccountType accountType = null;

    private List<Account> recyclerDataList;

    private RecyclerView vRecycler;

    private AccountRecyclerAdapter recyclerAdapter;

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
                lookupQueue().publish(QEvents.AccountMgnt.ON_SELECT_ACCOUNT, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Account selected) {
                lookupQueue().publish(QEvents.AccountMgnt.ON_RESELECT_ACCOUNT, selected);
                return true;
            }
        });
    }

    private void reloadData() {
        IDataProvider idp = contexts().getDataProvider();
        recyclerDataList.clear();
        recyclerDataList.addAll(idp.listAccount(accountType));
        recyclerAdapter.notifyDataSetChanged();
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
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.AccountMgnt.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.AccountMgnt.ON_RELOAD_FRAGMENT:
                recyclerAdapter.clearSelection();
                reloadData();
                break;
        }
    }

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