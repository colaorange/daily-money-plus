package com.colaorange.dailymoney.ui.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.colaorange.commons.util.GUIs;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.ContextsActivity;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.Book;
import com.colaorange.dailymoney.data.IMasterDataProvider;
import com.colaorange.dailymoney.data.SymbolPosition;
import com.colaorange.dailymoney.ui.Constants;

/**
 * Edit or create a book
 * @author dennis
 *
 */
public class BookEditorActivity extends ContextsActivity implements android.view.View.OnClickListener{

    public static final String PARAM_MODE_CREATE = "bkeditor.modeCreate";
    public static final String PARAM_BOOK = "bkeditor.book";
        
    private boolean modeCreate;
    private Book book;
    private Book workingBook;

    Activity activity;
    
    
    /** clone book without id **/
    private Book clone(Book book){
        Book b = new Book(book.getName(),book.getSymbol(),book.getSymbolPosition(),book.getNote());
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
        modeCreate = bundle.getBoolean(PARAM_MODE_CREATE,true);
        book = (Book)bundle.get(PARAM_BOOK);
        workingBook = clone(book);
        
        if(modeCreate){
            setTitle(R.string.title_bookeditor_create);
        }else{
            setTitle(R.string.title_bookeditor_update);
        }
    }

    /** need to mapping twice to do different mapping in spitem and spdropdown item*/
    private static String[] spfrom = new String[] { Constants.DISPLAY,Constants.DISPLAY};
    private static int[] spto = new int[] { R.id.simple_spinner_item_display, R.id.simple_spinner_dropdown_item_display};
    
    EditText nameEditor;
    EditText symbolEditor;
    EditText noteEditor;
    Spinner positionEditor;
    
    
    Button okBtn;
    Button cancelBtn;
    
    private void initMembers() {
        nameEditor = findViewById(R.id.book_editor_name);
        nameEditor.setText(workingBook.getName());
        
        symbolEditor = findViewById(R.id.book_editor_symbol);
        symbolEditor.setText(workingBook.getSymbol());
        
      //initial spinner
        positionEditor = findViewById(R.id.book_editor_symbol_position);
        List<Map<String, Object>> data = new  ArrayList<Map<String, Object>>();
        SymbolPosition symbolPos = workingBook.getSymbolPosition();
        int selpos,i;
        selpos = i = -1;
        for (SymbolPosition sp : SymbolPosition.getAvailable()) {
            i++;
            Map<String, Object> row = new HashMap<String, Object>();
            data.add(row);
            row.put(spfrom[0], new NamedItem(spfrom[0],sp,sp.getDisplay(i18n)));
            
            if(sp.equals(symbolPos)){
                selpos = i;
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.simple_spinner_dropdown_item, spfrom, spto);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        adapter.setViewBinder(new SymbolPositionViewBinder());
        positionEditor.setAdapter(adapter);
        if(selpos>-1){
            positionEditor.setSelection(selpos);
        }
        
        noteEditor = findViewById(R.id.book_editor_note);
        noteEditor.setText(workingBook.getNote());
        
        okBtn = findViewById(R.id.btn_ok);
        if(modeCreate){
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_white_24dp,0,0,0);
            okBtn.setText(R.string.cact_create);
        }else{
            okBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_white_24dp,0,0,0);
            okBtn.setText(R.string.cact_update);
        }
        okBtn.setOnClickListener(this);
        
        
        cancelBtn = findViewById(R.id.btn_cancel);

        
        cancelBtn.setOnClickListener(this);
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
    private void doOk(){   
        //verify
      //verify
        if(Spinner.INVALID_POSITION==positionEditor.getSelectedItemPosition()){
            GUIs.shortToast(this,i18n.string(R.string.cmsg_field_empty,i18n.string(R.string.label_symbol_position)));
            return;
        }
        
        String name = nameEditor.getText().toString().trim();
        if("".equals(name)){
            nameEditor.requestFocus();
            GUIs.alert(this,i18n.string(R.string.cmsg_field_empty,i18n.string(R.string.clabel_name)));
            return;
        }

        SymbolPosition pos = SymbolPosition.getAvailable()[positionEditor.getSelectedItemPosition()];
        
        //assign
        workingBook.setName(name);
        workingBook.setSymbol(symbolEditor.getText().toString().trim());
        workingBook.setNote(noteEditor.getText().toString().trim());
        workingBook.setSymbolPosition(pos);
        
        IMasterDataProvider idp = contexts().getMasterDataProvider();

        if (modeCreate) {
            idp.newBook(workingBook);
            GUIs.shortToast(this, i18n.string(R.string.msg_book_created, name));
            trackEvent(Contexts.TRACKER_EVT_CREATE);
        } else {
            idp.updateBook(book.getId(),workingBook);
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
    
    class SymbolPositionViewBinder implements SimpleAdapter.ViewBinder{
        @Override
        public boolean setViewValue(View view, Object data, String text) {
            
            NamedItem item = (NamedItem)data;
            String name = item.getName();
            if(!(view instanceof TextView)){
               return false;
            }
            if(Constants.DISPLAY.equals(name)){
                TextView tv = (TextView)view;
                tv.setTextColor(getResources().getColor(R.color.symbolpos_fgl));
                tv.setText(item.getToString());
                return true;
            }
            return false;
        }
    }
}
