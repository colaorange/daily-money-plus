package com.colaorange.dailymoney.core.data;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.R;
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

    public void createForWholeNew(boolean ignoreExist) {
        Logger.i(">>createForWholeNew");
        Contexts ctx = Contexts.instance();
        I18N i18n = ctx.getI18n();
        Preference preference = ctx.getPreference();
        CardCollection cards0 = preference.getCards(0);
        if (cards0.size() != 0) {
            //ignore it
            Logger.w("cards 0 is not empty");
            if (!ignoreExist) {
                return;
            }
        }
        cards0.setTitle(i18n.string(R.string.desktop_main));
        Card card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_nav_page));
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(
                NavPage.HOW2USE,
                NavPage.RECORD_EDITOR,
                NavPage.DAILY_LIST,
                NavPage.MONTHLY_LIST,
                NavPage.MONTHLY_BALANCE,
                NavPage.CUMULATIVE_BALANCE));
        cards0.add(card);

        card = new Card(CardType.INFO_EXPENSE, i18n.string(R.string.card_info_expense));
        cards0.add(card);
        preference.updateCards(0, cards0, true);

        CardCollection cards1 = preference.getCards(1);
        if (cards1.size() != 0) {
            Logger.w("cards 1 is not empty");
            if (!ignoreExist) {
                return;
            }
        }
        cards1.setTitle(i18n.string(R.string.desktop_reports));

        //TODO chart
        card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_chart_monthly_expense));
        card.withArg(CardFacade.ARG_SHOW_TITLE, Boolean.TRUE);
        cards1.add(card);

        //TODO chart
        card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_chart_monthly_expense));
        card.withArg(CardFacade.ARG_SHOW_TITLE, Boolean.TRUE);

        cards1.add(card);


        preference.updateCards(1, cards1, true);

    }

    public void createForUpgrade(boolean existIgnore) {
        Logger.i(">>createForUpgarde");
        Contexts ctx = Contexts.instance();
        I18N i18n = ctx.getI18n();
        Preference preference = ctx.getPreference();
        CardCollection cards0 = preference.getCards(0);
        if (cards0.size() != 0) {
            Logger.w("cards 0 is not empty");
            if (!existIgnore) {
                return;
            }
        }
        cards0.setTitle(i18n.string(R.string.desktop_main));
        Card card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_nav_page));
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(
                NavPage.RECORD_EDITOR,
                NavPage.DAILY_LIST,
                NavPage.WEEKLY_LIST,
                NavPage.MONTHLY_LIST,
                NavPage.YEARLY_LIST,
                NavPage.ACCOUNT_MGNT,
                NavPage.BOOK_MGNT,
                NavPage.DATA_MAIN,
                NavPage.PREFS,
                NavPage.HOW2USE));
        cards0.add(card);

        card = new Card(CardType.INFO_EXPENSE, i18n.string(R.string.card_info_expense));
        cards0.add(card);

        preference.updateCards(0, cards0, true);

        CardCollection cards1 = preference.getCards(1);

        if (cards1.size() != 0) {
            Logger.w("cards 1 is not empty");
            if (!existIgnore) {
                return;
            }
        }
        cards1.setTitle(i18n.string(R.string.desktop_reports));


        card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_nav_page));
        card.withArg(CardFacade.ARG_NAV_PAGES_LIST, Collections.asList(
                NavPage.MONTHLY_BALANCE,
                NavPage.YEARLY_BALANCE,
                NavPage.CUMULATIVE_BALANCE));
        cards1.add(card);


        //TODO chart
        card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_chart_monthly_expense));
        card.withArg(CardFacade.ARG_SHOW_TITLE, Boolean.TRUE);
        cards1.add(card);

        //TODO chart
        card = new Card(CardType.NAV_PAGES, i18n.string(R.string.card_chart_monthly_expense));
        cards1.add(card);

        preference.updateCards(1, cards1, true);
    }
}
