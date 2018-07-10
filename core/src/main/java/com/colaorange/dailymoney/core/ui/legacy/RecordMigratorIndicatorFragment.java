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

    private TextView vStep1Info;
    private ProgressBar vStep1Progress;
    private TextView vStep2Info;
    private ProgressBar vStep2Progress;
    private TextView vStep3Info;
    private ProgressBar vStep3Progress;
    private Button btnMigrate;

    private RecordMigratorActivity.Indicator data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.record_migrator_migrate_frag, container, false);
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

        vStep1Info = rootView.findViewById(R.id.step1_info);
        vStep1Progress = rootView.findViewById(R.id.step1_progress);
        vStep2Info = rootView.findViewById(R.id.step2_info);
        vStep2Progress = rootView.findViewById(R.id.step2_progress);
        vStep3Info = rootView.findViewById(R.id.step3_info);
        vStep3Progress = rootView.findViewById(R.id.step3_progress);

        btnMigrate = rootView.findViewById(R.id.migrate_record);
        btnMigrate.setOnClickListener(this);
    }

    private void reloadData(RecordMigratorActivity.Indicator data) {
        RecordMigratorActivity activity = getContextsActivity();
        I18N i18n = Contexts.instance().getI18n();
        this.data = data;
        String bookName = data.bookName;
        vStep1Info.setText(i18n.string(R.string.msg_record_migrate_step1_info, data.srcRecordListSize, bookName));
        vStep2Info.setText(i18n.string(R.string.msg_record_migrate_step2_info, data.newAccountListSize, bookName));
        vStep3Info.setText(i18n.string(R.string.msg_record_migrate_step3_info, data.updateAccountListSize));

        vStep1Progress.setMax(data.srcRecordListSize);
        vStep1Progress.setProgress(data.srcRecordListProgress);
        vStep2Progress.setMax(data.newAccountListSize);
        vStep2Progress.setProgress(data.newAccountListProgress);
        vStep3Progress.setMax(data.updateAccountListSize);
        vStep3Progress.setProgress(data.updateAccountListProgress);

        btnMigrate.setEnabled(!data.processing);
    }

    @Override
    public void onClick(View v) {
        RecordMigratorActivity activity = getContextsActivity();


        if (v.getId() == R.id.migrate_record) {
            lookupQueue().publish(QEvents.RecordMigratorFrag.ON_MIGRATE);
        }
    }
}
