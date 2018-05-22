package com.colaorange.dailymoney.core.ui.cards;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.context.Card;
import com.colaorange.dailymoney.core.context.CardCollection;
import com.colaorange.dailymoney.core.context.CardType;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * Created by Dennis
 */
public class DefaultCardsHelper {
    ContextsActivity activity;

    public DefaultCardsHelper(ContextsActivity activity) {
        this.activity = activity;
    }

    public void createDefaultCards() {
        Contexts ctx = Contexts.instance();
        Preference preference = ctx.getPreference();
        CardCollection cards = preference.getCards(0);
        if (cards.size() != 0) {
            //ignore it
            Logger.w("cards is not empty, ignore it");
            return;
        }
        Card card = new Card(CardType.NAV_PAGES);
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(NavPage.RECORD_EDITOR,
                NavPage.DAILY_LIST, NavPage.MONTHLY_LIST, NavPage.MONTHLY_BALANCE, NavPage.CUMULATIVE_BALANCE));
        cards.add(card);

        card = new Card(CardType.INFO_EXPENSE);
        card.withArg(CardFacade.ARG_INFO_EXPENSE_CASH, true);
        card.withArg(CardFacade.ARG_INFO_EXPENSE_WEEKLY_EXPENSE, true);
        card.withArg(CardFacade.ARG_INFO_EXPENSE_MONTHLY_EXPENSE, true);
        cards.add(card);

        preference.updateCards(0, cards);
    }
}
