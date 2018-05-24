package com.colaorange.dailymoney.core.ui.cards;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.ui.nav.NavPageFacade;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dennis
 */
public class CardNavPagesFragment extends CardBaseFragment implements EventQueue.EventListener {

    private List<NavPage> recyclerDataList;
    private RecyclerView vRecycler;

    private NavPageRecyclerAdapter recyclerAdapter;

    private LayoutInflater inflater;

    private NavPageFacade pageFacad;

    @Override
    protected void initMembers() {
        super.initMembers();

        i18n = Contexts.instance().getI18n();

        ContextsActivity activity = getContextsActivity();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pageFacad = new NavPageFacade(activity);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new NavPageRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.card_content);
        GridLayoutManager glm = new GridLayoutManager(activity, calColumn());
        vRecycler.setLayoutManager(glm);
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<NavPage>() {
            @Override
            public void onSelect(Set<NavPage> selection) {
                if (selection.size() > 0) {
                    pageFacad.doPage(selection.iterator().next());
                }
                recyclerAdapter.clearSelection();
            }

            @Override
            public boolean onReselect(NavPage selected) {

                return true;
            }
        });
    }

    private int calColumn() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 84);//72+6+6, read desktop_mgnt_item.xml

        switch (preference().getTextSize()) {
            case Preference.TEXT_SIZE_LARGE:
                noOfColumns--;
            case Preference.TEXT_SIZE_MEDIUM:
                noOfColumns--;
        }


        if (noOfColumns < 2) {
            noOfColumns = 2;
        }
        return noOfColumns;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.card_nav_pages_frag;
    }

    @Override
    protected int getMenuResId() {
        return R.menu.card_editable_menu;
    }

    @Override
    protected boolean doReloadContent(boolean editMode) {
        if (editMode) {
            return false;
        }


        List<String> data = card.getArg(CardFacade.ARG_NAV_PAGES_LIST, null);

        if (data == null) {
            setNoData(true);
            return true;
        }

        List<NavPage> npgdata = new LinkedList<>();
        for (String s : data) {
            try {
                npgdata.add(NavPage.valueOf(s));
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }

        if (npgdata.size() == 0) {
            setNoData(true);
            return true;
        }


        recyclerDataList.clear();

        if (data.size() == 0) {
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.VISIBLE);
        } else {
            vRecycler.setVisibility(View.VISIBLE);
            vNoData.setVisibility(View.GONE);
            recyclerDataList.addAll(npgdata);
        }
        recyclerAdapter.notifyDataSetChanged();

        return true;
    }


    public class NavPageRecyclerAdapter extends SelectableRecyclerViewAdaptor<NavPage, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

        public NavPageRecyclerAdapter(ContextsActivity activity, List<NavPage> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public DesktopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.card_nav_pages_item, parent, false);
            return new DesktopViewHolder(this, viewItem);
        }
    }

    public class DesktopViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<NavPageRecyclerAdapter, NavPage> {

        public DesktopViewHolder(NavPageRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(NavPage navPage) {
            super.bindViewValue(navPage);

            ImageView vicon = itemView.findViewById(R.id.item_icon);
            TextView vtext = itemView.findViewById(R.id.item_label);

            vicon.setImageResource(pageFacad.getPageIcon(navPage));
            vtext.setText(pageFacad.getPageText(navPage));

        }
    }
}