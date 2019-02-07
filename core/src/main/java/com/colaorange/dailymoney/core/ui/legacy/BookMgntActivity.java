package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.colaorange.commons.util.ObjectLabel;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.IMasterDataProvider;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.GUIs;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author dennis
 */
public class BookMgntActivity extends ContextsActivity {

    private List<Book> recyclerDataList;
    private BookRecyclerAdapter recyclerAdapter;

    private List<ObjectLabel<Book>> reorderDataList;
    private ObjectReorderRecyclerAdapter reorderAdapter;

    private RecyclerView vRecycler;

    private ActionMode actionMode;
    private Book actionObj;

    private boolean initOrdering;

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

        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BookRecyclerAdapter(this, recyclerDataList);

        vRecycler = findViewById(R.id.book_mgnt_recycler);
        vRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(this));

        vRecycler.setAdapter(recyclerAdapter);

        reorderDataList = new LinkedList<>();
        reorderAdapter = new ObjectReorderRecyclerAdapter(this, new ObjectReorderRecyclerAdapter.ObjectReorderCallback() {
            public void onMove(int posFrom, int posTo) {
                doMove(posFrom, posTo);
            }
        }, reorderDataList);

        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Book>() {
            @Override
            public void onSelect(Set<Book> selection) {
                doSelectBook(selection.size() == 0 ? null : selection.iterator().next());
            }

            @Override
            public boolean onReselect(Book selected) {
                doEditBook(selected);
                return true;
            }
        });

        recyclerAdapter.setOnLongClickListener(new SelectableRecyclerViewAdaptor.OnLongClickListener<Book>() {
            @Override
            public boolean onLongClick(Book pressed) {
                doSetWorkingBook(pressed, false);
                finish();
                return true;
            }
        });
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
        if (requestCode == Constants.REQUEST_BOOK_EDITOR_CODE && resultCode == Activity.RESULT_OK) {
            GUIs.delayPost(new Runnable() {
                @Override
                public void run() {
                    reloadData();
                }
            });
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                recyclerDataList.clear();


                for (Book book : data) {
                    recyclerDataList.add(book);
                    reorderAdapter.add(new ObjectLabel(book, book.getName()));
                }

                recyclerAdapter.notifyDataSetChanged();
                reorderAdapter.notifyDataSetChanged();
            }
        });
    }


    public void doNewBook() {
        Intent intent = null;
        intent = new Intent(this, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.ARG_MODE_CREATE, true);
        startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }


    public void doEditBook(Book book) {
        Intent intent = null;
        intent = new Intent(this, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.ARG_MODE_CREATE, false);
        intent.putExtra(BookEditorActivity.ARG_BOOK, book);
        startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }

    public void doDeleteBook(final Book book) {
        final int workingBookId = Contexts.instance().getWorkingBookId();
        final I18N i18n = Contexts.instance().getI18n();
        if (book.getId() == Contexts.DEFAULT_BOOK_ID) {
            //default book
            GUIs.shortToast(this, R.string.msg_cannot_delete_default_book);
            return;
        } else if (workingBookId == book.getId()) {
            //
            GUIs.shortToast(this, R.string.msg_cannot_delete_working_book);
            return;
        }
        GUIs.OnFinishListener l = new GUIs.OnFinishListener() {
            public boolean onFinish(int which, Object data) {
                if (which == GUIs.OK_BUTTON) {
                    int bookid = book.getId();
                    boolean r = Contexts.instance().getMasterDataProvider().deleteBook(bookid);
                    if (r) {
                        if (book.equals(actionObj)) {
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }
                        GUIs.shortToast(BookMgntActivity.this, i18n().string(R.string.msg_book_deleted, book.getName()));
                        reloadData();
                        trackEvent(TE.DELETE_BOOK);

                        Contexts ctxs = Contexts.instance();
                        ctxs.getPreference().clearRecordTemplates(bookid);
                        ctxs.deleteData(book);
                    }
                }
                return true;
            }
        };
        GUIs.confirm(this, i18n.string(R.string.qmsg_delete_book, book.getName()), l);
    }

    public void doSetWorkingBook(Book book, boolean reload) {
        if (Contexts.instance().getWorkingBookId() == book.getId()) {
            return;
        }
        Contexts.instance().setWorkingBookId(book.getId());
        if (reload) {
            reloadData();
        } else {
            //since working book was changed
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    public void clearSelection() {
        recyclerAdapter.clearSelection();
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
            doNewBook();
            return true;
        } else if (item.getItemId() == R.id.menu_reorder) {
            doReorderBook();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doReorderBook() {
        if (actionMode != null) {
            actionMode.invalidate();
            return;
        }

        actionMode = this.startSupportActionMode(new ReorderActionModeCallback());
        actionMode.setTitle(i18n().string(R.string.act_reorder));

        vRecycler.setAdapter(reorderAdapter);

        reorderAdapter.attachToRecyclerView(vRecycler);
    }


    private void doMove(int posFrom, int posTo) {
//        Preference preference = Contexts.instance().getPreference();
//
//        trackEvent(Contexts.TE.CHART+"move");
//
//        CardDesktop desktop = preference.getDesktop(desktopIndex);
//        if (pos >= desktop.size()) {
//            return;
//        }
//        desktop.move(pos, posTo);
//        preference.updateDesktop(desktopIndex, desktop);
//
        IMasterDataProvider idp = contexts().getMasterDataProvider();

        if (!initOrdering) {

            int s = recyclerDataList.size();
            for (int i = 0; i < s; i++) {
                Book book = recyclerDataList.get(i);
                if (book.getPriority() != i) {
                    book.setPriority(i);
                    idp.updateBook(book.getId(), book);
                }
            }
            
            initOrdering = true;
        }

        Book bookFrom = recyclerDataList.get(posFrom);
        Book bookTo = recyclerDataList.get(posTo);
        recyclerDataList.set(posFrom, bookTo);
        recyclerDataList.set(posTo, bookFrom);

        bookTo.setPriority(posFrom);
        bookFrom.setPriority(posTo);

        idp.updateBook(bookFrom.getId(), bookFrom);
        idp.updateBook(bookTo.getId(), bookTo);


        ObjectLabel objFrom = reorderDataList.get(posFrom);
        ObjectLabel objTo = reorderDataList.get(posTo);
        reorderDataList.set(posFrom, objTo);
        reorderDataList.set(posTo, objFrom);

        reorderAdapter.notifyItemMoved(posFrom, posTo);
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
                doEditBook(actionObj);
                return true;
            } else if (item.getItemId() == R.id.menu_delete) {
                doDeleteBook(actionObj);
//                mode.finish();//Finish action mode
                return true;
            } else if (item.getItemId() == R.id.menu_set_working) {
                doSetWorkingBook(actionObj, true);
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
            clearSelection();
        }
    }


    private class ReorderActionModeCallback implements android.support.v7.view.ActionMode.Callback {

        //onCreateActionMode(ActionMode, Menu) once on initial creation.
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.empty_menu, menu);//Inflate the menu over action mode
            return true;
        }

        //onPrepareActionMode(ActionMode, Menu) after creation and any time the ActionMode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        //onActionItemClicked(ActionMode, MenuItem) any time a contextual action button is clicked.
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return true;
        }

        //onDestroyActionMode(ActionMode) when the action mode is closed.
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //When action mode destroyed remove selected selections and set action mode to null
            //First check current fragment action mode
            actionMode = null;
            actionObj = null;
            clearSelection();

            vRecycler.setAdapter(recyclerAdapter);
            reorderAdapter.detachFromRecyclerView();
        }
    }
}
