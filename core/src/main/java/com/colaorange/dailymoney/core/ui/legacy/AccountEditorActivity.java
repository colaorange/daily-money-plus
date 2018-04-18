package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.HashMap;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

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

    Activity activity;

    ImageButton cal2Btn;


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

    /**
     * need to mapping twice to do different mapping in spitem and spdropdown item
     */
    private static String[] spfrom = new String[]{Constants.DISPLAY, Constants.DISPLAY};
    private static int[] spto = new int[]{R.id.simple_spinner_item_display, R.id.simple_spinner_dropdown_item_display};

    EditText nameEditor;
    EditText initvalEditor;
    Spinner typeEditor;
    CheckBox cashAccountEditor;

    Button okBtn;
    Button cancelBtn;
    Button closeBtn;

    private void initMembers() {
        I18N i18n = i18n();

        nameEditor = findViewById(R.id.account_editor_name);
        nameEditor.setText(workingAccount.getName());

        initvalEditor = findViewById(R.id.account_editor_initval);
        initvalEditor.setText(Formats.double2String(workingAccount.getInitialValue()));

        //initial spinner
        typeEditor = findViewById(R.id.account_editor_type);
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        String type = workingAccount.getType();

        AccountType selType = null;
        int selpos = 0;
        int i = 0;
        for (AccountType at : AccountType.getSupportedType()) {
            Map<String, Object> row = new HashMap<>();
            data.add(row);
            row.put(spfrom[0], new NamedItem(spfrom[0], at, at.getDisplay(i18n)));

            if (at.getType().equals(type)) {
                selpos = i;
                selType = at;
            }
            i++;
        }


        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.simple_spinner_dropdown_item, spfrom, spto);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        adapter.setViewBinder(new AccountTypeViewBinder());
        typeEditor.setAdapter(adapter);
        typeEditor.setSelection(selpos);
        typeEditor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                AccountType type = AccountType.getSupportedType()[typeEditor.getSelectedItemPosition()];
                doTypeChanged(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        cashAccountEditor = findViewById(R.id.account_editor_cash_account);

        cashAccountEditor.setChecked(workingAccount.isCashAccount());

        okBtn = findViewById(R.id.acceditor_ok);
        if (modeCreate) {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_24dp, 0, 0, 0);
            okBtn.setText(R.string.cact_create);
        } else {
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_white_24dp, 0, 0, 0);
            okBtn.setText(R.string.cact_update);
        }
        okBtn.setOnClickListener(this);


        cancelBtn = findViewById(R.id.acceditor_cancel);
        closeBtn = findViewById(R.id.acceditor_close);
        cal2Btn = findViewById(R.id.account_editor_cal2);

        cancelBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        cal2Btn.setOnClickListener(this);

        doTypeChanged(selType);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.acceditor_ok) {
            doOk();
        } else if (v.getId() == R.id.acceditor_cancel) {
            doCancel();
        } else if (v.getId() == R.id.acceditor_close) {
            doClose();
        } else if (v.getId() == R.id.account_editor_cal2) {
            doCalculator2();
        }
    }


    private void doCalculator2() {
        Intent intent = null;
        intent = new Intent(this, Calculator.class);
        intent.putExtra(Calculator.PARAM_NEED_RESULT, true);

        String start = "";
        try {
            start = Formats.editorTextNumberDecimalToCal2(initvalEditor.getText().toString());
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
                initvalEditor.setText(Formats.cal2ToEditorTextNumberDecimal(result));
            } catch (Exception x) {
            }
        }
    }

    private void doOk() {

        I18N i18n = i18n();
        //verify
        if (Spinner.INVALID_POSITION == typeEditor.getSelectedItemPosition()) {
            GUIs.shortToast(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.clabel_type)));
            return;
        }
        String name = nameEditor.getText().toString().trim();
        if ("".equals(name)) {
            nameEditor.requestFocus();
            GUIs.alert(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.clabel_name)));
            return;
        }
        String initval = initvalEditor.getText().toString();
        if ("".equals(initval)) {
            initvalEditor.requestFocus();
            GUIs.alert(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.label_initial_value)));
            return;
        }
        String type = AccountType.getSupportedType()[typeEditor.getSelectedItemPosition()].getType();
        //assign
        workingAccount.setType(type);
        workingAccount.setName(name);
        try {
            workingAccount.setInitialValue(Formats.string2Double(initval));
        }catch (Exception x){
            workingAccount.setInitialValue(0);
        }
        workingAccount.setCashAccount(cashAccountEditor.isChecked());

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
                    GUIs.alert(this, i18n.string(R.string.cmsg_error, e.getMessage()));
                    return;
                }
            }
            setResult(RESULT_OK);
            workingAccount = clone(workingAccount);
            workingAccount.setName("");
            nameEditor.setText("");
            nameEditor.requestFocus();
            counterCreate++;
            okBtn.setText(i18n.string(R.string.cact_create) + "(" + counterCreate + ")");
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
            cashAccountEditor.setVisibility(View.VISIBLE);
        } else {
            cashAccountEditor.setVisibility(View.INVISIBLE);
            cashAccountEditor.setChecked(false);
        }
    }

    class AccountTypeViewBinder implements SimpleAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Object data, String text) {

            NamedItem item = (NamedItem) data;
            String name = item.getName();
            AccountType at = (AccountType) item.getValue();
            if (!(view instanceof TextView)) {
                return false;
            }
            if (Constants.DISPLAY.equals(name)) {
                if (AccountType.INCOME == at) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.income_fgl));
                } else if (AccountType.ASSET == at) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.asset_fgl));
                } else if (AccountType.EXPENSE == at) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.expense_fgl));
                } else if (AccountType.LIABILITY == at) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.liability_fgl));
                } else if (AccountType.OTHER == at) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.other_fgl));
                } else {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.unknow_fgl));
                }
                ((TextView) view).setText(item.getToString());
                return true;
            }
            return false;
        }
    }

}
