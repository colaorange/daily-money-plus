package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.Collections;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.colaorange.dailymoney.core.ui.RegularSpinnerAdapter;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.List;

/**
 * Edit or create a book
 *
 * @author dennis
 */
public class BookEditorActivity extends ContextsActivity implements android.view.View.OnClickListener {

    public static final String PARAM_MODE_CREATE = "modeCreate";
    public static final String PARAM_BOOK = "book";

    private boolean modeCreate;
    private Book book;
    private Book workingBook;

    Activity activity;


    EditText vName;
    EditText vSymbol;
    EditText vNote;
    Spinner vPosition;


    Button okBtn;
    Button cancelBtn;

    private float dpRatio;


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
        initParams();
        initMembers();
    }

    private void initParams() {
        Bundle bundle = getIntentExtras();
        modeCreate = bundle.getBoolean(PARAM_MODE_CREATE, true);
        book = (Book) bundle.get(PARAM_BOOK);
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
        List<SymbolPosition> data = Collections.asList(SymbolPosition.getAvailable());

        int selpos = data.indexOf(workingBook.getSymbolPosition());
        RegularSpinnerAdapter<SymbolPosition> adapter = new RegularSpinnerAdapter<SymbolPosition>(this, data) {

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
        cancelBtn.setOnClickListener(this);

        dpRatio = GUIs.getDPRatio(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            doOk();
        } else if (v.getId() == R.id.btn_cancel) {
            doCancel();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

        IMasterDataProvider idp = contexts().getMasterDataProvider();

        if (modeCreate) {
            idp.newBook(workingBook);
            GUIs.shortToast(this, i18n.string(R.string.msg_book_created, name));
            trackEvent(Contexts.TRACKER_EVT_CREATE);
        } else {
            idp.updateBook(book.getId(), workingBook);
            GUIs.shortToast(this, i18n.string(R.string.msg_book_updated, name));
            setResult(RESULT_OK);
            finish();
            trackEvent(Contexts.TRACKER_EVT_UPDATE);
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
