package com.colaorange.dailymoney.core.ui.legacy;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Formats;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.calculator2.Calculator;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Detail;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.legacy.AccountUtil.IndentNode;

/**
 * Edit or create a record
 *
 * @author dennis
 */
public class RecordEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String PARAM_MODE_CREATE = "dteditor.modeCreate";
    public static final String PARAM_DETAIL = "dteditor.record";


    private boolean modeCreate;
    private int counterCreate;
    private Detail record;
    private Detail workingDetail;

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

    Spinner fromAccount;
    Spinner toAccount;

    EditText recordDate;
    EditText recordNote;
    EditText recordMoney;

    Button btnOk;
    Button btnCancel;
    Button btnClose;

    private float ddPaddingIntentBase;
    private Drawable ddSelected;


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
    private Detail clone(Detail detail) {
        Detail d = new Detail(detail.getFrom(), detail.getTo(), detail.getDate(), detail.getMoney(), detail.getNote());
        d.setArchived(detail.isArchived());
        return d;
    }


    private void initParams() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(PARAM_MODE_CREATE, true);
        record = (Detail) bundle.get(PARAM_DETAIL);

        //issue 51, for direct call from outside action, 
        if (record == null) {
            record = new Detail("", "", new Date(), 0D, "");
        }

        workingDetail = clone(record);

        if (modeCreate) {
            setTitle(R.string.title_deteditor_create);
        } else {
            setTitle(R.string.title_deteditor_update);
        }
    }


    private void initMembers() {

        boolean archived = workingDetail.isArchived();


        recordDate = findViewById(R.id.record_date);
        recordDate.setText(dateFormat.format(workingDetail.getDate()));
        recordDate.setEnabled(!archived);

        recordMoney = findViewById(R.id.record_money);
        recordMoney.setText(workingDetail.getMoney() <= 0 ? "" : Formats.double2String(workingDetail.getMoney()));
        recordMoney.setEnabled(!archived);

        recordNote = findViewById(R.id.record_note);
        recordNote.setText(workingDetail.getNote());

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
            btnOk.setText(R.string.cact_create);
        } else {
            btnOk.setCompoundDrawablesWithIntrinsicBounds(resolveThemeAttrResId(R.attr.ic_save), 0, 0, 0);
            btnOk.setText(R.string.cact_update);
            recordMoney.requestFocus();
        }
        btnOk.setOnClickListener(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnClose = findViewById(R.id.btn_close);

        btnCancel.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        fromAccount = findViewById(R.id.record_from_account);

        fromAccountList = new ArrayList<IndentNode>();
        fromAccountMapList = new ArrayList<Map<String, Object>>();
        fromAccountAdapter = new SimpleAdapter(this, fromAccountMapList, R.layout.simple_spinner_dropdown_item, accountMappingKeys, accountMappingResIds);
        fromAccountAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        fromAccountAdapter.setViewBinder(new AccountViewBinder() {
            public Account getSelectedAccount() {
                int pos = fromAccount.getSelectedItemPosition();
                if (pos >= 0) {
                    return fromAccountList.get(pos).getAccount();
                }
                return null;
            }
        });
        fromAccount.setAdapter(fromAccountAdapter);

        toAccount = findViewById(R.id.record_to_account);
        toAccountList = new ArrayList<IndentNode>();
        toAccountMapList = new ArrayList<Map<String, Object>>();
        toAccountAdapter = new SimpleAdapter(this, toAccountMapList, R.layout.simple_spinner_dropdown_item, accountMappingKeys, accountMappingResIds);
        toAccountAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        toAccountAdapter.setViewBinder(new AccountViewBinder() {
            public Account getSelectedAccount() {
                int pos = toAccount.getSelectedItemPosition();
                if (pos >= 0) {
                    return toAccountList.get(pos).getAccount();
                }
                return null;
            }
        });
        toAccount.setAdapter(toAccountAdapter);

        fromAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        toAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        ddPaddingIntentBase = 15 * GUIs.getDPRatio(RecordEditorActivity.this);
//                ddDisabled = RecordEditorActivity.this.getResources().getDrawable(android.R.color.darker_gray).mutate();
//                ddDisabled.setAlpha(32);
        ddSelected = RecordEditorActivity.this.getResources().getDrawable(android.R.color.darker_gray).mutate();
        ddSelected.setAlpha(128);
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
        String fromAccountId = workingDetail.getFrom();
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
        String toAccountId = workingDetail.getTo();
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
            this.fromAccount.setSelection(fromSel);
        } else if (firstFromSel > -1) {
            this.fromAccount.setSelection(firstFromSel);
            workingDetail.setFrom(fromAccountList.get(firstFromSel).getAccount().getId());
        } else {
            workingDetail.setFrom("");
        }

        if (toSel > -1) {
            this.toAccount.setSelection(toSel);
        } else if (firstToSel > -1) {
            this.toAccount.setSelection(firstToSel);
            workingDetail.setTo(toAccountList.get(firstToSel).getAccount().getId());
        } else {
            workingDetail.setTo("");
        }

        fromAccountAdapter.notifyDataSetChanged();
        toAccountAdapter.notifyDataSetChanged();
    }


    private void onFromChanged(Account acc) {
        workingDetail.setFrom(acc.getId());
        refreshSpinner();
    }

    private void onToChanged(Account acc) {
        workingDetail.setTo(acc.getId());
    }

    private void updateDateEditor(Date d) {
        recordDate.setText(dateFormat.format(d));
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
                Date d = dateFormat.parse(recordDate.getText().toString());
                updateDateEditor(cal.yesterday(d));
            } catch (ParseException e) {
                Logger.e(e.getMessage(), e);
            }
        } else if (v.getId() == R.id.btn_next) {
            try {
                Date d = dateFormat.parse(recordDate.getText().toString());
                updateDateEditor(cal.tomorrow(d));
            } catch (ParseException e) {
                Logger.e(e.getMessage(), e);
            }
        } else if (v.getId() == R.id.btn_today) {
            updateDateEditor(cal.today());
        } else if (v.getId() == R.id.btn_datepicker) {
            try {
                Date d = dateFormat.parse(recordDate.getText().toString());
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

        String start = "";
        try {
            start = Formats.editorTextNumberDecimalToCal2(recordMoney.getText().toString());
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
                recordMoney.setText(Formats.cal2ToEditorTextNumberDecimal(result));
            } catch (Exception x) {
            }
        }
    }

    private void doOk() {

        I18N i18n = i18n();

        // verify
        int fromPos = fromAccount.getSelectedItemPosition();
        if (Spinner.INVALID_POSITION == fromPos || fromAccountList.get(fromPos).getAccount() == null) {
            GUIs.alert(this,
                    i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.label_from_account)));
            return;
        }
        int toPos = toAccount.getSelectedItemPosition();
        if (Spinner.INVALID_POSITION == toPos || toAccountList.get(toPos).getAccount() == null) {
            GUIs.alert(this,
                    i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.label_to_account)));
            return;
        }
        String datestr = recordDate.getText().toString().trim();
        if ("".equals(datestr)) {
            recordDate.requestFocus();
            GUIs.alert(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.label_date)));
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

        String moneystr = recordMoney.getText().toString();
        if ("".equals(moneystr)) {
            recordMoney.requestFocus();
            GUIs.alert(this, i18n.string(R.string.cmsg_field_empty, i18n.string(R.string.label_money)));
            return;
        }
        double money = 0;
        try {
            money = Formats.string2Double(moneystr);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        if (money <= 0) {
            GUIs.alert(this, i18n.string(R.string.cmsg_field_zero, i18n.string(R.string.label_money)));
            return;
        }

        String note = recordNote.getText().toString();

        Account fromAcc = fromAccountList.get(fromPos).getAccount();
        Account toAcc = toAccountList.get(toPos).getAccount();

        if (fromAcc.getId().equals(toAcc.getId())) {
            GUIs.alert(this, i18n.string(R.string.msg_same_from_to));
            return;
        }


        workingDetail.setFrom(fromAcc.getId());
        workingDetail.setTo(toAcc.getId());

        workingDetail.setDate(date);
        workingDetail.setMoney(money);
        workingDetail.setNote(note.trim());
        IDataProvider idp = contexts().getDataProvider();
        if (modeCreate) {

            idp.newDetail(workingDetail);
            setResult(RESULT_OK);

            workingDetail = clone(workingDetail);
            workingDetail.setMoney(0D);
            workingDetail.setNote("");
            recordMoney.setText("");
            recordMoney.requestFocus();
            recordNote.setText("");
            counterCreate++;
            btnOk.setText(i18n.string(R.string.cact_create) + "(" + counterCreate + ")");
            btnCancel.setVisibility(Button.GONE);
            btnClose.setVisibility(Button.VISIBLE);
            trackEvent(Contexts.TRACKER_EVT_CREATE);
        } else {

            idp.updateDetail(record.getId(), workingDetail);

            GUIs.shortToast(this, i18n.string(R.string.msg_detail_updated));
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
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.income_fgl);
                } else if (AccountType.ASSET == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.asset_fgl);
                } else if (AccountType.EXPENSE == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.expense_fgl);
                } else if (AccountType.LIABILITY == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.liability_fgl);
                } else if (AccountType.OTHER == at) {
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.other_fgl);
                } else {
                    textColor = RecordEditorActivity.this.getResources().getColor(R.color.unknow_fgl);
                }
                tv.setTextColor(textColor);

                StringBuilder display = new StringBuilder();
                if (Constants.SIMPLE_SPINNER_ITEM_TAG.equals(tv.getTag())) {
                    tv.setPadding((int) (tv.getPaddingLeft() + node.getIndent() * ddPaddingIntentBase), tv.getPaddingTop(), tv.getPaddingRight(), tv.getPaddingBottom());
                    if (node.getAccount() == null) {
                        tv.setTextColor(textColor & 0x6FFFFFFF);
                    } else if (node.getAccount() == getSelectedAccount()) {
                        tv.setBackgroundDrawable(ddSelected);
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
