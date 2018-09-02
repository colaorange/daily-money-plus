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
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Balance;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.List;
import java.util.Map;

/**
 * Created by Dennis
 */
public class BalanceRecyclerAdapter extends SelectableRecyclerViewAdaptor<Balance, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

    private LayoutInflater inflater;
    private int decimalLength = 0;
    private GUIs.Dimen textSize;
    private GUIs.Dimen textSizeMedium;

    public BalanceRecyclerAdapter(ContextsActivity activity, List<Balance> data) {
        super(activity, data);

        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        textSize = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSize).data);
        textSizeMedium = GUIs.toDimen(activity.resolveThemeAttr(R.attr.textSizeMedium).data);
    }

    public void setDecimalLength(int decimalLength){
        this.decimalLength = decimalLength;
    }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = inflater.inflate(R.layout.balance_mgnt_item, parent, false);
        return new BalanceViewHolder(this, viewItem);
    }

    private class BalanceViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<BalanceRecyclerAdapter, Balance> {

        public BalanceViewHolder(BalanceRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Balance balance) {
            super.bindViewValue(balance);

            Contexts contexts = Contexts.instance();

            Map<AccountType, Integer> textColorMap = activity.getAccountTextColorMap();
            Map<AccountType, Integer> bgColorMap = activity.getAccountBgColorMap();
            boolean lightTheme = activity.isLightTheme();
            float dpRatio = activity.getDpRatio();

            LinearLayout vlayout = itemView.findViewById(R.id.balance_item_layout);
            TextView vname = itemView.findViewById(R.id.balance_item_name);
            TextView vmoney = itemView.findViewById(R.id.balance_item_money);

            AccountType at = AccountType.find(balance.getType());
            int indent = balance.getIndent();
            boolean head = indent == 0;

            Integer textColor;

            vname.setText(balance.getName());
            vmoney.setText(contexts.toFormattedMoneyString(balance.getMoney(), decimalLength));

            boolean selected = adaptor.isSelected(balance);

            //transparent mask for selecting ripple effect
            int mask = 0xE0FFFFFF;
            int hpd = (int) (10 * dpRatio);
            int vpd = (int) (6 * dpRatio);

            if (head) {

                vpd += 2;

                int bg = mask & activity.resolveThemeAttrResData(R.attr.balanceHeadBgColor);
                if (selected) {
                    bg = Colors.lighten(bg, 0.15f);
                }
                vlayout.setBackgroundColor(bg);
                if (!lightTheme) {
                    textColor = textColorMap.get(at);
                } else {
                    textColor = bgColorMap.get(at);
                }

                vname.setTextSize(textSizeMedium.unit, textSizeMedium.value);
                vmoney.setTextSize(textSizeMedium.unit, textSizeMedium.value);
            } else {
                int bg = mask & activity.resolveThemeAttrResData(R.attr.balanceItemBgColor);
                if (selected) {
                    bg = Colors.darken(bg, 0.15f);
                }
                vlayout.setBackgroundColor(bg);
                if (lightTheme) {
                    textColor = textColorMap.get(at);
                } else {
                    textColor = bgColorMap.get(at);
                }
                vname.setTextSize(textSize.unit, textSize.value);
                vmoney.setTextSize(textSize.unit, textSize.value);
            }


            vlayout.setPadding((int) ((1 + indent) * hpd), vpd, hpd, vpd);


            vname.setTextColor(textColor);
            vmoney.setTextColor(textColor);

        }
    }
}
