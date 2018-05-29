package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
@Deprecated
public class DesktopMgntFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_DESKTOP_NAME = "desktopName";

    private String desktopName = null;

    private Desktop desktop = null;

    private List<DesktopItem> recyclerDataList;

    View vNoData;
    private RecyclerView vRecycler;

    private DesktopItemRecyclerAdapter recyclerAdapter;

    private LayoutInflater inflater;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.desktop_mgnt_frag, container, false);
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
        desktopName = args.getString(ARG_DESKTOP_NAME);

    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        vNoData = rootView.findViewById(R.id.no_data);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new DesktopItemRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.desktop_mgnt_recycler);
        GridLayoutManager glm = new GridLayoutManager(activity, calColumn());
        vRecycler.setLayoutManager(glm);
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<DesktopItem>() {
            @Override
            public void onSelect(Set<DesktopItem> selection) {
                lookupQueue().publish(QEvents.DesktopMgntFrag.ON_SELECT_DESKTOP_TEIM, selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(DesktopItem selected) {
                lookupQueue().publish(QEvents.DesktopMgntFrag.ON_RESELECT_DESKTOP_TEIM, selected);
                return true;
            }
        });
    }

    private int calColumn() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 84);//72+6+6, read desktop_mgnt_item.xml

        switch(preference().getTextSize()){
            case Preference.TEXT_SIZE_LARGE:
                noOfColumns--;
            case Preference.TEXT_SIZE_MEDIUM:
                noOfColumns--;
        }


        if(noOfColumns<2){
            noOfColumns = 2;
        }
        return noOfColumns;
    }

    private void reloadData() {

        Map<String,Desktop> supportedDesktops = Desktop.getSupportedDesktops(getContextsActivity());

        Desktop desktop = supportedDesktops.get(desktopName);

        List<DesktopItem> data = desktop.getDesktopItems();

        recyclerDataList.clear();

        if(data.size()==0){
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.VISIBLE);
        }else {
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
            case QEvents.DesktopMgntFrag.ON_CLEAR_SELECTION:
                recyclerAdapter.clearSelection();
                break;
            case QEvents.DesktopMgntFrag.ON_RELOAD_FRAGMENT:
                recyclerAdapter.clearSelection();
                reloadData();
                break;
        }
    }

    public class DesktopItemRecyclerAdapter extends SelectableRecyclerViewAdaptor<DesktopItem, DesktopViewHolder> {

        public DesktopItemRecyclerAdapter(ContextsActivity activity, List<DesktopItem> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public DesktopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.desktop_mgnt_item, parent, false);
            return new DesktopViewHolder(this, viewItem);
        }
    }

    public class DesktopViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<DesktopItemRecyclerAdapter, DesktopItem> {

        public DesktopViewHolder(DesktopItemRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(DesktopItem desktopItem) {
            super.bindViewValue(desktopItem);

            ImageView vicon = itemView.findViewById(R.id.desktop_icon);
            TextView vtext = itemView.findViewById(R.id.desktop_label);

            vicon.setImageResource(desktopItem.getIcon());
            vtext.setText(desktopItem.getLabel());

        }
    }
}