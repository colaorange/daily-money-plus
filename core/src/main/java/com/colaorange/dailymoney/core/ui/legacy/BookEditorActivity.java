package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.DataCreator;
import com.colaorange.dailymoney.core.data.DuplicateKeyException;
import com.colaorange.dailymoney.core.data.IDataProvider;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * Edit or create a book
 *
 * @author dennis
 */
public class BookEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String ARG_MODE_CREATE = "modeCreate";
    public static final String ARG_BOOK = "book";

    private boolean modeCreate;
    private Book book;
    private Book workingBook;

    private Activity activity;


    private EditText vName;
    private EditText vSymbol;
    private EditText vNote;
    private Spinner vPosition;


    private Button btnOk;
    private Button btnCancel;

    private CheckBox vCopyAccount;
    private CheckBox vDefaultAccount;


    /**
     * clone book without id
     **/
    private Book clone(Book book) {
        Book b = new Book(book.getName(), book.getSymbol(), book.getSymbolPosition(), book.getNote());
        return b;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_editor);
        initArgs();
        initMembers();
    }

    private void initArgs() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(ARG_MODE_CREATE, true);
        book = (Book) bundle.get(ARG_BOOK);

        if (modeCreate && book == null) {
            book = new Book("", "$", SymbolPosition.FRONT, "");
        }
        workingBook = clone(book);

        if (modeCreate) {
            setTitle(R.string.title_bookeditor_create);
        } else {
            setTitle(R.string.title_bookeditor_update);
        }
    }


    private void initMembers() {
        vName = findViewById(R.id.book_name);
        vName.setText(workingBook.getName());

        vSymbol = findViewById(R.id.book_symbol);
        vSymbol.setText(workingBook.getSymbol());

        //initial regular_spinner
        vPosition = findViewById(R.id.book_symbol_position);
        List<SymbolPosition> list = Collections.asList(SymbolPosition.getAvailable());

        int selpos = list.indexOf(workingBook.getSymbolPosition());
        RegularSpinnerAdapter<SymbolPosition> adapter = new RegularSpinnerAdapter<SymbolPosition>(this, list) {

            public boolean isSelected(int position) {
                return vPosition.getSelectedItemPosition() == position;
            }

            @Override
            public ViewHolder<SymbolPosition> createViewHolder() {
                return new SymbolPositionViewBinder(this);
            }
        };

        vPosition.setAdapter(adapter);
        if (selpos > -1) {
            vPosition.setSelection(selpos);
        }

        vNote = findViewById(R.id.book_note);
        vNote.setText(workingBook.getNote());

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
        btnCancel.setOnClickListener(this);

        vCopyAccount = findViewById(R.id.book_copy_account);
        vDefaultAccount = findViewById(R.id.book_default_account);
        if (!modeCreate) {
            findViewById(R.id.title_account).setVisibility(View.GONE);
            vCopyAccount.setVisibility(View.GONE);
            vDefaultAccount.setVisibility(View.GONE);
        }else{
            //exclusive eahc other when checking
            vCopyAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        vDefaultAccount.setChecked(false);
                    }
                }
            });
            vDefaultAccount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        vCopyAccount.setChecked(false);
                    }
                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            doOk();
        } else if (v.getId() == R.id.btn_cancel) {
            doCancel();
        }
    }


    private void doOk() {
        I18N i18n = i18n();

        if (Spinner.INVALID_POSITION == vPosition.getSelectedItemPosition()) {
            GUIs.shortToast(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_symbol_position)));
            return;
        }

        String name = vName.getText().toString().trim();
        if ("".equals(name)) {
            vName.requestFocus();
            GUIs.alert(this, i18n.string(R.string.msg_field_empty, i18n.string(R.string.label_name)));
            return;
        }

        SymbolPosition pos = SymbolPosition.getAvailable()[vPosition.getSelectedItemPosition()];

        //assign
        workingBook.setName(name);
        workingBook.setSymbol(vSymbol.getText().toString().trim());
        workingBook.setNote(vNote.getText().toString().trim());
        workingBook.setSymbolPosition(pos);

        IMasterDataProvider imdp = contexts().getMasterDataProvider();

        if (modeCreate) {
            imdp.newBook(workingBook);

            if (vCopyAccount.isChecked()) {
                //copy account for current book
                List<Account> currAccountList = new LinkedList<>();
                IDataProvider idp = contexts().getDataProvider();
                currAccountList.addAll(idp.listAccount(null));

                IDataProvider newidp = contexts().newDataProvider(workingBook.getId());
                try {
                    for (Account acc : currAccountList) {
                        try {
                            newidp.newAccount(acc);
                        } catch (DuplicateKeyException e) {
                            //allow it.
                            Logger.w(e.getMessage(), e);
                        }
                    }
                }finally{
                    newidp.close();
                }
            }else if(vDefaultAccount.isChecked()){
                IDataProvider newidp = contexts().newDataProvider(workingBook.getId());
                try {
                    new DataCreator(newidp, i18n()).createDefaultAccount();
                }finally{
                    newidp.close();
                }

            }

            GUIs.shortToast(this, i18n.string(R.string.msg_book_created, name));
            trackEvent(TE.CREATE_BOOK);
        } else {
            imdp.updateBook(book.getId(), workingBook);
            GUIs.shortToast(this, i18n.string(R.string.msg_book_updated, name));
            setResult(RESULT_OK);
            finish();
            trackEvent(TE.UPDDATE_BOOK);
        }
        setResult(RESULT_OK);
        finish();

    }

    private void doCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public class SymbolPositionViewBinder extends RegularSpinnerAdapter.ViewHolder<SymbolPosition> {

        public SymbolPositionViewBinder(RegularSpinnerAdapter adapter) {
            super(adapter);
        }

        @Override
        public void bindViewValue(SymbolPosition item, LinearLayout vlayout, TextView vtext, boolean isDropdown, boolean isSelected) {

            vtext.setText(item.getDisplay(i18n()));

        }
    }
}
