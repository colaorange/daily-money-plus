package com.colaorange.dailymoney.ui.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.data.Book;
import com.colaorange.dailymoney.data.SymbolPosition;
import com.colaorange.dailymoney.ui.Constants;

/**
 * @author dennis
 */
public class BookListHelper implements OnItemClickListener {

    private static String[] bindingFrom = new String[]{"working_book", "id", "name", "symbol", "note"};

    private static int[] bindingTo = new int[]{R.id.book_mgnt_item_icon, R.id.book_mgnt_item_id, R.id.book_mgnt_item_name,
            R.id.book_mgnt_item_symbol, R.id.book_mgnt_item_note};


    private List<Book> listViewData = new ArrayList<Book>();

    private List<Map<String, Object>> listViewMapList = new ArrayList<Map<String, Object>>();

    private ListView listView;

    private SimpleAdapter listViewAdapter;

    private boolean clickeditable;

    private OnBookListener listener;

    private Activity activity;

    public BookListHelper(Activity activity, boolean clickeditable, OnBookListener listener) {
        this.activity = activity;
        this.clickeditable = clickeditable;
        this.listener = listener;
    }


    public void setup(ListView listview) {

        int layout = R.layout.book_mgnt_item;

        listViewAdapter = new SimpleAdapter(activity, listViewMapList, layout, bindingFrom, bindingTo);
        listViewAdapter.setViewBinder(new ListViewBinder());

        listView = listview;
        listView.setAdapter(listViewAdapter);
        if (clickeditable) {
            listView.setOnItemClickListener(this);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == listView) {
            doEditBook(pos);
        }
    }

    public void reloadData(List<Book> data) {
        listViewData = data;
        listViewMapList.clear();
        int workingBookId = Contexts.instance().getWorkingBookId();
        for (Book book : listViewData) {
            Map<String, Object> row = toBookMap(book, workingBookId);
            listViewMapList.add(row);
        }
        listViewAdapter.notifyDataSetChanged();
    }

    private Map<String, Object> toBookMap(Book book, int selectId) {
        Map<String, Object> row = new HashMap<String, Object>();

        Boolean selected = book.getId() == selectId;
        String id = String.valueOf(book.getId());
        String name = book.getName();
        String symbol = book.getSymbol();
        String note = book.getNote();

        row.put(bindingFrom[0], new NamedItem(bindingFrom[0], selected));
        row.put(bindingFrom[1], new NamedItem(bindingFrom[1], id));
        row.put(bindingFrom[2], new NamedItem(bindingFrom[2], name));
        row.put(bindingFrom[3], new NamedItem(bindingFrom[3], symbol));
        row.put(bindingFrom[4], new NamedItem(bindingFrom[4], note));
        return row;
    }

    public void doNewBook() {
        Book book = new Book("", "$", SymbolPosition.FRONT, "");
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, true);
        intent.putExtra(BookEditorActivity.PARAM_BOOK, book);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }


    public void doEditBook(int pos) {
        Book book = listViewData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(BookEditorActivity.PARAM_BOOK, book);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }

    public void doDeleteBook(final int pos) {
        final Book book = listViewData.get(pos);
        final int workingBookId = Contexts.instance().getWorkingBookId();
        final I18N i18n = Contexts.instance().getI18n();
        if (book.getId() == 0) {
            //default book
            GUIs.shortToast(activity, R.string.msg_cannot_delete_default_book);
            return;
        } else if (workingBookId == book.getId()) {
            //
            GUIs.shortToast(activity, R.string.msg_cannot_delete_working_book);
            return;
        }
        GUIs.confirm(activity, i18n.string(R.string.qmsg_delete_book, book.getName()), new GUIs.OnFinishListener() {
            public boolean onFinish(Object data) {
                if (((Integer) data).intValue() == GUIs.OK_BUTTON) {
                    boolean r = Contexts.instance().getMasterDataProvider().deleteBook(book.getId());
                    if (r) {
                        if (listener != null) {
                            listener.onBookDeleted(book);
                        } else {
                            listViewData.remove(pos);
                            listViewMapList.remove(pos);
                            listViewAdapter.notifyDataSetChanged();
                        }
                        Contexts.instance().deleteData(book);
                    }
                }
                return true;
            }
        });
    }

    public void doSetWorkingBook(int pos) {
        Book d = listViewData.get(pos);
        if (Contexts.instance().getWorkingBookId() == d.getId()) {
            return;
        }
        Contexts.instance().setWorkingBookId(d.getId());
        reloadData(listViewData);
    }


    public interface OnBookListener {
        void onBookDeleted(Book detail);
    }

    class ListViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data, String text) {
            NamedItem item = (NamedItem) data;
            String name = item.getName();
            if ("working_book".equals(name)) {
                ImageView layout = (ImageView) view;
                Boolean selected = (Boolean) item.getValue();
                if (selected.booleanValue()) {
                    layout.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_active));
                } else {
                    layout.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_notactive));
                }
                return true;
            }

            return false;
        }
    }

}
