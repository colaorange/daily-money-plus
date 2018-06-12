package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.ui.helper.PeriodModePagerAdapter;
import com.colaorange.dailymoney.core.ui.legacy.BalanceMgntActivity;
import com.colaorange.dailymoney.core.ui.legacy.BalanceMgntFragment;

import java.util.Date;

/**
 * Created by Dennis
 */
public abstract class PeriodModeChartBaseActivity extends ContextsActivity {

    public static final String ARG_TITLE = "title";
    public static final String ARG_PERIOD_MODE = "periodMode";
    public static final String ARG_BASE_DATE = "baseDate";
    public static final String ARG_FROM_BEGINNING = "fromBeginning";

    protected PeriodMode periodMode;
    protected Date baseDate;
    protected boolean fromBeginning;

    private ViewPager vPager;
    private ChartPagerAdapter adapter;


    protected abstract PeriodModeChartBaseFragment newChartFragment(Date targetDate);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_base);
        initArgs();
        initMembers();
    }

    protected void initArgs() {
        Bundle args = getIntentExtras();

        periodMode = (PeriodMode) args.getSerializable(ARG_PERIOD_MODE);
        if (periodMode == null) {
            periodMode = PeriodMode.MONTHLY;
        }

        baseDate = (Date) args.getSerializable(ARG_BASE_DATE);
        if (baseDate == null) {
            baseDate = new Date();
        }

        fromBeginning = args.getBoolean(ARG_FROM_BEGINNING, false);


        String title = args.getString(ARG_TITLE);
        if (title != null) {
            setTitle(title);
        }
    }

    protected void initMembers() {
        vPager = findViewById(R.id.viewpager);
        adapter = new ChartPagerAdapter(getSupportFragmentManager(), baseDate, periodMode);
        vPager.setAdapter(adapter);
        vPager.setCurrentItem(adapter.getBasePos());

    }


    public class ChartPagerAdapter extends PeriodModePagerAdapter {

        public ChartPagerAdapter(FragmentManager fm, Date baseDate, PeriodMode periodMode) {
            super(fm, baseDate, periodMode);
        }

        @Override
        protected Fragment getItem(int position, Date targetDate) {
            return newChartFragment(targetDate);
        }
    }

}
