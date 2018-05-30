package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Card;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.CardDesktop;
import com.colaorange.dailymoney.core.data.CardType;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountTypeFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartPieAccountTypeFragment;
import com.colaorange.dailymoney.core.ui.nav.NavPage;
import com.colaorange.dailymoney.core.ui.nav.NavPageFacade;
import com.colaorange.dailymoney.core.util.Dialogs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.Date;
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

    public Fragment newFragement(int desktopIndex, int pos, Card card) {
        switch (card.getType()) {
            case NAV_PAGES:
                return newNavPagesFragment(desktopIndex, pos, card);
            case INFO_EXPENSE:
                return newInfoExpenseFragment(desktopIndex, pos, card);
            case CHART_WEEKLY_EXPENSE_PIE:
                return newChartWeeklyExpensePieFragment(desktopIndex, pos, card);
            case CHART_MONTHLY_EXPENSE_PIE:
                return newChartMonthlyExpensePieFragment(desktopIndex, pos, card);
            case CHART_MONTHLY_EXPENSE_LINE:
                return newChartMonthlyExpenseLineFragment(desktopIndex, pos, card, false);
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                return newChartMonthlyExpenseLineFragment(desktopIndex, pos, card, true);
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                break;
        }
        throw new IllegalStateException("unknown card fragment " + card.getType());
    }

    private Fragment newChartMonthlyExpenseLineFragment(int desktopIndex, int pos, Card card, boolean cumulative) {
        CardLineAccountTypeFragment f = new CardLineAccountTypeFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_MODE, ChartLineAccountTypeFragment.Mode.MONTHLY);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartLineAccountTypeFragment.ARG_CALCULATION_MODE, cumulative ? ChartLineAccountTypeFragment.CalculationMode.CUMULATIVE : ChartLineAccountTypeFragment.CalculationMode.NORMAL);
        f.setArguments(b);
        return f;
    }

    private Fragment newChartMonthlyExpensePieFragment(int desktopIndex, int pos, Card card) {
        CardPieAccountTypeFragment f = new CardPieAccountTypeFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartPieAccountTypeFragment.ARG_MODE, ChartPieAccountTypeFragment.Mode.MONTHLY);
        b.putSerializable(ChartPieAccountTypeFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartPieAccountTypeFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        f.setArguments(b);
        return f;
    }

    private Fragment newChartWeeklyExpensePieFragment(int desktopIndex, int pos, Card card) {
        CardPieAccountTypeFragment f = new CardPieAccountTypeFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartPieAccountTypeFragment.ARG_MODE, ChartPieAccountTypeFragment.Mode.WEEKLY);
        b.putSerializable(ChartPieAccountTypeFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartPieAccountTypeFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        f.setArguments(b);
        return f;
    }


    private Bundle newBaseBundle(int desktopIndex, int pos) {
        Bundle b = new Bundle();
        b.putSerializable(InfoExpenseFragment.ARG_DESKTOP_INDEX, desktopIndex);
        b.putSerializable(InfoExpenseFragment.ARG_INDEX, pos);
        return b;
    }

    private Fragment newInfoExpenseFragment(int desktopIndex, int pos, Card card) {
        InfoExpenseFragment f = new InfoExpenseFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        f.setArguments(b);
        return f;
    }

    private Fragment newNavPagesFragment(int desktopIndex, int pos, Card card) {
        NavPagesFragment f = new NavPagesFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        f.setArguments(b);
        return f;
    }

    public String getTypeText(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return i18n.string(R.string.card_nav_page);
            case INFO_EXPENSE:
                return i18n.string(R.string.card_info_expense);
            case CHART_WEEKLY_EXPENSE_PIE:
                return i18n.string(R.string.card_chart_weekly_expense_pie);
            case CHART_MONTHLY_EXPENSE_PIE:
                return i18n.string(R.string.card_chart_monthly_expense_pie);
            case CHART_MONTHLY_EXPENSE_LINE:
                return i18n.string(R.string.card_chart_monthly_expense_line);
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                return i18n.string(R.string.card_chart_monthly_expense_line_cumulative);
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                return i18n.string(R.string.card_chart_yearly_income_expense_line);
        }
        return i18n.string(R.string.label_unknown);
    }

    public int getTypeIcon(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return activity.resolveThemeAttrResId(R.attr.ic_nav_page);
            case INFO_EXPENSE:
                return activity.resolveThemeAttrResId(R.attr.ic_info);
            case CHART_WEEKLY_EXPENSE_PIE:
                return activity.resolveThemeAttrResId(R.attr.ic_pie_chart);
            case CHART_MONTHLY_EXPENSE_PIE:
                return activity.resolveThemeAttrResId(R.attr.ic_pie_chart);
            case CHART_MONTHLY_EXPENSE_LINE:
                return activity.resolveThemeAttrResId(R.attr.ic_series_chart);
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                return activity.resolveThemeAttrResId(R.attr.ic_series_chart);
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                return activity.resolveThemeAttrResId(R.attr.ic_series_chart);
        }
        return -1;
    }

    public boolean isTypeEditable(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return true;
            case INFO_EXPENSE:
                return false;
            case CHART_WEEKLY_EXPENSE_PIE:
                return false;
            case CHART_MONTHLY_EXPENSE_PIE:
                return false;
            case CHART_MONTHLY_EXPENSE_LINE:
                return true;
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                return true;
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                return true;
        }
        return false;
    }

    public void doEditArgs(int desktopIndex, int pos, Card card, OnOKListener listener) {
        switch (card.getType()) {
            case NAV_PAGES:
                doEditNavPagesArgs(desktopIndex, pos, card, listener);
                return;
            case CHART_WEEKLY_EXPENSE_PIE:
                break;
            case CHART_MONTHLY_EXPENSE_PIE:
                break;
            case CHART_MONTHLY_EXPENSE_LINE:
                break;
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                break;
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                break;
            case INFO_EXPENSE:
            default:
                return;
        }
    }

    private void doEditNavPagesArgs(final int desktopIndex, final int pos, Card card, final OnOKListener listener) {
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

                            Set<NavPage> selection = (Set<NavPage>) data;
                            CardDesktop desktop = preference.getDesktop(desktopIndex);
                            Card card = desktop.get(pos);
                            card.withArg(ARG_NAV_PAGES_LIST, selection);
                            Logger.d(">>> new nav_page selection {}", selection);
                            preference.updateDesktop(desktopIndex, desktop);

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
                } else {
                    sb.append(i18n.string(R.string.msg_no_data));
                }
                break;
            case CHART_WEEKLY_EXPENSE_PIE:
                break;
            case CHART_MONTHLY_EXPENSE_PIE:
                break;
            case CHART_MONTHLY_EXPENSE_LINE:
                break;
            case CHART_YEARLY_INCOME_EXPENSE_LINE:
                break;
            case INFO_EXPENSE:
            default:
                break;
        }

        return sb.toString();
    }

    public List<CardType> listAvailableType() {
        return Collections.asList(CardType.NAV_PAGES,
                CardType.INFO_EXPENSE,
                CardType.CHART_WEEKLY_EXPENSE_PIE,
                CardType.CHART_MONTHLY_EXPENSE_PIE,
                CardType.CHART_MONTHLY_EXPENSE_LINE,
                CardType.CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE);
    }

    public interface OnOKListener {
        void onOK(Card card);
    }
}
