package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.colaorange.calculator2.Calculator;
import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Formats;
import com.colaorange.commons.util.Strings;
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
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.ui.legacy.AccountUtil.AccountIndentNode;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Edit or create a record
 *
 * @author dennis
 */
public class RecordSearcherActivity extends ContextsActivity implements View.OnClickListener, EventQueue.EventListener {

    private DateFormat dateFormat;

    private boolean archived = false;

    private List<AccountIndentNode> fromAccountList;
    private List<AccountIndentNode> toAccountList;

    private RegularSpinnerAdapter<AccountIndentNode> fromAccountAdapter;
    private RegularSpinnerAdapter<AccountIndentNode> toAccountAdapter;

    private CollapsingToolbarLayout collapsingToolbar;
    private Spinner vFromAccount;
    private Spinner vToAccount;

    private EditText vFromDate;
    private EditText vToDate;
    private EditText vFromMoney;
    private EditText vToMoney;
    private EditText vNote;
    private View vSearchHint;
    private View vResultContainer;
    private RecordListFragment listFragment;

    private int cal2RequestorId;

    private ActionMode actionMode;
    private Record actionObj;

    private int pos = 0;//always 0, single page

    I18N i18n;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_searcher);
        initArgs();
        initMembers();
        refreshSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.record_searcher_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_reset) {
            doReset();
        }
        return super.onOptionsItemSelected(item);
    }


    private void initArgs() {

    }


    private void initMembers() {
        i18n = i18n();
        dateFormat = preference().getDateFormat();

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.appCollapsingToolbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        vFromDate = findViewById(R.id.record_from_date);
        vToDate = findViewById(R.id.record_to_date);


        vFromMoney = findViewById(R.id.record_from_money);
        vToMoney = findViewById(R.id.record_to_money);

        vNote = findViewById(R.id.record_note);

        vSearchHint = findViewById(R.id.search_hint);
        vResultContainer = findViewById(R.id.frag_container);

        findViewById(R.id.btn_from_datepicker).setOnClickListener(this);
        findViewById(R.id.btn_to_datepicker).setOnClickListener(this);
        findViewById(R.id.btn_from_cal2).setOnClickListener(this);
        findViewById(R.id.btn_to_cal2).setOnClickListener(this);
        findViewById(R.id.fab_search).setOnClickListener(this);


        String nullText = " ";

        vFromAccount = findViewById(R.id.record_from_account);
        vFromAccount.setSelection(-1);
        fromAccountList = new LinkedList<>();
        fromAccountAdapter = new AccountIndentNodeSpinnerAdapter(this, fromAccountList, nullText) {
            @Override
            public boolean isSelected(int position) {
                return vFromAccount.getSelectedItemPosition() == position;
            }
        };
        vFromAccount.setAdapter(fromAccountAdapter);

        vToAccount = findViewById(R.id.record_to_account);
        vToAccount.setSelection(-1);
        toAccountList = new LinkedList<>();
        toAccountAdapter = new AccountIndentNodeSpinnerAdapter(this, toAccountList, nullText) {
            @Override
            public boolean isSelected(int position) {
                return vToAccount.getSelectedItemPosition() == position;
            }
        };
        vToAccount.setAdapter(toAccountAdapter);
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

    private void refreshSpinner() {
        IDataProvider idp = contexts().getDataProvider();

        fromAccountList.clear();
        toAccountList.clear();
        fromAccountList.add(null);
        toAccountList.add(null);
        for (AccountType at : AccountType.getSupportedType()) {
            List<AccountIndentNode> list = AccountUtil.toIndentNode(idp.listAccount(at));
            fromAccountList.addAll(list);
            toAccountList.addAll(list);
        }
        fromAccountAdapter.notifyDataSetChanged();
        toAccountAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        CalendarHelper cal = calendarHelper();
        if (id == R.id.btn_from_datepicker || v.getId() == R.id.btn_to_datepicker) {
            Date d = null;
            try {
                d = dateFormat.parse(vFromDate.getText().toString());
            } catch (ParseException e) {
                d = new Date();
            }
            GUIs.openDatePicker(this, d, new GUIs.OnFinishListener() {
                @Override
                public boolean onFinish(int which, Object data) {
                    if (which == GUIs.OK_BUTTON) {
                        if (id == R.id.btn_from_datepicker) {
                            vFromDate.setText(dateFormat.format((Date) data));
                        } else if (id == R.id.btn_to_datepicker) {
                            vToDate.setText(dateFormat.format((Date) data));
                        }
                    }
                    return true;
                }
            });

        } else if (id == R.id.btn_from_cal2 || id == R.id.btn_to_cal2) {
            doCalculator2(id);
        } else if (id == R.id.fab_search) {
            doSearch(true);
        }
    }

    private void doReset() {
        vFromAccount.setSelection(0);
        vToAccount.setSelection(0);
        vFromDate.setText("");
        vToDate.setText("");
        vFromMoney.setText("");
        vToMoney.setText("");
        vNote.setText("");
        vSearchHint.setVisibility(View.VISIBLE);
        vResultContainer.setVisibility(View.GONE);

        publishReloadData(new LinkedList());
        publishClearSelection();

        ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(true);

        setTitle(i18n.string(R.string.label_search));
    }


    private void doSearch(boolean collapse) {
        AccountIndentNode fromNode = fromAccountList.get(vFromAccount.getSelectedItemPosition());
        AccountIndentNode toNode = toAccountList.get(vToAccount.getSelectedItemPosition());
        String fromDateText = vFromDate.getText().toString();
        String toDateText = vToDate.getText().toString();
        String fromMoneyText = vFromMoney.getText().toString();
        String toMoneyText = vToMoney.getText().toString();
        String noteText = vNote.getText().toString();

        boolean anyCondition = false;

        String fromAccountId = null;
        String toAccountId = null;
        Date fromDate = null;
        Date toDate = null;
        Double fromMoney = null;
        Double toMoney = null;
        String note = null;

        if(fromNode!=null){
            Account account = fromNode.getAccount();
            if(account!=null) {
                fromAccountId = account.getId();
            }
        }
        if(toNode!=null){
            Account account = toNode.getAccount();
            if(account!=null) {
                toAccountId = account.getId();
            }
        }

        try {
            fromDate = dateFormat.parse(fromDateText);
            fromDate = calendarHelper().toDayStart(fromDate);
        } catch (ParseException e) {
        }
        try {
            toDate = dateFormat.parse(toDateText);
            toDate = calendarHelper().toDayEnd(toDate);
        } catch (ParseException e) {
        }
        try {
            fromMoney = Formats.string2Double(fromMoneyText);
        } catch (Exception x) {
        }
        try {
            toMoney = Formats.string2Double(toMoneyText);
        } catch (Exception x) {
        }
        if (!Strings.isBlank(noteText)) {
            note = noteText.trim();
        }

        anyCondition = fromAccountId != null || toAccountId != null || fromDate != null || toDate != null
                || fromMoney != null || toMoney != null || note != null;

        if (!anyCondition) {
            GUIs.shortToast(this, i18n.string(R.string.msg_no_condition));
            return;
        }

        final IDataProvider.SearchCondition condition = new IDataProvider.SearchCondition();
        condition.withFromAccountId(fromAccountId).withToAccountId(toAccountId)
                .withFromDate(fromDate).withToDate(toDate)
                .withFromMoney(fromMoney).withToMoney(toMoney)
                .withNote(note);

        if(collapse) {
            ((AppBarLayout) findViewById(R.id.appbar)).setExpanded(false);
        }

        vSearchHint.setVisibility(View.GONE);
        vResultContainer.setVisibility(View.VISIBLE);

        if (listFragment == null) {
            listFragment = new RecordListFragment();
            Bundle b = new Bundle();
            b.putInt(RecordListFragment.ARG_POS, pos);
            b.putInt(RecordListFragment.ARG_MODE, RecordListFragment.MODE_ALL);
            listFragment.setArguments(b);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frag_container, listFragment)
                    .disallowAddToBackStack()
                    .commit();
        }


        final IDataProvider idp = contexts().getDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @SuppressWarnings("unchecked")
            List<Record> data = Collections.EMPTY_LIST;
            int count = 0;

            @Override
            public void run() {

                data = idp.searchRecord(condition, preference().getMaxRecords());
                //do we need to count by sql for real size?
                count = data.size();
            }

            @Override
            public void onBusyFinish() {
                publishReloadData(data);
                setTitle(i18n.string(R.string.label_search)+" - "+i18n.string(R.string.msg_n_items, count));
            }
        });
    }

    private void doCalculator2(int cal2RequestorId) {
        this.cal2RequestorId = cal2RequestorId;

        Intent intent = null;
        intent = new Intent(this, Calculator.class);
        intent.putExtra(Calculator.ARG_NEED_RESULT, true);
        intent.putExtra(Calculator.ARG_THEME, isLightTheme() ? Calculator.THEME_LIGHT : Calculator.THEME_DARK);

        String start = "";
        try {
            if (cal2RequestorId == R.id.btn_from_cal2) {
                start = Formats.editorTextNumberDecimalToCal2(vFromMoney.getText().toString());
            } else {
                start = Formats.editorTextNumberDecimalToCal2(vToMoney.getText().toString());
            }

        } catch (Exception x) {
        }

        intent.putExtra(Calculator.ARG_START_VALUE, start);
        startActivityForResult(intent, Constants.REQUEST_CALCULATOR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CALCULATOR_CODE && resultCode == Activity.RESULT_OK) {
            String result = data.getExtras().getString(Calculator.ARG_RESULT_VALUE);
            try {
                if (cal2RequestorId == R.id.btn_from_cal2) {
                    vFromMoney.setText(Formats.cal2ToEditorTextNumberDecimal(result));
                } else {
                    vToMoney.setText(Formats.cal2ToEditorTextNumberDecimal(result));
                }

            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
            return;
        } else if (requestCode == Constants.REQUEST_RECORD_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    //user might add record, search again.
                    doSearch(false);

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

    private void publishReloadData(List<Record> data) {
        lookupQueue().publish(new EventQueue.EventBuilder(QEvents.RecordListFrag.ON_RELOAD_FRAGMENT).withData(data).withArg(RecordListFragment.ARG_POS, pos).build());
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
                        GUIs.shortToast(RecordSearcherActivity.this, i18n.string(R.string.msg_record_deleted));
                        doSearch(false);
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

}
