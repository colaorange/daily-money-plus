package com.colaorange.dailymoney.core.ui.legacy;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsFragment;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.I18N;

/**
 *
 */
public class RecordMigratorIndicatorFragment extends ContextsFragment implements View.OnClickListener, EventQueue.EventListener {

    private View rootView;

    private TextView vNewAccountInfo;
    private ProgressBar vNewAccountProgress;
    private TextView vNewRecordInfo;
    private ProgressBar vNewRecordProgress;
    private TextView vUpdateAccountInfo;
    private ProgressBar vUpdateAccountProgress;
    private Button btnStart;

    private RecordMigratorActivity.Indicator data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_migrator_indicator_frag, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        lookupQueue().subscribe(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lookupQueue().unsubscribe(this);
    }

    @Override
    public void onEvent(EventQueue.Event event) {
        switch (event.getName()) {
            case QEvents.RecordMigratorIndicatorFrag.ON_RELOAD_FRAGMENT:
                reloadData((RecordMigratorActivity.Indicator) event.getData());
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initArgs();
        initMembers();
    }

    @Override
    protected RecordMigratorActivity getContextsActivity() {
        return (RecordMigratorActivity) super.getContextsActivity();
    }

    private void initArgs() {
        Bundle args = getArguments();
    }

    private void initMembers() {

        vNewAccountInfo = rootView.findViewById(R.id.step1_info);
        vNewAccountProgress = rootView.findViewById(R.id.step1_progress);
        vNewRecordInfo = rootView.findViewById(R.id.step2_info);
        vNewRecordProgress = rootView.findViewById(R.id.step2_progress);
        vUpdateAccountInfo = rootView.findViewById(R.id.step3_info);
        vUpdateAccountProgress = rootView.findViewById(R.id.step3_progress);

        btnStart = rootView.findViewById(R.id.start);
        btnStart.setOnClickListener(this);
    }

    private void reloadData(RecordMigratorActivity.Indicator data) {
        RecordMigratorActivity activity = getContextsActivity();
        I18N i18n = Contexts.instance().getI18n();
        this.data = data;
        String bookName = data.destBookName;
        vNewAccountInfo.setText(i18n.string(R.string.msg_record_migrate_new_account_info, data.newAccountSize, data.newAccountProgress, bookName));
        vNewAccountProgress.setMax(data.newAccountSize);
        vNewAccountProgress.setProgress(data.newAccountProgress);

        vNewRecordInfo.setText(i18n.string(R.string.msg_record_migrate_new_record_info, data.newRecordSize, data.newRecordProgress, bookName));
        vNewRecordProgress.setMax(data.newRecordSize);
        vNewRecordProgress.setProgress(data.newRecordProgress);

        vUpdateAccountInfo.setText(i18n.string(R.string.msg_record_migrate_update_account_info, data.updateAccountSize, data.updateAccountProgress));
        vUpdateAccountProgress.setMax(data.updateAccountSize);
        vUpdateAccountProgress.setProgress(data.updateAccountProgress);

        btnStart.setEnabled(!data.processing);
    }

    @Override
    public void onClick(View v) {
        RecordMigratorActivity activity = getContextsActivity();

        if (v.getId() == R.id.start) {
            lookupQueue().publish(QEvents.RecordMigratorFrag.ON_MIGRATE);
        }
    }
}
