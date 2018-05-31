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

    public static final String ARG_CHART_SERIES_MODE = "series_mode";


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
        }
        throw new IllegalStateException("unknown card fragment " + card.getType());
    }

    private Fragment newChartMonthlyExpenseLineFragment(int desktopIndex, int pos, Card card, boolean cumulative) {
        CardLineAccountTypeFragment f = new CardLineAccountTypeFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_MODE, ChartLineAccountTypeFragment.Mode.MONTHLY);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartLineAccountTypeFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_CALCULATION_MODE, cumulative ? ChartLineAccountTypeFragment.CalculationMode.CUMULATIVE : ChartLineAccountTypeFragment.CalculationMode.INDIVIDUAL);

        try{
            ChartLineAccountTypeFragment.SeriesMode seriesMode;
            seriesMode = ChartLineAccountTypeFragment.SeriesMode.valueOf((String)card.getArg(ARG_CHART_SERIES_MODE));
            b.putSerializable(ChartLineAccountTypeFragment.ARG_SERIES_MODE,seriesMode);
        }catch(Exception x){
            //use default
        }

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
                doEditMonthlyExpenseLineArgs(desktopIndex, pos, card, listener);
                break;
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                doEditMonthlyExpenseLineArgs(desktopIndex, pos, card, listener);
                break;
            case INFO_EXPENSE:
            default:
                return;
        }
    }

    private void doEditMonthlyExpenseLineArgs(final int desktopIndex, final int pos, Card card, final OnOKListener listener) {
        List<ChartLineAccountTypeFragment.SeriesMode> values = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        Set<ChartLineAccountTypeFragment.SeriesMode> selection = new LinkedHashSet<>();

        NavPageFacade pgFacade = new NavPageFacade(activity);

        for (ChartLineAccountTypeFragment.SeriesMode item : ChartLineAccountTypeFragment.SeriesMode.values()) {
            values.add(item);
        }
        for (ChartLineAccountTypeFragment.SeriesMode item : values) {
            switch(item){
                case INDIVIDUAL:
                    labels.add(i18n.string(R.string.chart_line_account_type_series_individual));
                    break;
                case INTEGRATED:
                    labels.add(i18n.string(R.string.chart_line_account_type_series_integrated));
                    break;
            }

        }
        try {
            selection.add(ChartLineAccountTypeFragment.SeriesMode.valueOf((String)card.getArg(ARG_CHART_SERIES_MODE)));
        } catch (Exception x) {
            selection.add(ChartLineAccountTypeFragment.SeriesMode.INTEGRATED);
        }

        Dialogs.showSelectionList(activity, i18n.string(R.string.act_edit_args),
                i18n.string(R.string.msg_edit_chart_monthly_expense_args), (List) values, labels, false,
                (Set) selection, new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (Dialogs.OK_BUTTON == which) {
                            Preference preference = Contexts.instance().getPreference();

                            Set<ChartLineAccountTypeFragment.SeriesMode> selection = (Set<ChartLineAccountTypeFragment.SeriesMode>) data;
                            if(selection.size()>0) {
                                CardDesktop desktop = preference.getDesktop(desktopIndex);
                                Card card = desktop.get(pos);
                                card.withArg(ARG_CHART_SERIES_MODE, selection.iterator().next());
                                preference.updateDesktop(desktopIndex, desktop);
                                listener.onOK(card);
                            }
                        }
                        return true;
                    }
                });
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
                    try {
                        selection.add(NavPage.valueOf(s));
                    }catch (Exception x){}
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
        String item;
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
            case CHART_MONTHLY_EXPENSE_LINE_CUMULATIVE:
                ChartLineAccountTypeFragment.SeriesMode mode;
                try {
                    sb.append(" : ");
                    mode = ChartLineAccountTypeFragment.SeriesMode.valueOf((String) card.getArg(ARG_CHART_SERIES_MODE));
                }catch(Exception x){
                    mode = ChartLineAccountTypeFragment.SeriesMode.INTEGRATED;
                }
                switch(mode){
                    case INDIVIDUAL:
                        sb.append(i18n.string(R.string.chart_line_account_type_series_individual));
                        break;
                    case INTEGRATED:
                        sb.append(i18n.string(R.string.chart_line_account_type_series_integrated));
                        break;
                }
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
