package com.colaorange.dailymoney.core.ui.chart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;

/**
 * Created by Dennis
 */
public abstract class ChartBaseActivity extends ContextsActivity {

    public static final String ARG_TITLE = "title";

    View vChartContent;

    protected abstract ChartBaseFragment newChartFragment();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_base);
        initArgs();
        initMembers();
        reloadContent();
    }

    protected void initArgs() {
        Bundle args = getIntentExtras();
        String title = args.getString(ARG_TITLE);
        if (title != null) {
            setTitle(title);
        }
    }

    protected void initMembers() {
        vChartContent = findViewById(R.id.chart_content);
    }

    protected void reloadContent() {
        FragmentManager fm = getSupportFragmentManager();
        String fragTag = "chart:" + vChartContent.getId();
        if (fm.findFragmentByTag(fragTag) == null) {
            fm.beginTransaction()
                    .add(R.id.chart_content, newChartFragment(), fragTag)
                    .commit();
        }
    }


}
