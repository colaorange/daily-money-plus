package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public abstract class CardBaseFragment extends ContextsFragment implements EventQueue.EventListener {

    public static final String ARG_CARDS_POS = "cardsPos";
    public static final String ARG_POS = "pos";

    protected int pos;
    protected int cardsPos;

    protected Card card;

    protected View rootView;
    private Toolbar vToolbar;
    View vNoData;
    private View vContent;

    private boolean showTitle;

    protected I18N i18n;

    protected boolean lightTheme;

    protected abstract int getLayoutResId();

    /**
     * id of editMode menu, return -1 if you don't hava
     */
    protected abstract int getMenuResId();

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


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
        reloadView();
    }

    @CallSuper
    protected void initArgs() {
        Bundle args = getArguments();
        cardsPos = args.getInt(ARG_CARDS_POS, 0);
        pos = args.getInt(ARG_POS, 0);
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
        CardCollection cards = preference.getCards(cardsPos);
        card = cards.get(pos);

        if (vToolbar != null) {
            vToolbar.setTitle(card.getTitle());

            vToolbar.getMenu().clear();
            if (CardsActivity.isModeEdit()) {
                vToolbar.setBackgroundColor(getContextsActivity().resolveThemeAttrResData(R.attr.appPrimaryColor));
                final int menuId = getMenuResId();
                if (menuId > 0) {
                    final Toolbar.OnMenuItemClickListener l = new Toolbar.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return CardBaseFragment.this.onMenuItemClick(item);
                        }
                    };

                    vToolbar.inflateMenu(menuId);
                    vToolbar.setOnMenuItemClickListener(l);
                    Menu mMenu = vToolbar.getMenu();

                    MenuItem mi = mMenu.findItem(R.id.menu_move_up);
                    mi.setEnabled(pos > 0);
                    mi.setIcon(activity.buildDisabledIcon(activity.resolveThemeAttrResId(R.attr.ic_arrow_up), mi.isEnabled()));

                    mi = mMenu.findItem(R.id.menu_move_down);
                    mi.setEnabled(pos < cards.size() - 1);
                    mi.setIcon(activity.buildDisabledIcon(activity.resolveThemeAttrResId(R.attr.ic_arrow_down), mi.isEnabled()));

                    mi = mMenu.findItem(R.id.menu_mode_show_title);
                    mi.setChecked(showTitle = card.getArg(CardFacade.ARG_SHOW_TITLE, showTitle));
                }
            } else {
                vToolbar.setBackgroundColor(getContextsActivity().resolveThemeAttrResData(R.attr.appPrimaryLightColor));
            }

            if (!CardsActivity.isModeEdit()) {
                vToolbar.setVisibility(showTitle ? View.VISIBLE : View.GONE);
            } else {
                vToolbar.setVisibility(View.VISIBLE);
            }

            doAfterReloadToolbar(vToolbar, CardsActivity.isModeEdit());
        }

        //don't show content to highlight user , it is edit now
        if (!doReloadContent(CardsActivity.isModeEdit())) {
            vContent.setVisibility(View.GONE);
        } else {
            vContent.setVisibility(View.VISIBLE);
        }
        if (vNoData != null) {
            vNoData.setVisibility(CardsActivity.isModeEdit() ? View.GONE : vNoData.getVisibility());
        }
    }

    /**
     * help sub-class to show noData info
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
     * @return true if content should be show
     */
    protected boolean doReloadContent(boolean cardsEditMode) {
        return !cardsEditMode;
    }

    /**
     * call after a toolbar was handled in {@link #reloadView()}
     */
    protected void doAfterReloadToolbar(Toolbar vToolbar, boolean editMode) {
    }

    protected boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit_title) {
            doEditTitle();
            return true;
        } else if (item.getItemId() == R.id.menu_move_up) {
            doMoveUp();
            return true;
        } else if (item.getItemId() == R.id.menu_move_down) {
            doMoveDown();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            doDelete();
            return true;
        } else if (item.getItemId() == R.id.menu_mode_show_title) {
            item.setChecked(!item.isChecked());
            doModeShowTitle(item.isChecked());
            return true;
        }
        return false;
    }

    private void doModeShowTitle(boolean showTitle) {
        card.withArg(CardFacade.ARG_SHOW_TITLE, this.showTitle = showTitle);

        Preference preference = Contexts.instance().getPreference();

        CardCollection cards = preference.getCards(cardsPos);
        cards.set(pos, card);

        preference.updateCards(cardsPos, cards);

        //title only effect in display mode, currently is in edit mode, no need to update
    }

    private void doDelete() {
        //todo
    }

    private void doMoveDown() {
        Preference preference = Contexts.instance().getPreference();

        CardCollection cards = preference.getCards(cardsPos);
        if (pos >= cards.size()) {
            return;
        }
        cards.move(pos + 1, pos);
        preference.updateCards(cardsPos, cards);
        lookupQueue().publish(QEvents.CardsFrag.ON_RELOAD_FRAGMENT, null);
    }

    private void doMoveUp() {
        if (pos <= 0) {
            return;
        }
        Preference preference = Contexts.instance().getPreference();
        CardCollection cards = preference.getCards(cardsPos);
        cards.move(pos - 1, pos);
        preference.updateCards(cardsPos, cards);
        lookupQueue().publish(QEvents.CardsFrag.ON_RELOAD_FRAGMENT, null);
    }

    protected void publishReloadFragment() {
        lookupQueue().publish(QEvents.CardsFrag.ON_RELOAD_FRAGMENT, null);
    }

    private void doEditTitle() {
        //todo
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
        Logger.d(">>> onStart fragment {}:{}:{} ", cardsPos, pos, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
        Logger.d(">>> onStop fragment {}:{}:{} ", cardsPos, pos, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(">>> onDestroy fragment {}:{}:{} ", cardsPos, pos, this);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.CardFrag.ON_RELOAD_VIEW:
                reloadView();
                break;
        }
    }
}