package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
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

    private ActionMode actionMode;
    private Book actionObj;

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

        bookRecyclerHelper = new BookRecyclerHelper(this, new BookRecyclerHelper.OnBookListener() {
            @Override
            public void onSelectBook(Book book) {
                doSelectBook(book);
            }

            @Override
            public void onDeleteBook(Book book) {
                doDeleteBook(book);
            }
        });

        RecyclerView vrecycler = findViewById(R.id.book_mgnt_recycler);
        bookRecyclerHelper.setup(vrecycler);

//        registerForContextMenu(vrecycler);
    }

    private void doDeleteBook(Book book) {
        if (book.equals(actionObj)) {
            if (actionMode != null) {
                actionMode.finish();
            }
        }
        GUIs.shortToast(BookMgntActivity.this, i18n().string(R.string.msg_book_deleted, book.getName()));
        reloadData();
        trackEvent(TE.DELETE_BOOK);
    }

    private void doSelectBook(Book book) {
        if (book == null && actionMode != null) {
            actionMode.finish();
            return;
        }

        if (book != null) {
            actionObj = book;
            if (actionMode == null) {
                actionMode = this.startSupportActionMode(new BookActionModeCallback());
            } else {
                actionMode.invalidate();
            }
            actionMode.setTitle(book.getName());
        }

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

    private class BookActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.book_mgnt_item_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

            int workingBookId = contexts().getWorkingBookId();

            //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
            //So here show action menu according to SDK Levels
            MenuItem mi = menu.findItem(R.id.menu_set_working);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (workingBookId == actionObj.getId()) {
                mi.setEnabled(false);
            } else {
                mi.setEnabled(true);
            }
            mi.setIcon(buildDisabledIcon(resolveThemeAttrResId(R.attr.ic_set_working), mi.isEnabled()));


            mi = menu.findItem(R.id.menu_edit);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            mi = menu.findItem(R.id.menu_delete);
            mi.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            if (workingBookId == actionObj.getId() || Contexts.DEFAULT_BOOK_ID == actionObj.getId()) {
                mi.setEnabled(false);
            } else {
                mi.setEnabled(true);
            }
            mi.setIcon(buildDisabledIcon(resolveThemeAttrResId(R.attr.ic_delete_forever), mi.isEnabled()));

            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.menu_edit) {
                bookRecyclerHelper.doEditBook(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                bookRecyclerHelper.doDeleteBook(actionObj);
//                mode.finish();//Finish action mode
                return true;
            } else if (item.getItemId() == R.id.menu_set_working) {
                bookRecyclerHelper.doSetWorkingBook(actionObj);
                mode.invalidate();
                return true;
            }
            return false;
        }

        //onDestroyActionMode(ActionMode) when the action mode is closed.
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            actionMode = null;
            actionObj = null;
            bookRecyclerHelper.clearSelection();
        }


    }
}
