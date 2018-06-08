package com.colaorange.dailymoney.core.ui.legacy;

import android.support.annotation.NonNull;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.colaorange.commons.util.Colors;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List;
import java.util.Map;

/**
 * Created by Dennis
 */
public class AccountIndentNodeSpinnerAdapter extends RegularSpinnerAdapter<AccountUtil.AccountIndentNode> {

    private I18N i18n;
    private Map<AccountType, Integer> textColorMap;
    private int primaryTextColor;
    private float nodePaddingBase;
    private String nullText;

    public AccountIndentNodeSpinnerAdapter(@NonNull ContextsActivity context, List<AccountUtil.AccountIndentNode> items) {
        this(context, items, " ");
    }

    /**
     *
     * @param nullText the text for null item, it is for the case that provides select on nothing
     */
    public AccountIndentNodeSpinnerAdapter(@NonNull ContextsActivity context, List<AccountUtil.AccountIndentNode> items, String nullText) {
        super(context, items);
        this.nullText = nullText;
        i18n = Contexts.instance().getI18n();
        textColorMap = context.getAccountTextColorMap();
        primaryTextColor = context.resolveThemeAttrResData(R.attr.appPrimaryTextColor);
        //10dp
        nodePaddingBase = 10 * context.getDpRatio();
    }

    @Override
    public boolean isEnabled(int position) {
        AccountUtil.AccountIndentNode item = getItem(position);
        return item == null || item.getAccount() != null;
    }

    @Override
    public ViewHolder<AccountUtil.AccountIndentNode> createViewHolder() {
        return new AccountTypeViewBinder(this);
    }


    public class AccountTypeViewBinder extends RegularSpinnerAdapter.ViewHolder<AccountUtil.AccountIndentNode> {

        public AccountTypeViewBinder(RegularSpinnerAdapter adapter) {
            super(adapter);
        }

        @Override
        public void bindViewValue(AccountUtil.AccountIndentNode item, LinearLayout vlayout, TextView vtext, boolean isDropdown, boolean isSelected) {


            int textColor = textColorMap.get(AccountType.UNKONW);

            StringBuilder display = new StringBuilder();
            if (item != null) {
                AccountType at = item.getType();
                textColor = textColorMap.get(item.getType());
                if (isDropdown) {
                    vlayout.setPadding((int) ((1 + item.getIndent()) * nodePaddingBase), vlayout.getPaddingTop(), vlayout.getPaddingRight(), vlayout.getPaddingBottom());

                    if (item.getIndent() == 0) {
                        display.append(item.getType().getDisplay(i18n));
                        display.append(" - ");
                    }
                    display.append(item.getName());

                    if (item.getAccount() == null) {
                        textColor = Colors.lighten(textColor, 0.3f);
                    } else if (isSelected) {
                        textColor = Colors.darken(textColor, 0.3f);
                    }
                } else {
                    display.append(item.getType().getDisplay(i18n));
                    display.append("-");
                    if (item.getAccount() != null) {
                        display.append(item.getAccount().getName());
                    } else {
                        display.append(item.getFullPath());
                    }
                }
            } else {
                textColor = primaryTextColor;
                display.append(nullText);
            }
            vtext.setTextColor(textColor);
            vtext.setText(display.toString());
        }
    }
}
