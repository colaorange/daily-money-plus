package com.colaorange.dailymoney.core.ui.legacy;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * @author dennis
 */
public class AccountRecordListActivity extends ContextsActivity {

    public static final String PARAM_START = "accdet.start";
    public static final String PARAM_END = "accdet.end";
    public static final String PARAM_TARGET = "accdet.target";
    public static final String PARAM_TARGET_INFO = "accdet.targetInfo";


    RecordListHelper recordListHelper;

    TextView infoView;


    private Date startDate;
    private Date endDate;
    private String info;
    private Object target;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_record_list);
        initParams();
        initMembers();
        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        }, 25);
    }


    private void initParams() {
        I18N i18n = i18n();

        Bundle b = getIntentExtras();
        startDate = (Date) b.get(PARAM_START);
        endDate = (Date) b.get(PARAM_END);
        target = b.get(PARAM_TARGET);
        info = b.getString(PARAM_TARGET_INFO);
        info = info == null ? " " : info + " ";

        DateFormat format = preference().getDateFormat();
        String fromStr = startDate == null ? "" : format.format(startDate);
        String toStr = endDate == null ? "" : format.format(endDate);

        info = info + i18n.string(R.string.label_acc_reclist_dateinfo, fromStr, toStr);

        //TODO
        if (target instanceof AccountType) {
        } else if (target instanceof Account) {
        } else if (target instanceof String) {
        } else {
            throw new IllegalStateException("unknow target type " + target);
        }

    }


    private void initMembers() {

        recordListHelper = new RecordListHelper(this, true, new RecordListHelper.OnRecordListener() {
            @Override
            public void onRecordDeleted(Record record) {
                I18N i18n = i18n();
                GUIs.shortToast(AccountRecordListActivity.this, i18n.string(R.string.msg_record_deleted));
                refreshUI();
                setResult(RESULT_OK);
            }
        });

        infoView = findViewById(R.id.account_detail_list_infobar);

        ListView listView = findViewById(R.id.account_detail_list_list);
        recordListHelper.setup(listView);
        registerForContextMenu(listView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_DETAIL_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    refreshUI();
                    setResult(RESULT_OK);
                }
            });

        }
    }

    private void refreshUI() {
        infoView.setText(info);
        final IDataProvider idp = contexts().getDataProvider();
//        recordListHelper.refreshUI(idp.listAllRecord());
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @SuppressWarnings("unchecked")
            List<Record> data = Collections.EMPTY_LIST;
            int count = 0;

            @Override
            public void run() {
                if (target instanceof Account) {
                    data = idp.listRecord((Account) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((Account) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                } else if (target instanceof AccountType) {
                    data = idp.listRecord((AccountType) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((AccountType) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                } else if (target instanceof String) {
                    data = idp.listRecord((String) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countRecord((String) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                }
            }

            @Override
            public void onBusyFinish() {
                recordListHelper.reloadData(data);
                infoView.setText(info + i18n().string(R.string.label_count, count));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_detail_mgnt_optmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.account_detail_mgnt_menu_new) {
            recordListHelper.doNewRecord();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.account_detail_list_list) {
            getMenuInflater().inflate(R.menu.account_detail_mgnt_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.account_detail_mgnt_menu_edit) {
            recordListHelper.doEditRecord(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_detail_mgnt_menu_delete) {
            recordListHelper.doDeleteRecord(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_detail_mgnt_menu_copy) {
            recordListHelper.doCopyRecord(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
