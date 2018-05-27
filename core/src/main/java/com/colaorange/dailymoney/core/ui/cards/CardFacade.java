package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.CardCollection;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.ui.nav.NavPageFacade;
import com.colaorange.dailymoney.core.util.Dialogs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author dennis
 */
public class CardFacade {
    public static final String ARG_NAV_PAGES_LIST = "list";
    public static final String ARG_SHOW_TITLE = "show_title";

    ContextsActivity activity;
    I18N i18n;

    public CardFacade(ContextsActivity activity) {
        this.activity = activity;
        i18n = Contexts.instance().getI18n();
    }

    public Fragment newFragement(int cardsPos, int pos, Card card) {
        switch (card.getType()) {
            case NAV_PAGES:
                return newNavPagesFragment(cardsPos, pos, card);
            case INFO_EXPENSE:
                return newInfoExpenseFragment(cardsPos, pos, card);
        }
        throw new IllegalStateException("unknown card fragment " + card.getType());
    }

    private Bundle newBaseBundle(int cardsPos, int pos) {
        Bundle b = new Bundle();
        b.putSerializable(CardInfoExpenseFragment.ARG_CARDS_POS, cardsPos);
        b.putSerializable(CardInfoExpenseFragment.ARG_POS, pos);
        return b;
    }

    private Fragment newInfoExpenseFragment(int cardsPos, int pos, Card card) {
        CardInfoExpenseFragment f = new CardInfoExpenseFragment();
        Bundle b = newBaseBundle(cardsPos, pos);
        f.setArguments(b);
        return f;
    }

    private Fragment newNavPagesFragment(int cardsPos, int pos, Card card) {
        CardNavPagesFragment f = new CardNavPagesFragment();
        Bundle b = newBaseBundle(cardsPos, pos);
        f.setArguments(b);
        return f;
    }

    public String getTypeText(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return i18n.string(R.string.card_nav_page);
            case INFO_EXPENSE:
                return i18n.string(R.string.card_info_expense);
        }
        return i18n.string(R.string.label_unknown);
    }

    public boolean isTypeEditable(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return true;
            case INFO_EXPENSE:
                return false;
        }
        return false;
    }

    public void doEditArgs(int cardsPos, int pos, Card card, OnOKListener listener) {
        switch (card.getType()) {
            case NAV_PAGES:
                doEditNavPagesArgs(cardsPos, pos, card, listener);
                return;
            case INFO_EXPENSE:
            default:
                return;
        }
    }

    private void doEditNavPagesArgs(final int cardsPos, final int pos, Card card, final OnOKListener listener) {
        List<NavPage> values = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        Set<NavPage> selection = new LinkedHashSet<>();

        NavPageFacade pgFacade = new NavPageFacade(activity);

        for (NavPage pg : pgFacade.listPrimary()) {
            values.add(pg);
        }
        for (NavPage pg : values) {
            labels.add(pgFacade.getPageText(pg));
        }
        try {
            List<String> sl = card.getArg(ARG_NAV_PAGES_LIST);
            if (sl != null) {
                for (String s : sl) {
                    selection.add(NavPage.valueOf(s));
                }
            }
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }
        Logger.d(">>> old nav_page selection {}", selection);

        Dialogs.showSelectionList(activity, i18n.string(R.string.act_edit_args),
                i18n.string(R.string.msg_edit_nav_pages_args), (List) values, labels, true,
                (Set) selection, new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (Dialogs.OK_BUTTON == which) {
                            Preference preference = Contexts.instance().getPreference();

                            Set<String> selection = (Set<String>) data;
                            CardCollection cards = preference.getCards(cardsPos);
                            Card card = cards.get(pos);
                            card.withArg(ARG_NAV_PAGES_LIST, selection);
                            Logger.d(">>> new nav_page selection {}", selection);
                            preference.updateCards(cardsPos, cards);

                            listener.onOK(card);
                        }
                        return true;
                    }
                });
    }

    public String getCardInfo(Card card) {
        StringBuilder sb = new StringBuilder(getTypeText(card.getType()));

        List list;
        switch (card.getType()) {
            case NAV_PAGES:
                sb.append(" : ");
                list = card.getArg(ARG_NAV_PAGES_LIST);
                if (list != null && list.size() > 0) {
                    sb.append(i18n.string(R.string.msg_n_items, list.size()));
                }else{
                    sb.append(i18n.string(R.string.msg_no_data));
                }
                break;
            case INFO_EXPENSE:
            default:
                break;
        }

        return sb.toString();
    }

    public interface OnOKListener{
        void onOK(Card card);
    }
}
