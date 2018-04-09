package com.colaorange.dailymoney.ui.legacy;

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

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.GUIs;
import com.colaorange.commons.util.I18N;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.Account;
import com.colaorange.dailymoney.data.AccountType;
import com.colaorange.dailymoney.data.Detail;
import com.colaorange.dailymoney.data.IDataProvider;
import com.colaorange.dailymoney.ui.Constants;

/**
 * @author dennis
 */
public class AccountDetailListActivity extends ContextsActivity {

    public static final String PARAM_START = "accdet.start";
    public static final String PARAM_END = "accdet.end";
    public static final String PARAM_TARGET = "accdet.target";
    public static final String PARAM_TARGET_INFO = "accdet.targetInfo";


    DetailListHelper detailListHelper;

    TextView infoView;


    private Date startDate;
    private Date endDate;
    private String info;
    private Object target;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_detail_mgnt);
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

        info = info + i18n.string(R.string.label_accdetlist_dateinfo, fromStr, toStr);

        //TODO
        if (target instanceof AccountType) {
        } else if (target instanceof Account) {
        } else if (target instanceof String) {
        } else {
            throw new IllegalStateException("unknow target type " + target);
        }

    }


    private void initMembers() {

        detailListHelper = new DetailListHelper(this, true, new DetailListHelper.OnDetailListener() {
            @Override
            public void onDetailDeleted(Detail detail) {
                I18N i18n = i18n();
                GUIs.shortToast(AccountDetailListActivity.this, i18n.string(R.string.msg_detail_deleted));
                refreshUI();
                setResult(RESULT_OK);
            }
        });

        infoView = findViewById(R.id.account_detail_list_infobar);

        ListView listView = findViewById(R.id.account_detail_list_list);
        detailListHelper.setup(listView);
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
//        detailListHelper.refreshUI(idp.listAllDetail());
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            @SuppressWarnings("unchecked")
            List<Detail> data = Collections.EMPTY_LIST;
            int count = 0;

            @Override
            public void run() {
                if (target instanceof Account) {
                    data = idp.listDetail((Account) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countDetail((Account) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                } else if (target instanceof AccountType) {
                    data = idp.listDetail((AccountType) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countDetail((AccountType) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                } else if (target instanceof String) {
                    data = idp.listDetail((String) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate, preference().getMaxRecords());
                    count = idp.countDetail((String) target, IDataProvider.LIST_DETAIL_MODE_BOTH, startDate, endDate);
                }
            }

            @Override
            public void onBusyFinish() {
                detailListHelper.reloadData(data);
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
            detailListHelper.doNewDetail();
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
            detailListHelper.doEditDetail(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_detail_mgnt_menu_delete) {
            detailListHelper.doDeleteDetail(info.position);
            return true;
        } else if (item.getItemId() == R.id.account_detail_mgnt_menu_copy) {
            detailListHelper.doCopyDetail(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
