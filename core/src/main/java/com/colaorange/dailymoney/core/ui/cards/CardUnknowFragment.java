package com.colaorange.dailymoney.core.ui.cards;

import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Var;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.EventQueue;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.BalanceHelper;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.util.GUIs;

import java.util.Date;
import java.util.List;

/**
 * @author dennis
 */
public class CardUnknowFragment extends CardBaseFragment implements EventQueue.EventListener {

    private TextView vText;

    @Override
    protected void initMembers() {
        super.initMembers();

        vText = rootView.findViewById(R.id.card_text);

        i18n = Contexts.instance().getI18n();

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.card_unknow_frag;
    }

    @Override
    protected boolean doReloadContent() {

        vText.setText(i18n.string(R.string.label_unknown) + ":" + getCard().getType());

        return true;
    }
}