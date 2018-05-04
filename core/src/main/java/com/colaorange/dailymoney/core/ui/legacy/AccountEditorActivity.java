package com.colaorange.dailymoney.core.ui.legacy;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.Collections;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.calculator2.Calculator;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.DuplicateKeyException;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * Edit or create a account
 *
 * @author dennis
 */
public class AccountEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String PARAM_MODE_CREATE = "modeCreate";
    public static final String PARAM_ACCOUNT = "account";

    private boolean modeCreate;
    private int counterCreate;
    private Account account;
    private Account workingAccount;


    private EditText vName;
    private EditText vInitval;
    private Spinner vType;
    private CheckBox vCash;

    private Button btnOk;
    private Button btnCancel;
    private Button btnClose;
    private ImageButton btnCal2;

    /**
     * clone account without id
     **/
    private Account clone(Account account) {
        Account acc = new Account(account.getType(), account.getName(), account.getInitialValue());
        acc.setCashAccount(account.isCashAccount());
        return acc;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_editor);
        initParams();
        initMembers();
    }

    private void initParams() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(PARAM_MODE_CREATE, true);
        account = (Account) bundle.get(PARAM_ACCOUNT);

        if (modeCreate && account == null) {
            account = new Account(AccountType.INCOME.getType(), "", 0D);
        }
        workingAccount = clone(account);

        if (modeCreate) {
            setTitle(R.string.title_acceditor_create);
        } else {
            setTitle(R.string.title_acceditor_update);
        }
    }

    private void initMembers() {
        I18N i18n = i18n();

        vName = findViewById(R.id.account_name);
        vName.setText(workingAccount.getName());

        vInitval = findViewById(R.id.account_initval);
        vInitval.setText(Formats.double2String(workingAccount.getInitialValue()));

        //initial regular_spinner
        vType = findViewById(R.id.account_type);
        final List<AccountType> list = Collections.asList(AccountType.getSupportedType());
        AccountType type = AccountType.find(workingAccount.getType());

        int selpos = list.indexOf(type);
        RegularSpinnerAdapter<AccountType> adapter = new RegularSpinnerAdapter<AccountType>(this, list) {

            public boolean isSelected(int position) {
                return vType.getSelectedItemPosition() == position;
            }

            @Override
            public ViewHolder<AccountType> createViewHolder() {
                return new AccountTypeViewBinder(this);
            }
        };

        vType.setAdapter(adapter);
        if (selpos > -1) {
            vType.setSelection(selpos);
        }
        vType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AccountType type = list.get(pos);
                doTypeChanged(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        vCash = findViewById(R.id.account_cash);

        vCash.setChecked(workingAccount.isCashAccount());

        btnOk = findViewById(R.id.btn_ok);
        if (modeCreate) {
            btnOk.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_add), 0, 0, 0);
            btnOk.setText(R.string.act_create);
        } else {
            btnOk.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_save), 0, 0, 0);
            btnOk.setText(R.string.act_update);
        }
        btnOk.setOnClickListener(this);


        btnCancel = findViewById(R.id.btn_cancel);
        btnClose = findViewById(R.id.btn_close);
        btnCal2 = findViewById(R.id.btn_cal2);

        btnCancel.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnCal2.setOnClickListener(this);

        doTypeChanged(type);


    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            doOk();
        } else if (v.getId() == R.id.btn_cancel) {
            doCancel();
        } else if (v.getId() == R.id.btn_close) {
            doClose();
        } else if (v.getId() == R.id.btn_cal2) {
            doCalculator2();
        }
    }


    private void doCalculator2() {
        Intent intent = null;
        intent = new Intent(this, Calculator.class);
        intent.putExtra(Calculator.PARAM_NEED_RESULT, true);
        intent.putExtra(Calculator.PARAM_THEME, isLightTheme() ? Calculator.THEME_LIGHT : Calculator.THEME_DARK);

        String start = "";
        try {
            start = Formats.editorTextNumberDecimalToCal2(vInitval.getText().toString());
        } catch (Exception e) {
        }

        intent.putExtra(Calculator.PARAM_START_VALUE, start);
        startActivityForResult(intent, Constants.REQUEST_CALCULATOR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CALCULATOR_CODE && resultCode == Activity.RESULT_OK) {
            String result = data.getExtras().getString(Calculator.PARAM_RESULT_VALUE);
            try {
                double d = Formats.string2Double(result);
                vInitval.setText(Formats.cal2ToEditorTextNumberDecimal(result));
            } catch (Exception x) {
            }
        }
    }

    private void doOk() {

        I18N i18n = i18n();
        //verify
        if (Spinner.INVALID_POSITION == vType.getSelectedItemPosition()) {
            GUIs.shortToast(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_type)));
            return;
        }
        String name = vName.getText().toString().trim();
        if ("".equals(name)) {
            vName.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_name)));
            return;
        }
        String initval = vInitval.getText().toString();
        if ("".equals(initval)) {
            vInitval.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_initial_value)));
            return;
        }
        String type = AccountType.getSupportedType()[vType.getSelectedItemPosition()].getType();
        //assign
        workingAccount.setType(type);
        workingAccount.setName(name);
        try {
            workingAccount.setInitialValue(Formats.string2Double(initval));
        } catch (Exception x) {
            workingAccount.setInitialValue(0);
        }
        workingAccount.setCashAccount(vCash.isChecked());

        IDataProvider idp = contexts().getDataProvider();

        Account namedAcc = idp.findAccount(type, name);
        if (modeCreate) {
            if (namedAcc != null) {
                GUIs.alert(
                        this, i18n.string(R.string.msg_account_existed, name,
                                AccountType.getDisplay(i18n, namedAcc.getType())));
                return;
            } else {
                try {
                    idp.newAccount(workingAccount);
                    GUIs.shortToast(this, i18n.string(R.string.msg_account_created, name, AccountType.getDisplay(i18n, workingAccount.getType())));
                } catch (DuplicateKeyException e) {
                    GUIs.alert(this, i18n.string(R.string.msg_error, e.getMessage()));
                    return;
                }
            }
            setResult(RESULT_OK);
            workingAccount = clone(workingAccount);
            workingAccount.setName("");
            vName.setText("");
            vName.requestFocus();
            counterCreate++;
            btnOk.setText(i18n.string(R.string.act_create) + "(" + counterCreate + ")");
            btnCancel.setVisibility(Button.GONE);
            btnClose.setVisibility(Button.VISIBLE);
            trackEvent(TE.CREATE_ACCOUNT);
        } else {
            if (namedAcc != null && !namedAcc.getId().equals(account.getId())) {
                GUIs.alert(this, i18n.string(R.string.msg_account_existed, name,
                        AccountType.getDisplay(i18n, namedAcc.getType())));
                return;
            } else {
                idp.updateAccount(account.getId(), workingAccount);
                GUIs.shortToast(this, i18n.string(R.string.msg_account_updated, name, AccountType.getDisplay(i18n, workingAccount.getType())));
            }

            setResult(RESULT_OK);
            finish();
            trackEvent(TE.UPDDATE_ACCOUNT);
        }

    }

    private void doCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void doClose() {
        setResult(RESULT_OK);
        GUIs.shortToast(this, i18n().string(R.string.msg_created_account, counterCreate));
        finish();
    }

    private void doTypeChanged(AccountType type) {
        if (AccountType.ASSET.equals(type)) {
            vCash.setVisibility(View.VISIBLE);
        } else {
            vCash.setVisibility(View.INVISIBLE);
            vCash.setChecked(false);
        }
    }

    public class AccountTypeViewBinder extends RegularSpinnerAdapter.ViewHolder<AccountType> {

        public AccountTypeViewBinder(RegularSpinnerAdapter adapter) {
            super(adapter);
        }

        @Override
        public void bindViewValue(AccountType item, LinearLayout vlayout, TextView vtext, boolean isDropdown, boolean isSelected) {
            Map<AccountType, Integer> textColorMap = getAccountTextColorMap();
            Map<AccountType, Integer> bgColorMap = getAccountBgColorMap();

            int textColor = textColorMap.get(item);

            vtext.setTextColor(textColor);
            vtext.setText(item.getDisplay(i18n()));

            if (isDropdown && isSelected) {
                textColor = Colors.darken(textColor, 0.3f);
                vtext.setTextColor(textColor);
            }

        }
    }

}
