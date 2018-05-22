package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.colaorange.dailymoney.core.context.Card;
import com.colaorange.dailymoney.core.context.ContextsActivity;

/**
 * @author dennis
 */
public class CardFacade {
    public static final String ARG_NAV_PAGES_LIST = "list";
    public static final String ARG_INFO_EXPENSE_CASH = "cash";
    public static final String ARG_INFO_EXPENSE_WEEKLY_EXPENSE = "weekly-expense";
    public static final String ARG_INFO_EXPENSE_MONTHLY_EXPENSE = "monthly-expense";

    ContextsActivity activity;

    public CardFacade(ContextsActivity activity){
        this.activity = activity;
    }

    public Fragment newFragement(Card card) {
        CardNavPagesFragment f = new CardNavPagesFragment();
        Bundle b = new Bundle();
        b.putSerializable(CardNavPagesFragment.ARG_CARD, card);
        f.setArguments(b);
        return f;
    }
}
