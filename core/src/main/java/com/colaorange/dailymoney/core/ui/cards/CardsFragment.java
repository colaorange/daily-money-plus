package com.colaorange.dailymoney.core.ui.cards;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.RecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class CardsFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_CARDS_POS = "cardsPos";

    private int cardsPos;

    private List<Card> recyclerDataList;

    View vNoData;
    private RecyclerView vRecycler;

    private CardFragmentAdapter cardFragmentAdapter;
    private CardEditorAdapter cardEditorAdapter;

    private LayoutInflater inflater;

    private View rootView;

    private CardFacade cardFacade;

    protected boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.cards_frag, container, false);
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
        cardsPos = args.getInt(ARG_CARDS_POS, 0);
    }

    private void initMembers() {

        ContextsActivity activity = getContextsActivity();
        cardFacade = new CardFacade(activity);

        lightTheme = activity.isLightTheme();

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        vNoData = rootView.findViewById(R.id.no_data);

        recyclerDataList = new LinkedList<>();
        cardFragmentAdapter = new CardFragmentAdapter(activity, recyclerDataList);
        cardEditorAdapter = new CardEditorAdapter(activity, recyclerDataList);


        vRecycler = rootView.findViewById(R.id.cards_recycler);
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        //prevent tab switch back
        cardFragmentAdapter.clearCreatedFragments();
    }

    private void reloadData() {

        CardCollection cards = preference().getCards(cardsPos);

        List<Card> data = new LinkedList<>();
        for (int i = 0; i < cards.size(); i++) {
            data.add(cards.get(i));
        }

        cardFragmentAdapter.clearCreatedFragments();

        recyclerDataList.clear();

        if (data.size() == 0) {
            vRecycler.setVisibility(View.GONE);
            vNoData.setVisibility(View.VISIBLE);
        } else {
            vRecycler.setVisibility(View.VISIBLE);
            vNoData.setVisibility(View.GONE);
            recyclerDataList.addAll(data);
        }

        if (!CardsDesktopActivity.isModeEdit()) {
            if (vRecycler.getAdapter() == cardFragmentAdapter) {
                cardFragmentAdapter.notifyDataSetChanged();
            } else {
                vRecycler.setAdapter(cardFragmentAdapter);
            }
        } else {
            if (vRecycler.getAdapter() == cardEditorAdapter) {
                cardEditorAdapter.notifyDataSetChanged();
            } else {
                vRecycler.setAdapter(cardEditorAdapter);
            }
        }


        cardFragmentAdapter.notifyDataSetChanged();
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
        Object data;
        switch (event.getName()) {
            case QEvents.CardsFrag.ON_RELOAD_FRAGMENT:
                data = event.getData();
                /**
                 * if no data, or data is my pos, reload my data.
                 */
                if (data == null || (data instanceof Integer && ((Integer) data).intValue() == cardsPos)) {
                    reloadData();
                }
                break;
            case QEvents.CardsFrag.ON_CLEAR_FRAGMENT:
                cardFragmentAdapter.clearCreatedFragments();
                break;
        }
    }

    public class CardFragmentAdapter extends RecyclerViewAdaptor<Card, RecyclerViewAdaptor.SimpleViewHolder> {

        Map<String, Fragment> createdFragments = new LinkedHashMap<>();

        public CardFragmentAdapter(ContextsActivity activity, List<Card> data) {
            super(activity, data);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }


        @Override
        public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);

            /**
             * create fragment when viewholder is binding to a position.
             * I don't check frag for existing here is because we always clear whole adapter when reload
             */

            int pos = position;

            Card card = get(pos);
            FragmentManager fragmentManager = getChildFragmentManager();
            String fragTag = card.getId();
            Fragment f = new CardFacade(getContextsActivity()).newFragement(cardsPos, pos, card);

            Logger.d(">>> new fragment {}:{} ", fragTag, f);

            //itemView id is dynamic, use replace or add?
            fragmentManager.beginTransaction()
                    .add(holder.itemView.getId(), f, fragTag)
                    .commit();
            createdFragments.put(fragTag, f);

        }


        private void clearCreatedFragments() {
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (String k : createdFragments.keySet()) {
                Fragment f = createdFragments.get(k);
                ft.detach(f);
                ft.remove(f);
                Logger.d(">>> remove fragment {}:{} ", k, f);
            }
            //very important to call commitNow, or will get error for immedidatelly adapter binding
            ft.commitNowAllowingStateLoss();
            createdFragments.clear();
        }


        @NonNull
        @Override
        public CardFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.card_anchor, parent, false);
            //use dynamic id , so we can add fragment to it later.
            itemView.setId(activity.generateViewId());

            return new CardFragmentViewHolder(this, itemView);
        }
    }

    public class CardFragmentViewHolder extends RecyclerViewAdaptor.SimpleViewHolder<CardFragmentAdapter, Card> {

        public CardFragmentViewHolder(CardFragmentAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Card card) {
            super.bindViewValue(card);

        }
    }

    public class CardEditorAdapter extends RecyclerViewAdaptor<Card, RecyclerViewAdaptor.SimpleViewHolder> {

        public CardEditorAdapter(ContextsActivity activity, List<Card> data) {
            super(activity, data);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }


        @Override
        public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }


        @NonNull
        @Override
        public CardEditorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.card_editor, parent, false);

            Toolbar vtoolbar = itemView.findViewById(R.id.card_toolbar);
            vtoolbar.inflateMenu(R.menu.card_editable_menu);

            return new CardEditorHolder(this, itemView);
        }
    }

    public class CardEditorHolder extends RecyclerViewAdaptor.SimpleViewHolder<CardEditorAdapter, Card> {

        public CardEditorHolder(CardEditorAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Card card) {
            ContextsActivity activity = getContextsActivity();

            Toolbar vtoolbar = itemView.findViewById(R.id.card_toolbar);
            TextView vtext = itemView.findViewById(R.id.card_content);

            int pos = getAdapterPosition();

            Preference preference = preference();
            CardCollection cards = preference.getCards(cardsPos);
            card = cards.get(pos);

            vtoolbar.setTitle(card.getTitle());
            vtext.setText(cardFacade.getTypeText(card.getType()));

            Menu mMenu = vtoolbar.getMenu();
            vtoolbar.setOnMenuItemClickListener(new CardOnMenuItemClickListener(pos));
            MenuItem mi = mMenu.findItem(R.id.menu_edit);
            mi.setVisible(cardFacade.isTypeEditable(card.getType()));

            mi = mMenu.findItem(R.id.menu_move_up);
            mi.setEnabled(pos > 0);
            mi.setIcon(activity.buildDisabledIcon(activity.resolveThemeAttrResId(R.attr.ic_arrow_up), mi.isEnabled()));

            mi = mMenu.findItem(R.id.menu_move_down);
            mi.setEnabled(pos < cards.size() - 1);
            mi.setIcon(activity.buildDisabledIcon(activity.resolveThemeAttrResId(R.attr.ic_arrow_down), mi.isEnabled()));

            mi = mMenu.findItem(R.id.menu_mode_show_title);
            boolean showTitle = card.getArg(CardFacade.ARG_SHOW_TITLE, false);
            mi.setChecked(showTitle);

            int color = activity.resolveThemeAttrResData(R.attr.appPrimaryTextColor);
            if (!showTitle) {
                if(lightTheme) {
                    color = Colors.lighten(color, 0.3f);
                }else{
                    color = Colors.darken(color, 0.3f);
                }
            }
            vtoolbar.setTitleTextColor(color);
        }
    }

    private class CardOnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        int pos;

        public CardOnMenuItemClickListener(int pos) {
            this.pos = pos;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.menu_edit_title) {
                doEditTitle(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_move_up) {
                doMoveUp(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_move_down) {
                doMoveDown(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                doDelete(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_mode_show_title) {
                item.setChecked(!item.isChecked());
                doModeShowTitle(pos, item.isChecked());
                return true;
            }
            return false;
        }
    }

    private void doModeShowTitle(int pos, boolean showTitle) {
        Preference preference = Contexts.instance().getPreference();

        CardCollection cards = preference.getCards(cardsPos);
        Card card = cards.get(pos);
        card.withArg(CardFacade.ARG_SHOW_TITLE, showTitle);

        cards.set(pos, card);
        preference.updateCards(cardsPos, cards);

        cardEditorAdapter.notifyItemChanged(pos);
    }

    private void doDelete(final int pos) {
        I18N i18n = i18n();
        GUIs.confirm(getContextsActivity(), i18n.string(R.string.qmsg_common_confirm, i18n.string(R.string.act_delete)),
                new GUIs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (GUIs.OK_BUTTON == which) {
                            Preference preference = Contexts.instance().getPreference();

                            CardCollection cards = preference.getCards(cardsPos);
                            if (pos >= cards.size()) {
                                return true;
                            }

                            cards.remove(pos);

                            preference.updateCards(cardsPos, cards);

                            recyclerDataList.remove(pos);
                            cardEditorAdapter.notifyItemRemoved(pos);
                        }
                        return true;
                    }
                });

    }

    private void doMoveDown(int pos) {
        Preference preference = Contexts.instance().getPreference();

        CardCollection cards = preference.getCards(cardsPos);
        if (pos >= cards.size()) {
            return;
        }
        cards.move(pos, pos + 1);
        preference.updateCards(cardsPos, cards);

        recyclerDataList.set(pos, cards.get(pos));
        recyclerDataList.set(pos + 1, cards.get(pos + 1));

        cardEditorAdapter.notifyItemMoved(pos, pos + 1);
    }

    private void doMoveUp(int pos) {
        if (pos <= 0) {
            return;
        }
        Preference preference = Contexts.instance().getPreference();
        CardCollection cards = preference.getCards(cardsPos);
        cards.move(pos, pos - 1);
        preference.updateCards(cardsPos, cards);

        recyclerDataList.set(pos, cards.get(pos));
        recyclerDataList.set(pos - 1, cards.get(pos - 1));

        cardEditorAdapter.notifyItemMoved(pos, pos - 1);

    }

    private void doEditTitle(final int pos) {
        I18N i18n = i18n();

        CardCollection cards = preference().getCards(cardsPos);
        Card card = cards.get(pos);

        GUIs.inputText(getContextsActivity(), i18n.string(R.string.act_edit_title),
                i18n.string(R.string.msg_edit_card_title),
                i18n.string(R.string.act_ok), i18n().string(R.string.act_cancel),
                InputType.TYPE_CLASS_TEXT, card.getTitle(), new GUIs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (which == GUIs.OK_BUTTON) {
                            CardCollection cards = preference().getCards(cardsPos);
                            Card card = cards.get(pos);
                            card.setTitle((String) data);

                            preference().updateCards(cardsPos, cards);


                            recyclerDataList.set(pos, card);

                            cardEditorAdapter.notifyItemChanged(pos);
                        }
                        return true;
                    }
                });
    }

}