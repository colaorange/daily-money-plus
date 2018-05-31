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
import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountTypeFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartPieAccountFragment;
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

    public static final String ARG_CHART_CALCULATION_MODE = "calculation_mode";


    ContextsActivity activity;
    I18N i18n;

    public CardFacade(ContextsActivity activity) {
        this.activity = activity;
        i18n = Contexts.instance().getI18n();
    }

    public Fragment newFragment(int desktopIndex, int pos, Card card) {

        CardType type;
        try {
            type = card.getTypeEnum();
        } catch (Exception x) {
            return newUnknow(desktopIndex, pos, card);
        }
        switch (type) {
            case NAV_PAGES:
                return newNavPages(desktopIndex, pos, card);
            case INFO_EXPENSE:
                return newInfoExpense(desktopIndex, pos, card);
            case PIE_WEEKLY_EXPENSE:
                return newPieWeeklyExpense(desktopIndex, pos, card);
            case PIE_MONTHLY_EXPENSE:
                return newPieMonthlyExpense(desktopIndex, pos, card);
            case LINE_MONTHLY_EXPENSE:
                return newLineMonthlyExpense(desktopIndex, pos, card);
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
                return newLineMonthlyExpenseAggregate(desktopIndex, pos, card);
        }
        return newUnknow(desktopIndex, pos, card);
    }

    private Fragment newLineMonthlyExpense(int desktopIndex, int pos, Card card) {
        LineAccountFragment f = new LineAccountFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartLineAccountFragment.ARG_PERIOD_MODE, ChartBaseFragment.PeriodMode.MONTHLY);
        b.putSerializable(ChartLineAccountFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartLineAccountFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        try {
            ChartBaseFragment.CalculationMode calMode;
            calMode = ChartBaseFragment.CalculationMode.valueOf((String) card.getArg(ARG_CHART_CALCULATION_MODE));
            b.putSerializable(ChartLineAccountFragment.ARG_CALCULATION_MODE, calMode);
        } catch (Exception x) {
            //use default
        }

        f.setArguments(b);
        return f;
    }

    private Fragment newLineMonthlyExpenseAggregate(int desktopIndex, int pos, Card card) {
        LineAccountTypeFragment f = new LineAccountTypeFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_PERIOD_MODE, ChartBaseFragment.PeriodMode.MONTHLY);
        b.putSerializable(ChartLineAccountTypeFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartLineAccountTypeFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        try {
            ChartBaseFragment.CalculationMode calMode;
            calMode = ChartBaseFragment.CalculationMode.valueOf((String) card.getArg(ARG_CHART_CALCULATION_MODE));
            b.putSerializable(ChartLineAccountTypeFragment.ARG_CALCULATION_MODE, calMode);
        } catch (Exception x) {
            //use default
        }

        f.setArguments(b);
        return f;
    }

    private Fragment newPieMonthlyExpense(int desktopIndex, int pos, Card card) {
        PieAccountFragment f = new PieAccountFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartPieAccountFragment.ARG_PERIOD_MODE, ChartBaseFragment.PeriodMode.MONTHLY);
        b.putSerializable(ChartPieAccountFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartPieAccountFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        f.setArguments(b);
        return f;
    }

    private Fragment newPieWeeklyExpense(int desktopIndex, int pos, Card card) {
        PieAccountFragment f = new PieAccountFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        b.putSerializable(ChartPieAccountFragment.ARG_PERIOD_MODE, ChartBaseFragment.PeriodMode.WEEKLY);
        b.putSerializable(ChartPieAccountFragment.ARG_BASE_DATE, new Date());
        b.putSerializable(ChartPieAccountFragment.ARG_ACCOUNT_TYPE, AccountType.EXPENSE);
        f.setArguments(b);
        return f;
    }


    private Bundle newBaseBundle(int desktopIndex, int pos) {
        Bundle b = new Bundle();
        b.putSerializable(InfoExpenseFragment.ARG_DESKTOP_INDEX, desktopIndex);
        b.putSerializable(InfoExpenseFragment.ARG_INDEX, pos);
        return b;
    }

    private Fragment newInfoExpense(int desktopIndex, int pos, Card card) {
        InfoExpenseFragment f = new InfoExpenseFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        f.setArguments(b);
        return f;
    }

    private Fragment newUnknow(int desktopIndex, int pos, Card card) {
        UnknowFragment f = new UnknowFragment();
        Bundle b = newBaseBundle(desktopIndex, pos);
        f.setArguments(b);
        return f;
    }

    private Fragment newNavPages(int desktopIndex, int pos, Card card) {
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
            case PIE_WEEKLY_EXPENSE:
                return i18n.string(R.string.card_pie_weekly_expense);
            case PIE_MONTHLY_EXPENSE:
                return i18n.string(R.string.card_pie_monthly_expense);
            case LINE_MONTHLY_EXPENSE:
                return i18n.string(R.string.card_line_monthly_expense);
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
                return i18n.string(R.string.card_line_monthly_expense_aggregate);
        }
        return i18n.string(R.string.label_unknown);
    }

    public int getTypeIcon(CardType type) {
        switch (type) {
            case NAV_PAGES:
                return activity.resolveThemeAttrResId(R.attr.ic_nav_page);
            case INFO_EXPENSE:
                return activity.resolveThemeAttrResId(R.attr.ic_info);
            case PIE_WEEKLY_EXPENSE:
                return activity.resolveThemeAttrResId(R.attr.ic_pie_chart);
            case PIE_MONTHLY_EXPENSE:
                return activity.resolveThemeAttrResId(R.attr.ic_pie_chart);
            case LINE_MONTHLY_EXPENSE:
                return activity.resolveThemeAttrResId(R.attr.ic_series_chart);
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
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
            case PIE_WEEKLY_EXPENSE:
                return false;
            case PIE_MONTHLY_EXPENSE:
                return false;
            case LINE_MONTHLY_EXPENSE:
                return true;
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
                return true;
        }
        return false;
    }

    public void doEditArgs(int desktopIndex, int pos, Card card, OnOKListener listener) {
        CardType type;
        try {
            type = card.getTypeEnum();
        } catch (Exception x) {
            return;
        }
        switch (type) {
            case NAV_PAGES:
                doEditNavPagesArgs(desktopIndex, pos, card, listener);
                return;
            case PIE_WEEKLY_EXPENSE:
                break;
            case PIE_MONTHLY_EXPENSE:
                break;
            case LINE_MONTHLY_EXPENSE:
                doEditLineMonthlyExpenseArgs(desktopIndex, pos, card, listener);
                break;
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
                doEditLineMonthlyExpenseArgs(desktopIndex, pos, card, listener);
                break;
            case INFO_EXPENSE:
            default:
                return;
        }
    }

    private void doEditLineMonthlyExpenseArgs(final int desktopIndex, final int pos, Card card, final OnOKListener listener) {
        List<ChartBaseFragment.CalculationMode> values = new LinkedList<>();
        List<String> labels = new LinkedList<>();
        Set<ChartBaseFragment.CalculationMode> selection = new LinkedHashSet<>();

        NavPageFacade pgFacade = new NavPageFacade(activity);

        for (ChartBaseFragment.CalculationMode item : ChartBaseFragment.CalculationMode.values()) {
            values.add(item);
        }
        for (ChartBaseFragment.CalculationMode item : values) {
            switch (item) {
                case INDIVIDUAL:
                    labels.add(i18n.string(R.string.chart_arg_calculation_mode_individual));
                    break;
                case CUMULATIVE:
                    labels.add(i18n.string(R.string.chart_arg_calculation_mode_cumulative));
                    break;
            }

        }
        try {
            selection.add(ChartBaseFragment.CalculationMode.valueOf((String) card.getArg(ARG_CHART_CALCULATION_MODE)));
        } catch (Exception x) {
            selection.add(ChartBaseFragment.CalculationMode.CUMULATIVE);
        }

        Dialogs.showSelectionList(activity, i18n.string(R.string.act_edit_args),
                i18n.string(R.string.msg_edit_line_monthly_expense_args), (List) values, labels, false,
                (Set) selection, new Dialogs.OnFinishListener() {
                    @Override
                    public boolean onFinish(int which, Object data) {
                        if (Dialogs.OK_BUTTON == which) {
                            Preference preference = Contexts.instance().getPreference();

                            Set<ChartBaseFragment.CalculationMode> selection = (Set<ChartBaseFragment.CalculationMode>) data;
                            if (selection.size() > 0) {
                                CardDesktop desktop = preference.getDesktop(desktopIndex);
                                Card card = desktop.get(pos);
                                card.withArg(ARG_CHART_CALCULATION_MODE, selection.iterator().next());
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
                    } catch (Exception x) {
                    }
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
        CardType type;
        try {
            type = card.getTypeEnum();
        } catch (Exception x) {
            return  "";
        }

        StringBuilder sb = new StringBuilder(getTypeText(type));

        List list;
        String item;
        switch (type) {
            case NAV_PAGES:
                sb.append(" : ");
                list = card.getArg(ARG_NAV_PAGES_LIST);
                if (list != null && list.size() > 0) {
                    sb.append(i18n.string(R.string.msg_n_items, list.size()));
                } else {
                    sb.append(i18n.string(R.string.msg_no_data));
                }
                break;
            case PIE_WEEKLY_EXPENSE:
                break;
            case PIE_MONTHLY_EXPENSE:
                break;
            case LINE_MONTHLY_EXPENSE:
            case LINE_MONTHLY_EXPENSE_AGGREGATE:
                ChartBaseFragment.CalculationMode mode;
                try {
                    sb.append(" : ");
                    mode = ChartBaseFragment.CalculationMode.valueOf((String) card.getArg(ARG_CHART_CALCULATION_MODE));
                } catch (Exception x) {
                    mode = ChartBaseFragment.CalculationMode.CUMULATIVE;
                }
                switch (mode) {
                    case INDIVIDUAL:
                        sb.append(i18n.string(R.string.chart_arg_calculation_mode_individual));
                        break;
                    case CUMULATIVE:
                        sb.append(i18n.string(R.string.chart_arg_calculation_mode_cumulative));
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
                CardType.PIE_WEEKLY_EXPENSE,
                CardType.PIE_MONTHLY_EXPENSE,
                CardType.LINE_MONTHLY_EXPENSE,
                CardType.LINE_MONTHLY_EXPENSE_AGGREGATE);
    }

    public interface OnOKListener {
        void onOK(Card card);
    }
}
