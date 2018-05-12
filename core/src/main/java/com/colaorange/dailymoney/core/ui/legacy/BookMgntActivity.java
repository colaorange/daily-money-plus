package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.util.GUIs;

import java.util.List;

/**
 * @author dennis
 */
public class BookMgntActivity extends ContextsActivity {

    BookRecyclerHelper bookRecyclerHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_mgnt);
        initArgs();
        initMembers();
        enableAppbarHideOnScroll(false);

        GUIs.delayPost(new Runnable() {
            @Override
            public void run() {
                reloadData();
            }
        }, 25);
    }


    private void initArgs() {

    }


    private void initMembers() {

        bookRecyclerHelper = new BookRecyclerHelper(this, true, new BookRecyclerHelper.OnBookListener() {
            @Override
            public void onBookDeleted(Book book) {
                GUIs.shortToast(BookMgntActivity.this, i18n().string(R.string.msg_book_deleted, book.getName()));
                reloadData();
                trackEvent(TE.DELETE_BOOK);
            }
        });

        RecyclerView vrecycler = findViewById(R.id.book_mgnt_recycler);
        bookRecyclerHelper.setup(vrecycler);

//        registerForContextMenu(vrecycler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_BOOK_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
        }
    }


    private void reloadData() {
        final IMasterDataProvider idp = contexts().getMasterDataProvider();
        GUIs.doBusy(this, new GUIs.BusyAdapter() {
            List<Book> data = null;

            @Override
            public void run() {
                data = idp.listAllBook();
            }

            @Override
            public void onBusyFinish() {
                //update data
                bookRecyclerHelper.reloadData(data);
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
            bookRecyclerHelper.doNewBook();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        if (v.getId() == R.id.book_mgnt_list) {
//            getMenuInflater().inflate(R.menu.book_mgnt_ctxmenu, menu);
//        }
//
//    }

//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//        if (item.getItemId() == R.id.menu_edit) {
//            bookRecyclerHelper.doEditBook(info.position);
//            return true;
//        } else if (item.getItemId() == R.id.menu_delete) {
//            bookRecyclerHelper.doDeleteBook(info.position);
//            return true;
//        } else if (item.getItemId() == R.id.menu_set_working) {
//            bookRecyclerHelper.doSetWorkingBook(info.position);
//            finish();
//            return true;
//        }
//        return super.onContextItemSelected(item);
//    }
}
