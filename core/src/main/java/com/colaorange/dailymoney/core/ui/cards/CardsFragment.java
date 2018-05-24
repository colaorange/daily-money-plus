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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.ui.helper.RecyclerViewAdaptor;
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

    private CardCollection cards = null;

    private List<Card> recyclerDataList;

    View vNoData;
    private RecyclerView vRecycler;

    private CardRecyclerAdapter recyclerAdapter;

    private LayoutInflater inflater;

    private View rootView;


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
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        vNoData = rootView.findViewById(R.id.no_data);

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new CardRecyclerAdapter(activity, recyclerDataList);
        vRecycler = rootView.findViewById(R.id.cards_recycler);
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        reloadData();
    }

    private void reloadData() {

        cards = preference().getCards(cardsPos);

        List<Card> data = new LinkedList<>();
        for (int i = 0; i < cards.size(); i++) {
            data.add(cards.get(i));
        }

        recyclerDataList.clear();
        recyclerAdapter.clearCreatedFragments();

        if (data.size() == 0) {
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
            case QEvents.CardsFrag.ON_RELOAD_FRAGMENT:
                reloadData();
                break;
        }
    }

    public class CardRecyclerAdapter extends RecyclerViewAdaptor<Card, RecyclerViewAdaptor.SimpleViewHolder> {

        Map<String, Fragment> createdFragments = new LinkedHashMap<>();

        public CardRecyclerAdapter(ContextsActivity activity, List<Card> data) {
            super(activity, data);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }


        private String getFragTag(int pos) {
            return "cards:" + cardsPos + ":" + pos;
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
            String fragTag = getFragTag(pos);
            Fragment f = new CardFacade(getContextsActivity()).newFragement(cardsPos, pos, card);

            Logger.d(">>> new fragment {}:{} ", fragTag, f);

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
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.card_anchor, parent, false);
            //use dynamic id , so we can add fragment to it later.
            viewItem.setId(activity.generateViewId());

            return new CardViewHolder(this, viewItem);
        }
    }

    public class CardViewHolder extends RecyclerViewAdaptor.SimpleViewHolder<CardRecyclerAdapter, Card> {

        public CardViewHolder(CardRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Card card) {
            super.bindViewValue(card);

        }
    }
}