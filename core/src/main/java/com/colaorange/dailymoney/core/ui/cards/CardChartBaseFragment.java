package com.colaorange.dailymoney.core.ui.cards;

import android.os.Bundle;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.ui.chart.ChartBaseFragment;
import com.colaorange.dailymoney.core.ui.chart.PieAccountFragment;

/**
 * @author dennis
 */
public abstract class CardChartBaseFragment extends CardBaseFragment {

    ChartBaseFragment frag;

    @Override
    protected int getLayoutResId() {
        return R.layout.card_chart_base_frag;
    }

    @Override
    protected boolean doReloadContent() {

        if (frag == null) {
            frag = newFragment();
            Bundle arg = (Bundle) getArguments().clone();
            arg.putBoolean(PieAccountFragment.ARG_TITLE_PADDING, !showTitle);
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

    abstract protected ChartBaseFragment newFragment();

}