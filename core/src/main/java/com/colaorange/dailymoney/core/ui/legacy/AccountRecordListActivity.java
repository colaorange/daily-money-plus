package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.context.RecordTemplate;
import com.colaorange.dailymoney.core.context.RecordTemplateCollection;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.QEvents;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dennis
 */
public class AccountRecordListActivity extends ContextsActivity implements EventQueue.EventListener {

    public static final String ARG_START = "start";
    public static final String ARG_END = "end";

    public static final int MODE_MONTH = RecordMgntFragment.MODE_MONTH;
    public static final int MODE_YEAR = RecordMgntFragment.MODE_YEAR;
    public static final int MODE_ALL = RecordMgntFragment.MODE_ALL;

    public static final String ARG_MODE = "mode";
    /**
     * accept AccountType, Account or the Account id path
     */
    public static final String ARG_CONDITION = "condition";
    public static final String ARG_CONDITION_INFO = "info";

    private TextView infoView;

    I18N i18n;

    private Date startDate;
    private Date endDate;
    private String info;
    private Serializable condition;

    private ActionMode actionMode;
    private Record actionObj;

    //there is only one, so set is to 0
    private int pos = 0;
    private int mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_record_list);
        initArgs();
        initMembers();

        reloadData();
    }


    private void initArgs() {
        i18n = Contexts.instance().getI18n();

        Bundle b = getIntentExtras();
        startDate = (Date) b.get(ARG_START);
        endDate = (Date) b.get(ARG_END);
        condition = b.getSerializable(ARG_CONDITION);
        mode = b.getInt(ARG_MODE, MODE_MONTH);

        String title = b.getString(ARG_CONDITION_INFO);
        if(title!=null) {
            setTitle(title);
        }

        DateFormat format = preference().getDateFormat();
        String fromStr = startDate == null ? "" : format.format(startDate);
        String toStr = endDate == null ? "" : format.format(endDate);

        info = i18n.string(R.string.label_acc_reclist_dateinfo, fromStr, toStr);

        //TODO
        if (condition instanceof AccountType) {
        } else if (condition instanceof Account) {
        } else if (condition instanceof String) {
        } else {
            Logger.w("unsupported condition {}", condition);
        }

    }


    private void initMembers() {

        infoView = findViewById(R.id.record_info);


        FragmentManager fragmentManager = getSupportFragmentManager();
        //clear frag before add frag, it might be android's bug
        String fragTag = getClass().getName() + ":" + pos;
        Fragment f;
        if ((f = fragmentManager.findFragmentByTag(fragTag)) != null) {
            //very strange, why a fragment is here already in create/or create again?
            //I need to read more document
        } else {

            f = new RecordListFragment();
            Bundle b = new Bundle();
            b.putInt(RecordListFragment.ARG_POS, pos);
            b.putInt(RecordListFragment.ARG_MODE, mode);
            f.setArguments(b);

            fragmentManager.beginTransaction()
                    .add(R.id.frag_container, f, fragTag)
                    .disallowAddToBackStack()
                    .commit();
        }


    }

    private void doSelectRecord(Record record) {
        if (record == null && actionMode != null) {
            actionMode.finish();
            return;
        }

        if (record != null) {
            actionObj = record;
            if (actionMode == null) {
                actionMode = this.startSupportActionMode(new RecordActionModeCallback());
            } else {
                actionMode.invalidate();
            }
            actionMode.setTitle(Contexts.instance().toFormattedMoneyString(record.getMoney()));
        }

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
            case QEvents.RecordListFrag.ON_SELECT_RECORD:
                doSelectRecord((Record) event.getData());
                break;
            case QEvents.RecordListFrag.ON_RESELECT_RECORD:
                doEditRecord((Record) event.getData());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_RECORD_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    //user might add record, reload it.
                    reloadData();

                    //refresh action mode
                    if (actionMode != null) {
                        actionObj = contexts().getDataProvider().findRecord(actionObj.getId());
                        if (actionObj == null) {
                            actionMode.finish();
                        } else {
                            actionMode.setTitle(Contexts.instance().toFormattedMoneyString(actionObj.getMoney()));
                        }
                    }

                    //mark ok for changed
                    setResult(RESULT_OK);
                }
            });
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void reloadData() {
        infoView.setText(info);
        final IDataProvider idp = contexts().getDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @SuppressWarnings("unchecked")
            List<Record> data = Collections.EMPTY_LIST;
            int count = 0;

            @Override
            public void run() {
                if (condition instanceof Account) {
                    data = idp.listRecord((Account) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((Account) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate);
                } else if (condition instanceof AccountType) {
                    data = idp.listRecord((AccountType) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((AccountType) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate);
                } else if (condition instanceof String) {
                    data = idp.listRecord((String) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((String) condition, IDataProvider.LIST_RECORD_MODE_BOTH, startDate, endDate);
                } else {
                    data = Collections.emptyList();
                    count = 0;
                }
            }

            @Override
            public void onBusyFinish() {
                lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(data).withArg(RecordListFragment.ARG_POS, pos).build());

                infoView.setText(info + i18n.string(R.string.label_count, count));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_record_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            doNewRecord();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doNewRecord() {
        Intent intent = null;
        intent = new Intent(this, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }


    private void doEditRecord(Record record) {
        Intent intent = null;
        intent = new Intent(this, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, false);
        intent.putExtra(RecordEditorActivity.ARG_RECORD, record);
        startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }

    public void doDeleteRecord(final Record record) {
        GUIs.confirm(this, i18n.string(R.string.qmsg_delete_record, Contexts.instance().toFormattedMoneyString(record.getMoney())), new GUIs.OnFinishListener() {
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {
                    boolean r = Contexts.instance().getDataProvider().deleteRecord(record.getId());
                    if (r) {
                        if (record.equals(actionObj)) {
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }
                        GUIs.shortToast(AccountRecordListActivity.this, i18n.string(R.string.msg_record_deleted));
                        reloadData();
                        trackEvent(TE.DELETE_RECORD);

                        //mark ok for changed
                        setResult(RESULT_OK);
                    }
                }
                return true;
            }
        });


    }

    private void doSetTemplate(final Record actionObj) {
        ;
        List<String> items = new LinkedList<>();
        RecordTemplateCollection col = preference().getRecordTemplates();
        String nodata = i18n.string(R.string.msg_no_data);
        for (int i = 0; i < col.size(); i++) {
            RecordTemplate t = col.getTemplateIfAny(i);
            items.add((i + 1) + ". " + (t == null ? nodata : (t.toString(i18n))));
        }

        new AlertDialog.Builder(this).setTitle(i18n.string(R.string.qmsg_set_tempalte))
                .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {

                        RecordTemplateCollection col = preference().getRecordTemplates();
                        col.setTemplate(which, actionObj.getFrom(), actionObj.getTo(), actionObj.getNote());
                        preference().updateRecordTemplates(col);
                        trackEvent(TE.SET_TEMPLATE + which);
                    }
                }).show();
    }


    public void doCopyRecord(final Record record) {
        Intent intent = null;
        intent = new Intent(this, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        intent.putExtra(RecordEditorActivity.ARG_RECORD, record);
        startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }

    private class RecordActionModeCallback implements ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.record_mgnt_item_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels
            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_edit) {
                doEditRecord(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                doDeleteRecord(actionObj);
                mode.finish();//Finish action mode
                return true;
            } else if (item.getItemId() == R.id.menu_copy) {
                doCopyRecord(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_set_template) {
                doSetTemplate(actionObj);
            }
            return false;
        }

        //onDestroyActionMode(ActionMode) when the action mode is closed.
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            actionMode = null;
            actionObj = null;
            publishClearSelection();
        }
    }

    private void publishClearSelection() {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_CLEAR_SELECTION).build());
    }
}
