package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.calculator2.Calculator;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
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

    public static final String PARAM_MODE_CREATE = "account_editor.modeCreate";
    public static final String PARAM_ACCOUNT = "account_editor.account";

    private boolean modeCreate;
    private int counterCreate;
    private Account account;
    private Account workingAccount;

    private Activity activity;

    private ImageButton btnCal2;

    private float ddItemPaddingBase;
    private Drawable ddSelectedBg;

    /**
     * need to mapping twice to do different mapping in spitem and spdropdown item
     */
    private static String[] typeMappingKeys = new String[]{Constants.SIMPLE_SPINNER_LABEL_KEY};
    private static int[] typeMappingResIds = new int[]{R.id.simple_spinner_item_label};

    private EditText editName;
    private EditText editInitval;
    private Spinner spType;
    private CheckBox ckCash;

    private Button okBtn;
    private Button cancelBtn;
    private Button closeBtn;

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
        workingAccount = clone(account);

        if (modeCreate) {
            setTitle(R.string.title_acceditor_create);
        } else {
            setTitle(R.string.title_acceditor_update);
        }
    }

    private void initMembers() {
        I18N i18n = i18n();

        editName = findViewById(R.id.account_name);
        editName.setText(workingAccount.getName());

        editInitval = findViewById(R.id.account_initval);
        editInitval.setText(Formats.double2String(workingAccount.getInitialValue()));

        //initial spinner
        spType = findViewById(R.id.account_type);
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        String type = workingAccount.getType();

        AccountType selType = null;
        int selpos = 0;
        int i = 0;
        for (AccountType at : AccountType.getSupportedType()) {
            Map<String, Object> row = new HashMap<>();
            data.add(row);
            row.put(typeMappingKeys[0], new NamedItem(typeMappingKeys[0], at, at.getDisplay(i18n)));

            if (at.getType().equals(type)) {
                selpos = i;
                selType = at;
            }
            i++;
        }


        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.simple_spinner_dropdown, typeMappingKeys, typeMappingResIds);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        adapter.setViewBinder(new AccountTypeViewBinder());
        spType.setAdapter(adapter);
        spType.setSelection(selpos);
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AccountType type = AccountType.getSupportedType()[spType.getSelectedItemPosition()];
                doTypeChanged(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        ckCash = findViewById(R.id.account_cash);

        ckCash.setChecked(workingAccount.isCashAccount());

        okBtn = findViewById(R.id.btn_ok);
        if (modeCreate) {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_add), 0, 0, 0);
            okBtn.setText(R.string.act_create);
        } else {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_save), 0, 0, 0);
            okBtn.setText(R.string.act_update);
        }
        okBtn.setOnClickListener(this);


        cancelBtn = findViewById(R.id.btn_cancel);
        closeBtn = findViewById(R.id.btn_close);
        btnCal2 = findViewById(R.id.btn_cal2);

        cancelBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        btnCal2.setOnClickListener(this);


        ddItemPaddingBase = 15 * GUIs.getDPRatio(this);
        ddSelectedBg = getResources().getDrawable(resolveThemeAttrResId(R.attr.colorControlNormal));

        doTypeChanged(selType);
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
        intent.putExtra(Calculator.PARAM_THEME, isAppLightTheme() ? Calculator.THEME_LIGHT : Calculator.THEME_DARK);

        String start = "";
        try {
            start = Formats.editorTextNumberDecimalToCal2(editInitval.getText().toString());
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
                editInitval.setText(Formats.cal2ToEditorTextNumberDecimal(result));
            } catch (Exception x) {
            }
        }
    }

    private void doOk() {

        I18N i18n = i18n();
        //verify
        if (Spinner.INVALID_POSITION == spType.getSelectedItemPosition()) {
            GUIs.shortToast(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_type)));
            return;
        }
        String name = editName.getText().toString().trim();
        if ("".equals(name)) {
            editName.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_name)));
            return;
        }
        String initval = editInitval.getText().toString();
        if ("".equals(initval)) {
            editInitval.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_initial_value)));
            return;
        }
        String type = AccountType.getSupportedType()[spType.getSelectedItemPosition()].getType();
        //assign
        workingAccount.setType(type);
        workingAccount.setName(name);
        try {
            workingAccount.setInitialValue(Formats.string2Double(initval));
        } catch (Exception x) {
            workingAccount.setInitialValue(0);
        }
        workingAccount.setCashAccount(ckCash.isChecked());

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
            editName.setText("");
            editName.requestFocus();
            counterCreate++;
            okBtn.setText(i18n.string(R.string.act_create) + "(" + counterCreate + ")");
            cancelBtn.setVisibility(Button.GONE);
            closeBtn.setVisibility(Button.VISIBLE);
            trackEvent(Contexts.TRACKER_EVT_CREATE);
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
            trackEvent(Contexts.TRACKER_EVT_UPDATE);
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
            ckCash.setVisibility(View.VISIBLE);
        } else {
            ckCash.setVisibility(View.INVISIBLE);
            ckCash.setChecked(false);
        }
    }

    private class AccountTypeViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String text) {

            if (view.getId() == typeMappingResIds[0]) {
                NamedItem item = (NamedItem) data;
                AccountType at = (AccountType) item.getValue();
                TextView tv = (TextView) view;
                int textColor;
                if (AccountType.INCOME == at) {
                    textColor = resolveThemeAttrResData(R.attr.accountIncomeTextColor);
                } else if (AccountType.ASSET == at) {
                    textColor = resolveThemeAttrResData(R.attr.accountAssetTextColor);
                } else if (AccountType.EXPENSE == at) {
                    textColor = resolveThemeAttrResData(R.attr.accountExpenseTextColor);
                } else if (AccountType.LIABILITY == at) {
                    textColor = resolveThemeAttrResData(R.attr.accountLiabilityTextColor);
                } else if (AccountType.OTHER == at) {
                    textColor = resolveThemeAttrResData(R.attr.accountOtherTextColor);
                } else {
                    textColor = resolveThemeAttrResData(R.attr.accountUnknownTextColor);
                }
                tv.setTextColor(textColor);
                tv.setText(item.getToString());

                if (Constants.SIMPLE_SPINNER_ITEM_TAG.equals(tv.getTag())) {

                    int pos = spType.getSelectedItemPosition();
                    if (pos >= 0 && AccountType.getSupportedType()[spType.getSelectedItemPosition()].equals(at)) {
                        if (isAppLightTheme()) {
                            textColor = Colors.darken(textColor, 0.2f);
                        } else {
                            textColor = Colors.lighten(textColor, 0.2f);
                        }
                        tv.setBackgroundDrawable(ddSelectedBg);
                        tv.setTextColor(textColor);
                    }

                    tv.setPadding((int) ddItemPaddingBase, tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());
                }
                return true;
            }
            return false;
        }
    }

}
