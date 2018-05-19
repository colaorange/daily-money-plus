package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List;
import java.util.Map;

/**
 * @author dennis
 */
public class AccountRecyclerAdapter extends SelectableRecyclerViewAdaptor<Account, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

    private LayoutInflater inflater;
    private I18N i18n;
    private Map<AccountType, Integer> textColorMap;

    public AccountRecyclerAdapter(ContextsActivity activity, List<Account> data) {
        super(activity, data);

        i18n = Contexts.instance().getI18n();
        textColorMap = activity.getAccountTextColorMap();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = inflater.inflate(R.layout.account_mgnt_item, parent, false);
        return new AccountViewHolder(this, viewItem);
    }


    public class AccountViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<AccountRecyclerAdapter, Account> {

        public AccountViewHolder(AccountRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Account account) {
            super.bindViewValue(account);

            TextView vname = itemView.findViewById(R.id.account_item_name);
            TextView vid = itemView.findViewById(R.id.account_item_id);
            TextView initvalue = itemView.findViewById(R.id.account_item_initvalue);

            vname.setText(account.getName());
            vid.setText(account.getId());
            initvalue.setText(i18n.string(R.string.label_initial_value) + " : " + Formats.double2String(account.getInitialValue()));

            int textColor = textColorMap.get(AccountType.find(account.getType()));

            vname.setTextColor(textColor);
            vid.setTextColor(textColor);
            initvalue.setTextColor(textColor);
        }
    }
}