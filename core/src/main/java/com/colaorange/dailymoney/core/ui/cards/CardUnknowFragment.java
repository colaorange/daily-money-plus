package com.colaorange.dailymoney.core.ui.cards;

import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.EventQueue;

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