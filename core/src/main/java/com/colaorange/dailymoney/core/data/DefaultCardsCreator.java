package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.ui.cards.CardFacade;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * Created by Dennis
 */
public class DefaultCardsCreator {

    public DefaultCardsCreator() {
    }

    public void create() {
        Contexts ctx = Contexts.instance();
        I18N i18n = ctx.getI18n();
        Preference preference = ctx.getPreference();
        CardCollection cards = preference.getCards(0);
        if (cards.size() != 0) {
            //ignore it
            Logger.w("cards is not empty, ignore it");
            return;
        }
        cards.setTitle("Test 1");
        Card card = new Card(CardType.NAV_PAGES, "card 1");
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(NavPage.RECORD_EDITOR,
                NavPage.DAILY_LIST, NavPage.MONTHLY_LIST, NavPage.MONTHLY_BALANCE, NavPage.CUMULATIVE_BALANCE));
        cards.add(card);

        card = new Card(CardType.INFO_EXPENSE, "card 2");
        cards.add(card);

        preference.updateCards(0, cards);

        cards = preference.getCards(1);

        cards.setTitle("Test 2");
        card = new Card(CardType.INFO_EXPENSE, "card 4");
        cards.add(card);

        card = new Card(CardType.NAV_PAGES, "card 3");
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(NavPage.MONTHLY_BALANCE, NavPage.CUMULATIVE_BALANCE));
        cards.add(card);


        preference.updateCards(1, cards);

    }
}
