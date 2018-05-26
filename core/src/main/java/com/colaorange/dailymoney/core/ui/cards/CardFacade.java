package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.util.I18N;

/**
 * @author dennis
 */
public class CardFacade {
    public static final String ARG_NAV_PAGES_LIST = "list";
    public static final String ARG_SHOW_TITLE = "show_title";

    ContextsActivity activity;

    public CardFacade(ContextsActivity activity) {
        this.activity = activity;
    }

    public Fragment newFragement(int cardsPos, int pos, Card card) {
        switch (card.getType()) {
            case NAV_PAGES:
                return newNavPagesFragment(cardsPos, pos, card);
            case INFO_EXPENSE:
                return newInfoExpenseFragment(cardsPos, pos, card);
        }
        throw new IllegalStateException("no such card fragment "+card.getType());
    }

    private Bundle newBaseBundle(int cardsPos, int pos){
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
        I18N i18n = Contexts.instance().getI18n();
        switch (type) {
            case NAV_PAGES:
                return i18n.string(R.string.card_nav_page);
            case INFO_EXPENSE:
                return i18n.string(R.string.card_info_expense);
        }
        throw new IllegalStateException("no such card type "+type);
    }

    public boolean isTypeEditable(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return true;
            case INFO_EXPENSE:
                return false;
        }
        throw new IllegalStateException("no such card type "+type);
    }
}
