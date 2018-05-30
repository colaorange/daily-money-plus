package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartLineAccountTypeFragment;
import com.colaorange.dailymoney.core.ui.chart.ChartPieAccountTypeFragment;

/**
 * @author dennis
 */
public class CardLineAccountTypeFragment  extends CardBaseFragment {

    ChartBaseFragment frag;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_line_account_type_frag;
    }

    @Override
    protected boolean doReloadContent() {

        if (frag == null) {
            frag = new ChartLineAccountTypeFragment();
            Bundle arg = (Bundle) getArguments().clone();
            arg.putBoolean(ChartPieAccountTypeFragment.ARG_TITLE_PADDING, !showTitle);
            frag.setArguments(arg);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.card_content, frag)
                    .commit();
        } else {
            frag.reloadChart();
        }
        return true;
    }

}