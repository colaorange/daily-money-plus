package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.calculator2.Calculator;
import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Colors;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Record;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.legacy.AccountUtil.IndentNode;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Edit or create a record
 *
 * @author dennis
 */
public class RecordEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String PARAM_MODE_CREATE = "recordEditor.modeCreate";
    public static final String PARAM_RECORD = "recordEditor.record";


    private boolean modeCreate;
    private int counterCreate;
    private Record record;
    private Record workingRecord;

    private DateFormat dateFormat;

    boolean archived = false;

    private List<IndentNode> fromAccountList;
    private List<IndentNode> toAccountList;

    List<Map<String, Object>> fromAccountMapList;
    List<Map<String, Object>> toAccountMapList;

    private SimpleAdapter fromAccountAdapter;
    private SimpleAdapter toAccountAdapter;


    private static String[] accountMappingKeys = new String[]{Constants.SIMPLE_SPINNER_LABEL_KEY};
    private static int[] accountMappingResIds = new int[]{R.id.simple_spinner_item_label};

    Spinner spFromAccount;
    Spinner spToAccount;

    EditText editRecordDate;
    EditText editRecordNote;
    EditText editRecordMoney;

    Button btnOk;
    Button btnCancel;
    Button btnClose;

    private float ddItemPaddingBase;
    private Drawable ddSelectedBg;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_editor);
        dateFormat = preference().getDateFormat();
        initParams();
        initMembers();
        refreshUI();
    }


    /**
     * clone a record without id
     **/
    private Record clone(Record record) {
        Record d = new Record(record.getFrom(), record.getTo(), record.getDate(), record.getMoney(), record.getNote());
        d.setArchived(record.isArchived());
        return d;
    }


    private void initParams() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(PARAM_MODE_CREATE, true);
        record = (Record) bundle.get(PARAM_RECORD);

        //issue 51, for direct call from outside action, 
        if (record == null) {
            record = new Record("", "", new Date(), 0D, "");
        }

        workingRecord = clone(record);

        if (modeCreate) {
            setTitle(R.string.title_receditor_create);
        } else {
            setTitle(R.string.title_receditor_update);
        }
    }


    private void initMembers() {

        boolean archived = workingRecord.isArchived();


        editRecordDate = findViewById(R.id.record_date);
        editRecordDate.setText(dateFormat.format(workingRecord.getDate()));
        editRecordDate.setEnabled(!archived);

        editRecordMoney = findViewById(R.id.record_money);
        editRecordMoney.setText(workingRecord.getMoney() <= 0 ? "" : Formats.double2String(workingRecord.getMoney()));
        editRecordMoney.setEnabled(!archived);

        editRecordNote = findViewById(R.id.record_note);
        editRecordNote.setText(workingRecord.getNote());

        if (!archived) {
            findViewById(R.id.btn_prev).setOnClickListener(this);
            findViewById(R.id.btn_next).setOnClickListener(this);
            findViewById(R.id.btn_today).setOnClickListener(this);
            findViewById(R.id.btn_datepicker).setOnClickListener(this);
        }
        findViewById(R.id.btn_cal2).setOnClickListener(this);

        btnOk = findViewById(R.id.btn_ok);
        if (modeCreate) {
            btnOk.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_add), 0, 0, 0);
            btnOk.setText(R.string.act_create);
        } else {
            btnOk.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_save), 0, 0, 0);
            btnOk.setText(R.string.act_update);
            editRecordMoney.requestFocus();
        }
        btnOk.setOnClickListener(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnClose = findViewById(R.id.btn_close);

        btnCancel.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        spFromAccount = findViewById(R.id.record_from_account);

        fromAccountList = new ArrayList<IndentNode>();
        fromAccountMapList = new ArrayList<Map<String, Object>>();
        fromAccountAdapter = new SimpleAdapter(this, fromAccountMapList, R.layout.simple_spinner_dropdown, accountMappingKeys, accountMappingResIds);
        fromAccountAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        fromAccountAdapter.setViewBinder(new AccountViewBinder() {
            public Account getSelectedAccount() {
                int pos = spFromAccount.getSelectedItemPosition();
                if (pos >= 0) {
                    return fromAccountList.get(pos).getAccount();
                }
                return null;
            }
        });
        spFromAccount.setAdapter(fromAccountAdapter);

        spToAccount = findViewById(R.id.record_to_account);
        toAccountList = new ArrayList<IndentNode>();
        toAccountMapList = new ArrayList<Map<String, Object>>();
        toAccountAdapter = new SimpleAdapter(this, toAccountMapList, R.layout.simple_spinner_dropdown, accountMappingKeys, accountMappingResIds);
        toAccountAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        toAccountAdapter.setViewBinder(new AccountViewBinder() {
            public Account getSelectedAccount() {
                int pos = spToAccount.getSelectedItemPosition();
                if (pos >= 0) {
                    return toAccountList.get(pos).getAccount();
                }
                return null;
            }
        });
        spToAccount.setAdapter(toAccountAdapter);

        spFromAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                IndentNode tn = fromAccountList.get(pos);
                if (tn.getAccount() != null) {
                    onFromChanged(tn.getAccount());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spToAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                IndentNode tn = toAccountList.get(pos);
                if (tn.getAccount() != null) {
                    onToChanged(tn.getAccount());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ddItemPaddingBase = 15 * GUIs.getDPRatio(RecordEditorActivity.this);
        ddSelectedBg = getResources().getDrawable(resolveThemeAttrResId(R.attr.colorControlNormal));//.mutate();
    }

    private void refreshUI() {
        refreshSpinner();
    }

    private void refreshSpinner() {
        IDataProvider idp = contexts().getDataProvider();
        // initial from
        fromAccountList.clear();
        fromAccountMapList.clear();
        for (AccountType at : AccountType.getFromType()) {
            List<Account> list = idp.listAccount(at);
            fromAccountList.addAll(AccountUtil.toIndentNode(list));
        }
        String fromAccountId = workingRecord.getFrom();
        int fromSel, firstFromSel, i;
        fromSel = firstFromSel = i = -1;
        String fromType = null;
        for (IndentNode node : fromAccountList) {
            i++;
            Map<String, Object> itemMap = new HashMap<String, Object>();
            fromAccountMapList.add(itemMap);

            //label
            itemMap.put(accountMappingKeys[0], node);

            if (node.getAccount() != null) {
                if (firstFromSel == -1) {
                    firstFromSel = i;
                }
                if (fromSel == -1 && node.getAccount().getId().equals(fromAccountId)) {
                    fromSel = i;
                    fromType = node.getAccount().getType();
                }

            }
        }

        // initial to
        toAccountList.clear();
        toAccountMapList.clear();
        for (AccountType at : AccountType.getToType(fromType)) {
            List<Account> list = idp.listAccount(at);
            toAccountList.addAll(AccountUtil.toIndentNode(list));
        }
        String toAccountId = workingRecord.getTo();
        int toSel, firstToSel;
        toSel = firstToSel = i = -1;
        // String toType = null;
        for (IndentNode node : toAccountList) {
            i++;
            Map<String, Object> itemMap = new HashMap<String, Object>();
            toAccountMapList.add(itemMap);

            //label
            itemMap.put(accountMappingKeys[0], node);

            if (node.getAccount() != null) {
                if (firstToSel == -1) {
                    firstToSel = i;
                }
                if (toSel == -1 && node.getAccount().getId().equals(toAccountId)) {
                    toSel = i;
                }

            }
        }

        if (fromSel > -1) {
            this.spFromAccount.setSelection(fromSel);
        } else if (firstFromSel > -1) {
            this.spFromAccount.setSelection(firstFromSel);
            workingRecord.setFrom(fromAccountList.get(firstFromSel).getAccount().getId());
        } else {
            workingRecord.setFrom("");
        }

        if (toSel > -1) {
            this.spToAccount.setSelection(toSel);
        } else if (firstToSel > -1) {
            this.spToAccount.setSelection(firstToSel);
            workingRecord.setTo(toAccountList.get(firstToSel).getAccount().getId());
        } else {
            workingRecord.setTo("");
        }

        fromAccountAdapter.notifyDataSetChanged();
        toAccountAdapter.notifyDataSetChanged();
    }


    private void onFromChanged(Account acc) {
        workingRecord.setFrom(acc.getId());
        refreshSpinner();
    }

    private void onToChanged(Account acc) {
        workingRecord.setTo(acc.getId());
    }

    private void updateDateEditor(Date d) {
        editRecordDate.setText(dateFormat.format(d));
    }

    @Override
    public void onClick(View v) {
        CalendarHelper cal = calendarHelper();
        if (v.getId() == R.id.btn_ok) {
            doOk();
        } else if (v.getId() == R.id.btn_cancel) {
            doCancel();
        } else if (v.getId() == R.id.btn_close) {
            doClose();
        } else if (v.getId() == R.id.btn_prev) {
            try {
                Date d = dateFormat.parse(editRecordDate.getText().toString());
                updateDateEditor(cal.yesterday(d));
            } catch (ParseException e) {
                Logger.e(e.getMessage(), e);
            }
        } else if (v.getId() == R.id.btn_next) {
            try {
                Date d = dateFormat.parse(editRecordDate.getText().toString());
                updateDateEditor(cal.tomorrow(d));
            } catch (ParseException e) {
                Logger.e(e.getMessage(), e);
            }
        } else if (v.getId() == R.id.btn_today) {
            updateDateEditor(cal.today());
        } else if (v.getId() == R.id.btn_datepicker) {
            try {
                Date d = dateFormat.parse(editRecordDate.getText().toString());
                GUIs.openDatePicker(this, d, new GUIs.OnFinishListener() {
                    @Override
                    public boolean onFinish(Object data) {
                        updateDateEditor((Date) data);
                        return true;
                    }
                });
            } catch (ParseException e) {
                Logger.e(e.getMessage(), e);
            }
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
            start = Formats.editorTextNumberDecimalToCal2(editRecordMoney.getText().toString());
        } catch (Exception x) {
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
                editRecordMoney.setText(Formats.cal2ToEditorTextNumberDecimal(result));
            } catch (Exception x) {
            }
        }
    }

    private void doOk() {

        I18N i18n = i18n();

        // verify
        int fromPos = spFromAccount.getSelectedItemPosition();
        if (Spinner.INVALID_POSITION == fromPos || fromAccountList.get(fromPos).getAccount() == null) {
            GUIs.alert(this,
                    i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_from_account)));
            return;
        }
        int toPos = spToAccount.getSelectedItemPosition();
        if (Spinner.INVALID_POSITION == toPos || toAccountList.get(toPos).getAccount() == null) {
            GUIs.alert(this,
                    i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_to_account)));
            return;
        }
        String datestr = editRecordDate.getText().toString().trim();
        if ("".equals(datestr)) {
            editRecordDate.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_date)));
            return;
        }

        Date date = null;
        try {
            date = dateFormat.parse(datestr);
        } catch (ParseException e) {
            Logger.e(e.getMessage(), e);
            GUIs.errorToast(this, e);
            return;
        }

        String moneystr = editRecordMoney.getText().toString();
        if ("".equals(moneystr)) {
            editRecordMoney.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_money)));
            return;
        }
        double money = 0;
        try {
            money = Formats.string2Double(moneystr);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        if (money <= 0) {
            GUIs.alert(this, i18n.string(R.string.msg_field_zero, i18n.string(R.string.label_money)));
            return;
        }

        String note = editRecordNote.getText().toString();

        Account fromAcc = fromAccountList.get(fromPos).getAccount();
        Account toAcc = toAccountList.get(toPos).getAccount();

        if (fromAcc.getId().equals(toAcc.getId())) {
            GUIs.alert(this, i18n.string(R.string.msg_same_from_to));
            return;
        }


        workingRecord.setFrom(fromAcc.getId());
        workingRecord.setTo(toAcc.getId());

        workingRecord.setDate(date);
        workingRecord.setMoney(money);
        workingRecord.setNote(note.trim());
        IDataProvider idp = contexts().getDataProvider();
        if (modeCreate) {

            idp.newRecord(workingRecord);
            setResult(RESULT_OK);

            workingRecord = clone(workingRecord);
            workingRecord.setMoney(0D);
            workingRecord.setNote("");
            editRecordMoney.setText("");
            editRecordMoney.requestFocus();
            editRecordNote.setText("");
            counterCreate++;
            btnOk.setText(i18n.string(R.string.act_create) + "(" + counterCreate + ")");
            btnCancel.setVisibility(Button.GONE);
            btnClose.setVisibility(Button.VISIBLE);
            trackEvent(Contexts.TRACKER_EVT_CREATE);
        } else {

            idp.updateRecord(record.getId(), workingRecord);

            GUIs.shortToast(this, i18n.string(R.string.msg_record_updated));
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
        GUIs.shortToast(this, i18n().string(R.string.msg_created_detail, counterCreate));
        finish();
    }


    abstract class AccountViewBinder implements SimpleAdapter.ViewBinder {


        abstract public Account getSelectedAccount();

        @Override
        public boolean setViewValue(View view, Object data, String text) {
            I18N i18n = i18n();

            IndentNode node = (IndentNode) data;

            if (view.getId() == accountMappingResIds[0]) {

                AccountType at = node.getType();
                TextView tv = (TextView) view;
                int textColor;
                tv.setBackgroundDrawable(null);
                if (AccountType.INCOME == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountIncomeTextColor));
                } else if (AccountType.ASSET == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountAssetTextColor));
                } else if (AccountType.EXPENSE == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountExpenseTextColor));
                } else if (AccountType.LIABILITY == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountLiabilityTextColor));
                } else if (AccountType.OTHER == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountOtherTextColor));
                } else {
                    textColor = RecordEditorActivity.this.getResources().getColor(resolveThemeAttrResId(R.attr.accountUnknownTextColor));
                }
                tv.setTextColor(textColor);


                StringBuilder display = new StringBuilder();
                if (Constants.SIMPLE_SPINNER_ITEM_TAG.equals(tv.getTag())) {
                    tv.setPadding((int) ((1 + node.getIndent()) * ddItemPaddingBase), tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());

                    if (node.getAccount() == null) {
                        if (isLightTheme()) {
                            textColor = Colors.lighten(textColor, 0.3f);
                        } else {
                            textColor = Colors.darken(textColor, 0.3f);
                        }
                        tv.setTextColor(textColor);
                    } else if (node.getAccount().equals(getSelectedAccount())) {
                        tv.setBackgroundDrawable(ddSelectedBg);
                    } else {
                        tv.setBackgroundDrawable(null);
                    }

                    if (node.getIndent() == 0) {
                        display.append(node.getType().getDisplay(i18n));
                        display.append(" - ");
                    }
                    display.append(node.getName());
                } else {
                    if (node.getAccount() == null) {
                        display.append("");
                    } else {
                        display.append(node.getType().getDisplay(i18n));
                        display.append("-");
                        display.append(node.getAccount().getName());
                    }
                }
                tv.setText(display.toString());
                return true;
            }
            return false;
        }
    }
}
