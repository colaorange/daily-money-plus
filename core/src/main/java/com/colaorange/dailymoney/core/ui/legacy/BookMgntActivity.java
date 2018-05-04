package com.colaorange.dailymoney.core.ui.legacy;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * 
 * @author dennis
 * 
 */
public class BookMgntActivity extends ContextsActivity {
    
    BookListHelper bookListHelper;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_mgnt);
        initParams();
        initMembers();
        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        },25);
    }
    

    private void initParams() {

    }


    private void initMembers() {
        
        
        bookListHelper = new BookListHelper(this, true, new BookListHelper.OnBookListener() {
            @Override
            public void onBookDeleted(Book book) {
                GUIs.shortToast(BookMgntActivity.this, i18n().string(R.string.msg_book_deleted,book.getName()));
                reloadData();
                trackEvent(TE.DELETE_BOOK);
            }
        });
        
        ListView listView = findViewById(R.id.book_mgnt_list);
        bookListHelper.setup(listView);
        
        registerForContextMenu(listView);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQUEST_BOOK_EDITOR_CODE && resultCode==Activity.RESULT_OK){
            GUIs.delayPost(new Runnable(){
                @Override
                public void run() {
                    reloadData();
                }});
        }
    }


    private void reloadData() {
        final IMasterDataProvider idp = contexts().getMasterDataProvider();
        GUIs.doBusy(this,new GUIs.BusyAdapter() {
            List<Book> data = null;
            
            @Override
            public void run() {
                data = idp.listAllBook();
            }
            @Override
            public void onBusyFinish() {
              //update data
                bookListHelper.reloadData(data);
            }
        });
        
        
    }
    
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.book_mgnt_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            bookListHelper.doNewBook();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.book_mgnt_list) {
            getMenuInflater().inflate(R.menu.book_mgnt_ctxmenu, menu);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.menu_edit) {
            bookListHelper.doEditBook(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            bookListHelper.doDeleteBook(info.position);
            return true;
        } else if (item.getItemId() == R.id.menu_set_working) {
            bookListHelper.doSetWorkingBook(info.position);
            finish();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
