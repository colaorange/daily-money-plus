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
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.colaorange.dailymoney.core.util.Dialogs;
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

    private ItemTouchHelper cardDragHelper;

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

        ItemTouchHelper.Callback callback =
                new CardDragCallback();

        cardDragHelper = new ItemTouchHelper(callback);
        cardDragHelper.attachToRecyclerView(vRecycler);
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadData();
    }

    @Override
    public void onPause() {
        super.onPause();
        //clear all on pause to prevent error when xxback try to reload fragment but view is not here already
        cardFragmentAdapter.clearCreatedFragments();
    }

    private void reloadData() {

        CardCollection cards = preference().getCards(cardsPos);

        List<Card> data = new LinkedList<>();
        for (int i = 0; i < cards.size(); i++) {
            data.add(cards.get(i));
        }

        //clear again.
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


        private void clearCreatedFragments() {
            if (createdFragments.size() > 0) {
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


            FragmentManager fragmentManager = getChildFragmentManager();
            String fragTag = card.getId();

            if (fragmentManager.findFragmentByTag(fragTag) != null) {
                //bound, ignore it
            } else {
                int pos = getAdapterPosition();

                Fragment f = new CardFacade(getContextsActivity()).newFragement(cardsPos, pos, card);

                Logger.d(">>> new fragment {}:{}:{} ", fragTag, "0x" + Integer.toHexString(itemView.getId()), f);

                //itemView id is dynamic, use replace or add?
                fragmentManager.beginTransaction()
                        .add(itemView.getId(), f, fragTag)
                        .commit();
                adaptor.createdFragments.put(fragTag, f);
            }
        }
    }

    public class CardEditorAdapter extends RecyclerViewAdaptor<Card, RecyclerViewAdaptor.SimpleViewHolder> {

        public CardEditorAdapter(ContextsActivity activity, List<Card> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public CardEditorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = inflater.inflate(R.layout.card_editor, parent, false);

            Toolbar vtoolbar = itemView.findViewById(R.id.card_toolbar);
            vtoolbar.inflateMenu(R.menu.card_editable_menu);

            return new CardEditorViewHolder(this, itemView);
        }
    }

    public class CardEditorViewHolder extends RecyclerViewAdaptor.SimpleViewHolder<CardEditorAdapter, Card> {

        public CardEditorViewHolder(CardEditorAdapter adapter, View itemView) {
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
            vtoolbar.setOnMenuItemClickListener(new CardOnMenuItemClickListener(this) {
                @Override
                int getPosition() {
                    return getAdapterPosition();
                }
            });
            MenuItem mi = mMenu.findItem(R.id.menu_edit);
            mi.setVisible(cardFacade.isTypeEditable(card.getType()));

            mi = mMenu.findItem(R.id.menu_mode_show_title);
            boolean showTitle = card.getArg(CardFacade.ARG_SHOW_TITLE, false);
            mi.setChecked(showTitle);

            int color = activity.resolveThemeAttrResData(R.attr.appPrimaryTextColor);
            if (!showTitle) {
                if (lightTheme) {
                    color = Colors.lighten(color, 0.3f);
                } else {
                    color = Colors.darken(color, 0.3f);
                }
            }
            vtoolbar.setTitleTextColor(color);
        }
    }

    private abstract class CardOnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {
        RecyclerView.ViewHolder viewHolder;

        public CardOnMenuItemClickListener(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        abstract int getPosition();

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int pos = getPosition();
            if (item.getItemId() == R.id.menu_edit_title) {
                doEditTitle(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                doDelete(pos);
                return true;
            } else if (item.getItemId() == R.id.menu_mode_show_title) {
                item.setChecked(!item.isChecked());
                doModeShowTitle(pos, item.isChecked());
                return true;
            } else if (item.getItemId() == R.id.menu_move) {
                GUIs.shortToast(getContextsActivity(), R.string.msg_press_long_move);
                return true;
            } else if (item.getItemId() == R.id.menu_edit) {
                doEditArg(pos);
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

    private void doMove(int pos, int posTo) {
        Preference preference = Contexts.instance().getPreference();

        CardCollection cards = preference.getCards(cardsPos);
        if (pos >= cards.size()) {
            return;
        }
        cards.move(pos, posTo);
        preference.updateCards(cardsPos, cards);

        recyclerDataList.set(pos, cards.get(pos));
        recyclerDataList.set(posTo, cards.get(posTo));

        cardEditorAdapter.notifyItemMoved(pos, posTo);
    }


    private void doEditTitle(final int pos) {
        I18N i18n = i18n();

        CardCollection cards = preference().getCards(cardsPos);
        Card card = cards.get(pos);

        Dialogs.showTextEditor(getContextsActivity(), i18n.string(R.string.act_edit_title),
                i18n.string(R.string.msg_edit_card_title),
                InputType.TYPE_CLASS_TEXT, card.getTitle(), new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (which == Dialogs.OK_BUTTON) {
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

    private void doEditArg(final int pos) {
        CardCollection cards = preference().getCards(cardsPos);
        Card card = cards.get(pos);

        cardFacade.doEditArgs(cardsPos, pos, card);
    }

    public class CardDragCallback extends ItemTouchHelper.Callback {

        public CardDragCallback() {
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return CardsDesktopActivity.isModeEdit();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            doMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }


    }

}