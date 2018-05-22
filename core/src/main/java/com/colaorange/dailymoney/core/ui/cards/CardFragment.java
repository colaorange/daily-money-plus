package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List;

/**
 * @author dennis
 */
abstract public class CardFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_POS = "pos";
    public static final String ARG_CARD = "card";

    private View vNoData;
    private TextView vNoDataText;

    private int pos;

    private View rootView;

    I18N i18n;

    boolean lightTheme;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_nav_pages_frag, container, false);
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

        i18n = Contexts.instance().getI18n();

    }

    private void reloadData(List<Record> data) {

        final CalendarHelper cal = calendarHelper();
        final IDataProvider idp = contexts().getDataProvider();


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
            case QEvents.CardsFrag.ON_RELOAD_CARD_VIEW:
//                Integer pos = event.getArg(ARG_POS);
//                if (pos != null && pos.intValue() == this.pos) {
//                    reloadData((List<Record>) event.getData());
//                    return;
//                }
                break;
        }
    }
}