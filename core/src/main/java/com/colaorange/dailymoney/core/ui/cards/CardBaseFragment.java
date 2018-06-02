package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardDesktop;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public abstract class CardBaseFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_DESKTOP_INDEX = "desktopIndex";
    public static final String ARG_INDEX = "index";

    protected int index;
    protected int desktopIndex;

    protected View rootView;
    private Toolbar vToolbar;
    private View vNoData;
    protected View vContent;

    protected boolean showTitle;

    protected I18N i18n;

    protected boolean lightTheme;

    protected abstract int getLayoutResId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutResId(), container, false);
        return rootView;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    public Card getCard() {
        CardDesktop desktop = preference().getDesktop(desktopIndex);
        Card card = desktop.get(index);
        return card;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadView();
        trackEvent(Contexts.TE.CARD+""+getClass().getSimpleName());
    }

    @CallSuper
    protected void initArgs() {
        Bundle args = getArguments();
        desktopIndex = args.getInt(ARG_DESKTOP_INDEX, 0);
        index = args.getInt(ARG_INDEX, 0);
    }

    @CallSuper
    protected void initMembers() {
        i18n = Contexts.instance().getI18n();
        ContextsActivity activity = getContextsActivity();
        lightTheme = activity.isLightTheme();

        vToolbar = rootView.findViewById(R.id.card_toolbar);
        vNoData = rootView.findViewById(R.id.no_data);
        vContent = rootView.findViewById(R.id.card_content);

    }

    protected void reloadView() {
        ContextsActivity activity = getContextsActivity();
        Preference preference = preference();
        CardDesktop desktop = preference.getDesktop(desktopIndex);
        Card card = desktop.get(index);
        if (vToolbar != null) {
            showTitle = card.getArg(CardFacade.ARG_SHOW_TITLE, showTitle);
            vToolbar.setTitle(card.getTitle());

            vToolbar.setVisibility(showTitle ? View.VISIBLE : View.GONE);

            doAfterReloadToolbar(vToolbar);
        }

        //don't show content to highlight user , it is edit now
        if (!doReloadContent()) {
            vContent.setVisibility(View.GONE);
            setNoData(true);
        } else {
            vContent.setVisibility(View.VISIBLE);
            setNoData(false);
        }
    }

    /**
     * help to show infor for card type and sub-class to show noData info
     */
    protected void setNoData(boolean noData) {
        if (vNoData != null) {
            vNoData.setVisibility(noData ? View.VISIBLE : View.GONE);
        }
        vContent.setVisibility(!noData ? View.VISIBLE : View.GONE);
    }

    /**
     * called in {@link #reloadView()},
     *
     * @return true if has data to show
     */
    abstract protected boolean doReloadContent();

    /**
     * call after a toolbar was handled in {@link #reloadView()}
     */
    protected void doAfterReloadToolbar(Toolbar vToolbar) {
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
//        Logger.d(">>> onStart fragment {}:{}:{} ", desktopIndex, index, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
//        Logger.d(">>> onStop fragment {}:{}:{} ", desktopIndex, index, this);
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        Logger.d(">>> onDestroy fragment {}:{}:{} ", desktopIndex, index, this);
//    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.CardFrag.ON_RELOAD_VIEW:
                reloadView();
                break;
        }
    }
}