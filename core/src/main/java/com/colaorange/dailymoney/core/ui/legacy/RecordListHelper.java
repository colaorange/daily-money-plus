package com.colaorange.dailymoney.core.ui.legacy;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author dennis
 */
public class RecordListHelper implements OnItemClickListener {


    private List<Record> listData = new ArrayList<>();

    private ListView listView;

    private RecordListAdapter listAdapter;

    private Map<String, Account> accountMap = new HashMap<String, Account>();

    private boolean clickeditable;

    private OnRecordListener listener;

    private ContextsActivity activity;

    private int recordListLayout;

    private int workingBookId;
    private DateFormat dateFormat;
    private DateFormat weekDayFormat;
    CalendarHelper calHelper;
    I18N i18n;

    Map<AccountType, Integer> accountBgColorMap;
    Map<AccountType, Integer> accountTextColorMap;

    public RecordListHelper(ContextsActivity activity, boolean clickeditable, OnRecordListener listener) {
        this.activity = activity;
        this.clickeditable = clickeditable;
        this.listener = listener;

        Preference preference = Contexts.instance().getPreference();
        recordListLayout = preference.getRecordListLayout();
        dateFormat = preference.getDateFormat();//for 2010/01/01
        weekDayFormat = preference.getWeekDayFormat();// Wed.
        calHelper = Contexts.instance().getCalendarHelper();
        i18n = Contexts.instance().getI18n();
        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
    }


    public void setup(ListView listview) {
        workingBookId = Contexts.instance().getWorkingBookId();
        listData = new LinkedList<>();
        listAdapter = new RecordListAdapter(activity, listData);

        listView = listview;
        listView.setAdapter(listAdapter);
        if (clickeditable) {
            listView.setOnItemClickListener(this);
        }

        IDataProvider idp = Contexts.instance().getDataProvider();
        for (Account acc : idp.listAccount(null)) {
            accountMap.put(acc.getId(), acc);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == listView) {
            doEditRecord(pos);
        }
    }


    public void reloadData(List<Record> data) {
        if (listData != data) {//not self call
            listData.clear();
            listData.addAll(data);
        }

        workingBookId = Contexts.instance().getWorkingBookId();
        listAdapter.notifyDataSetChanged();

        listAdapter.notifyDataSetChanged();
    }

    public void doNewRecord() {
        doNewRecord(null);
    }

    public void doNewRecord(Date date) {
        Intent intent = null;
        intent = new Intent(activity, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        intent.putExtra(RecordEditorActivity.ARG_CREATED_DATE, date);
        activity.startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }


    public void doEditRecord(int pos) {
        Record d = listData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, false);
        intent.putExtra(RecordEditorActivity.ARG_RECORD, d);
        activity.startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }

    public void doDeleteRecord(final int pos) {
        final Record record = listData.get(pos);

        GUIs.confirm(activity, i18n.string(R.string.qmsg_delete_record, Contexts.instance().toFormattedMoneyString(record.getMoney())), new GUIs.OnFinishListener() {
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    boolean r = Contexts.instance().getDataProvider().deleteRecord(record.getId());
                    if (r) {
                        if (listener != null) {
                            listener.onRecordDeleted(record);
                        } else {
                            listData.remove(pos);
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                }
                return true;
            }
        });


    }


    public void doCopyRecord(int pos) {
        Record d = listData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, RecordEditorActivity.class);
        intent.putExtra(RecordEditorActivity.ARG_MODE_CREATE, true);
        intent.putExtra(RecordEditorActivity.ARG_RECORD, d);
        activity.startActivityForResult(intent, Constants.REQUEST_RECORD_EDITOR_CODE);
    }


    public interface OnRecordListener {
        void onRecordDeleted(Record record);
    }


    private class RecordListAdapter extends ArrayAdapter<Record> {

        LayoutInflater inflater;

        public RecordListAdapter(@NonNull Context context, List<Record> list) {
            super(context, R.layout.book_mgnt_item, list);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            RecordViewHolder holder;
            if (convertView == null) {
                int layout;
                switch (recordListLayout) {
                    case 2:
                        layout = R.layout.record_list_item2;
                        break;
                    case 3:
                        layout = R.layout.record_list_item3;
                        break;
                    case 4:
                        layout = R.layout.record_list_item4;
                        break;
                    case 1:
                    default:
                        layout = R.layout.record_list_item1;
                }

                convertView = inflater.inflate(layout, null);
                convertView.setTag(holder = new RecordViewHolder());
            } else {
                holder = (RecordViewHolder) convertView.getTag();
            }

            holder.bindViewValue(getItem(position), convertView);

            return convertView;
        }


    }

    private class RecordViewHolder {

        public void bindViewValue(Record record, View convertView) {

            LinearLayout vlayout = convertView.findViewById(R.id.record_item_layout);
            LinearLayout vinner = convertView.findViewById(R.id.record_item_layout);
            LinearLayout vfromborder = convertView.findViewById(R.id.record_item_from_border);
            LinearLayout vtoborder = convertView.findViewById(R.id.record_item_to_border);
            TextView vfrom = convertView.findViewById(R.id.detail_mgnt_item_from);
            TextView vto = convertView.findViewById(R.id.detail_mgnt_item_to);
            TextView vmoney = convertView.findViewById(R.id.detail_mgnt_item_money);
            TextView vnote = convertView.findViewById(R.id.detail_mgnt_item_note);
            TextView vdate = convertView.findViewById(R.id.detail_mgnt_item_date);


            Account fromAcc = accountMap.get(record.getFrom());
            Account toAcc = accountMap.get(record.getTo());

            AccountType fromAccType = fromAcc == null ? AccountType.UNKONW : AccountType.find(fromAcc.getType());
            AccountType toAccType = toAcc == null ? AccountType.UNKONW : AccountType.find(toAcc.getType());


            vlayout.setBackgroundColor(accountBgColorMap.get(toAccType));
            vtoborder.setBackgroundColor(accountTextColorMap.get(toAccType));
            vto.setTextColor(accountTextColorMap.get(toAccType));
            vnote.setTextColor(accountTextColorMap.get(toAccType));

            vfromborder.setBackgroundColor(accountTextColorMap.get(fromAccType));
            vfrom.setTextColor(accountTextColorMap.get(fromAccType));

            //
            String from = fromAcc == null ? record.getFrom() : (i18n.string(R.string.label_reclist_from, fromAcc.getName(), AccountType.getDisplay(i18n, fromAcc.getType())));
            String to = toAcc == null ? record.getTo() : (i18n.string(R.string.label_reclist_to, toAcc.getName(), AccountType.getDisplay(i18n, toAcc.getType())));
            String money = Contexts.instance().toFormattedMoneyString(record.getMoney());
            String date = dateFormat.format(record.getDate()) + " " + weekDayFormat.format(record.getDate());

            vfrom.setText(from);
            vto.setText(to);
            vmoney.setText(money);
            vnote.setText(record.getNote());
            vdate.setText(date);
        }
    }
}
