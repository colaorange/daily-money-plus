package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RecordRecyclerAdapter extends SelectableRecyclerViewAdaptor<Record, SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, Record>> {

    private int listLayout;
    private Map<String, Account> accountMap;
    private LayoutInflater inflater;
    private Map<AccountType, Integer> accountBgColorMap;
    private Map<AccountType, Integer> accountTextColorMap;
    private I18N i18n;
    private boolean lightTheme;
    private DateFormat dateFormat;
    private DateFormat weekDayFormat;

    public RecordRecyclerAdapter(ContextsActivity activity, List<Record> data) {
        super(activity, data);
        this.accountMap = accountMap;
        accountBgColorMap = activity.getAccountBgColorMap();
        accountTextColorMap = activity.getAccountTextColorMap();
        lightTheme = activity.isLightTheme();

        Preference preference = Contexts.instance().getPreference();
        dateFormat = preference.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
        weekDayFormat = preference.getWeekDayFormat();// Wed.

        listLayout = preference.getRecordListLayout();
        i18n = Contexts.instance().getI18n();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setAccountMap(Map<String, Account> accountMap) {
        this.accountMap = accountMap;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int itemLayout;
        switch (listLayout) {
            case 2:
                itemLayout = R.layout.record_list_item2;
                break;
            case 3:
                itemLayout = R.layout.record_list_item3;
                break;
            case 4:
                itemLayout = R.layout.record_list_item4;
                break;
            case 1:
            default:
                itemLayout = R.layout.record_list_item1;
        }

        View viewItem = inflater.inflate(R.layout.record_mgnt_item, parent, false);
        inflater.inflate(itemLayout, (ViewGroup) viewItem.findViewById(R.id.layout_select), true);
        return new RecordViewHolder(this, viewItem);
    }


    private class RecordViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<RecordRecyclerAdapter, Record> {

        private RecordViewHolder(RecordRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Record record) {
            super.bindViewValue(record);

            if(accountMap==null){
                throw new IllegalStateException("accountMap is null");
            }

            LinearLayout vlayout = itemView.findViewById(R.id.record_item_layout);
            LinearLayout vfromborder = itemView.findViewById(R.id.record_item_from_border);
            LinearLayout vtoborder = itemView.findViewById(R.id.record_item_to_border);
            TextView vfrom = itemView.findViewById(R.id.record_item_from);
            TextView vto = itemView.findViewById(R.id.record_item_to);
            TextView vmoney = itemView.findViewById(R.id.record_item_money);
            TextView vnote = itemView.findViewById(R.id.record_item_note);
            TextView vdate = itemView.findViewById(R.id.record_item_date);


            Account fromAcc = accountMap.get(record.getFrom());
            Account toAcc = accountMap.get(record.getTo());

            AccountType fromAccType = fromAcc == null ? AccountType.UNKONW : AccountType.find(fromAcc.getType());
            AccountType toAccType = toAcc == null ? AccountType.UNKONW : AccountType.find(toAcc.getType());

            //transparent mask for selecting ripple effect
            int mask = 0xE0FFFFFF;
            boolean selected = adaptor.isSelected(record);

            int bgcolor;
            bgcolor = mask & accountBgColorMap.get(toAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }

            }
            vlayout.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(toAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vtoborder.setBackgroundColor(bgcolor);

            bgcolor = mask & accountTextColorMap.get(fromAccType);
            if (selected) {
                if (lightTheme) {
                    bgcolor = Colors.darken(bgcolor, 0.15f);
                } else {
                    bgcolor = Colors.lighten(bgcolor, 0.15f);
                }
            }
            vfromborder.setBackgroundColor(bgcolor);

            vto.setTextColor(accountTextColorMap.get(toAccType));
            vfrom.setTextColor(accountTextColorMap.get(fromAccType));
            vnote.setTextColor(accountTextColorMap.get(toAccType));


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