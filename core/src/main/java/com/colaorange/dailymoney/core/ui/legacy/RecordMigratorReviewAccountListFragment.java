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

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class RecordMigratorReviewAccountListFragment extends ContextsFragment implements EventQueue.EventListener {

    public enum StepMode {
        CREATE_NEW, UPDATE_EXISTING
    }

    public static final String ARG_POS = "pos";

    public static final String ARG_STEP_MODE = "stepMode";

    public static final String ARG_DISABLE_SELECTION = "disableSelection";

    private View vNoData;
    private TextView vNoDataText;

    private int pos;
    private StepMode stepMode;
    private boolean disableSelection;

    private List<RecordMigratorActivity.ReviewAccount> recyclerDataList;
    private RecyclerView vRecycler;
    private ReviewAccountRecyclerAdapter recyclerAdapter;

    private View rootView;

    Map<AccountType, Integer> accountBgColorMap;
    Map<AccountType, Integer> accountTextColorMap;
    I18N i18n;
    CalendarHelper calendarHelper;

    boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_migrator_review_account_list_frag, container, false);
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
        stepMode = (StepMode)args.getSerializable(ARG_STEP_MODE);
        disableSelection = args.getBoolean(ARG_DISABLE_SELECTION, false);
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        vNoData = rootView.findViewById(R.id.no_data);
        vNoDataText = rootView.findViewById(R.id.no_data_text);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new ReviewAccountRecyclerAdapter(activity, recyclerDataList);
        recyclerAdapter.setDisableSelection(disableSelection);
        vRecycler = rootView.findViewById(R.id.account_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);

        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        i18n = Contexts.instance().getI18n();
        calendarHelper = calendarHelper();
    }

    private void reloadData(List<RecordMigratorActivity.ReviewAccount> data) {

        final CalendarHelper cal = calendarHelper();
        final IDataProvider idp = contexts().getDataProvider();


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
            case QEvents.MigrateReviewAccountListFrag.ON_RELOAD_FRAGMENT:
                Integer pos = event.getArg(ARG_POS);
                if (pos != null && pos.intValue() == this.pos) {
                    reloadData((List<RecordMigratorActivity.ReviewAccount>) event.getData());
                    return;
                }
                break;
        }
    }

    public class ReviewAccountRecyclerAdapter extends SelectableRecyclerViewAdaptor<RecordMigratorActivity.ReviewAccount, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

        private LayoutInflater inflater;
        private boolean disableSelection;

        public ReviewAccountRecyclerAdapter(ContextsActivity activity, List<RecordMigratorActivity.ReviewAccount> data) {
            super(activity, data);
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public boolean isDisableSelection() {
            return disableSelection;
        }

        public void setDisableSelection(boolean disableSelection) {
            this.disableSelection = disableSelection;
        }

        @Override
        public boolean isSelectable(RecordMigratorActivity.ReviewAccount obj) {
            return disableSelection ? false : true;
        }

        @NonNull
        @Override
        public SelectableRecyclerViewAdaptor.SelectableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int l ;
            switch(stepMode){
                default:
                case UPDATE_EXISTING:
                case CREATE_NEW:
                    l = R.layout.record_migrator_review_account_list_item;
                break;
            }
            View viewItem = inflater.inflate(l, parent, false);
            return new ReviewAccountAccountViewHolder(this, viewItem);
        }


        private class ReviewAccountAccountViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<ReviewAccountRecyclerAdapter, RecordMigratorActivity.ReviewAccount> {

            public ReviewAccountAccountViewHolder(ReviewAccountRecyclerAdapter adapter, View itemView) {
                super(adapter, itemView);
            }

            @Override
            public void bindViewValue(RecordMigratorActivity.ReviewAccount reviewAccount) {
                super.bindViewValue(reviewAccount);
                Account account = reviewAccount.account;
                TextView vname = itemView.findViewById(R.id.account_item_name);
                TextView vid = itemView.findViewById(R.id.account_item_id);
                TextView initvalue = itemView.findViewById(R.id.account_item_initvalue);

                vname.setText(account.getName());
                vid.setText(account.getId());
                String initVal = i18n.string(R.string.label_initial_value) + " : " + Formats.double2String(account.getInitialValue());//


                int textColor = accountTextColorMap.get(AccountType.find(account.getType()));

                vname.setTextColor(textColor);
                vid.setTextColor(textColor);
                initvalue.setTextColor(textColor);

                switch(stepMode){
                    case UPDATE_EXISTING:
                        initVal = initVal + " >> "+Formats.double2String(reviewAccount.newInitialValue);
                        break;
                    default:
                    case CREATE_NEW:
                        break;
                }

                initvalue.setText(initVal);
            }
        }
    }
}