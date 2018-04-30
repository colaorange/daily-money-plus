package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.data.SymbolPosition;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * @author dennis
 */
public class BookListHelper implements OnItemClickListener {


    private List<Book> listData = new ArrayList<Book>();


    private ListView vList;

    private BookListAdapter listAdapter;

    private boolean clickEditable;

    private OnBookListener listener;

    private Activity activity;

    private int workingBookId;

    public BookListHelper(Activity activity, boolean clickEditable, OnBookListener listener) {
        this.activity = activity;
        this.clickEditable = clickEditable;
        this.listener = listener;
    }


    public void setup(ListView listview) {
        workingBookId = Contexts.instance().getWorkingBookId();
        listData = new LinkedList<>();
        listAdapter = new BookListAdapter(activity, listData);
        vList = listview;
        vList.setAdapter(listAdapter);
        if (clickEditable) {
            vList.setOnItemClickListener(this);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == vList) {
            doEditBook(pos);
        }
    }

    public void reloadData(List<Book> data) {
        if(listData !=data) {//not self call
            listData.clear();
            listData.addAll(data);
        }

        workingBookId = Contexts.instance().getWorkingBookId();
        listAdapter.notifyDataSetChanged();
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
        Book book = listData.get(pos);
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(BookEditorActivity.PARAM_BOOK, book);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }

    public void doDeleteBook(final int pos) {
        final Book book = listData.get(pos);
        final int workingBookId = Contexts.instance().getWorkingBookId();
        final I18N i18n = Contexts.instance().getI18n();
        if (book.getId() == Contexts.DEFAULT_BOOK_ID) {
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
                            listData.remove(pos);
                            listAdapter.notifyDataSetChanged();
                        }
                        Contexts.instance().deleteData(book);
                    }
                }
                return true;
            }
        });
    }

    public void doSetWorkingBook(int pos) {
        Book d = listData.get(pos);
        if (Contexts.instance().getWorkingBookId() == d.getId()) {
            return;
        }
        Contexts.instance().setWorkingBookId(d.getId());

        reloadData(listData);
    }


    public interface OnBookListener {
        void onBookDeleted(Book detail);
    }



    private class BookListAdapter extends ArrayAdapter<Book> {

        LayoutInflater inflater;

        public BookListAdapter(@NonNull Context context, List<Book> list) {
            super(context, R.layout.book_mgnt_item, list);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BookViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.book_mgnt_item, null);
                convertView.setTag(holder = new BookViewHolder());
            } else {
                holder = (BookViewHolder) convertView.getTag();
            }

            holder.bindViewValue(getItem(position), convertView);

            return convertView;
        }


    }

    private class BookViewHolder {

        public void bindViewValue(Book book, View convertView) {

            ImageView vicon = convertView.findViewById(R.id.book_mgnt_item_icon);
            TextView vname = convertView.findViewById(R.id.book_mgnt_item_name);
            TextView vid = convertView.findViewById(R.id.book_mgnt_item_id);
            TextView vnote = convertView.findViewById(R.id.book_mgnt_item_note);
            TextView vsymbol = convertView.findViewById(R.id.book_mgnt_item_symbol);

            vname.setText(book.getName());
            vid.setText(Integer.toString(book.getId()));
            vnote.setText(book.getNote());
            vsymbol.setText(book.getSymbol());

            if(book.getId()==workingBookId){
                vicon.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_active));
            } else {
                vicon.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_notactive));
            }
        }
    }

}
