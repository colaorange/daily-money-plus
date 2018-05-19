package com.colaorange.dailymoney.core.ui.legacy;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class RecordListFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_POS = "pos";

    private View vNoData;
    private TextView vNoDataText;

    private int pos;

    private List<Record> recyclerDataList;
    private RecyclerView vRecycler;
    private RecordRecyclerAdapter recyclerAdapter;

    private View rootView;

    private Map<String, Account> accountMap = new HashMap<>();
    Map<AccountType, Integer> accountBgColorMap;
    Map<AccountType, Integer> accountTextColorMap;
    I18N i18n;

    boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_list_frag, container, false);
        return rootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadData(null);
    }


    private void initArgs() {
        Bundle args = getArguments();

        pos = args.getInt(ARG_POS, 0);
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        vNoData = rootView.findViewById(R.id.no_data);
        vNoDataText = rootView.findViewById(R.id.no_data_text);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new RecordRecyclerAdapter(activity, recyclerDataList);
        recyclerAdapter.setAccountMap(accountMap);
        vRecycler = rootView.findViewById(R.id.record_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Record>() {
            @Override
            public void onSelect(Set<Record> selection) {
                lookupQueue().publish(QEvents.RecordListFrag.ON_SELECT_RECORD, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Record selected) {
                lookupQueue().publish(QEvents.RecordListFrag.ON_RESELECT_RECORD, selected);
                return true;
            }
        });

        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        i18n = Contexts.instance().getI18n();

    }

    private void reloadData(List<Record> data) {

        final CalendarHelper cal = calendarHelper();
        final IDataProvider idp = contexts().getDataProvider();

        accountMap.clear();
        for (Account acc : idp.listAccount(null)) {
            accountMap.put(acc.getId(), acc);
        }


        //refresh account
        recyclerAdapter.setAccountMap(accountMap);

        //refresh data
        recyclerDataList.clear();
        if (data == null) {
            recyclerDataList.clear();
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.GONE);
        } else if (data.size() == 0) {
            recyclerDataList.clear();
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.VISIBLE);
            vNoDataText.setText(R.string.msg_no_data);
        } else {
            vRecycler.setVisibility(View.VISIBLE);
            vNoData.setVisibility(View.GONE);
            recyclerDataList.addAll(data);
        }
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
            case QEvents.RecordListFrag.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.RecordListFrag.ON_RELOAD_FRAGMENT:
                Integer pos = event.getArg(ARG_POS);
                if (pos != null && pos.intValue() == this.pos) {
                    reloadData((List<Record>) event.getData());
                    return;
                }
                break;
        }
    }
}