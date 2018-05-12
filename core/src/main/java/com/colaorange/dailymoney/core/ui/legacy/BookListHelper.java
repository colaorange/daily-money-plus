package com.colaorange.dailymoney.core.ui.legacy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.ui.Constants;

/**
 * @author dennis
 */
public class BookListHelper /*implements OnItemClickListener */ {


    private List<Book> recyclerDataList;


    private RecyclerView vRecycler;

    private BookRecyclerAdapter recyclerAdapter;

    private boolean clickEditable;

    private OnBookListener listener;

    private ContextsActivity activity;

    private int workingBookId;

    LayoutInflater inflater;

    public BookListHelper(ContextsActivity activity, boolean clickEditable, OnBookListener listener) {
        this.activity = activity;
        this.clickEditable = clickEditable;
        this.listener = listener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setup(RecyclerView vRecycler) {
        workingBookId = Contexts.instance().getWorkingBookId();
        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BookRecyclerAdapter(recyclerDataList);
        this.vRecycler = vRecycler;
        this.vRecycler.setAdapter(recyclerAdapter);
//        if (clickEditable) {
//            vRecycler.setOnItemClickListener(this);
//        }
    }


//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//        if (parent == vRecycler) {
//            doEditBook(pos);
//        }
//    }

    public void reloadData(List<Book> data) {
        if (recyclerDataList != data) {//not self call
            recyclerDataList.clear();
            recyclerDataList.addAll(data);
        }
        System.out.println(">>>>>>>>>>" + recyclerDataList);

        workingBookId = Contexts.instance().getWorkingBookId();
        recyclerAdapter.notifyDataSetChanged();
//        recyclerAdapter.notifyItemRangeChanged(0, recyclerDataList.size());
    }


    public void doNewBook() {
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, true);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }


    public void doEditBook(int pos) {
        Book book = recyclerDataList.get(pos);
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(BookEditorActivity.PARAM_BOOK, book);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }

    public void doDeleteBook(final int pos) {
        final Book book = recyclerDataList.get(pos);
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
                            recyclerDataList.remove(pos);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                        Contexts.instance().deleteData(book);
                    }
                }
                return true;
            }
        });
    }

    public void doSetWorkingBook(int pos) {
        Book d = recyclerDataList.get(pos);
        if (Contexts.instance().getWorkingBookId() == d.getId()) {
            return;
        }
        Contexts.instance().setWorkingBookId(d.getId());

        reloadData(recyclerDataList);
    }


    public interface OnBookListener {
        void onBookDeleted(Book detail);
    }


    private class BookRecyclerAdapter extends RecyclerView.Adapter<BookViewHolder> {

        List<Book> list;

        public BookRecyclerAdapter(List<Book> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookViewHolder(inflater.inflate(R.layout.book_mgnt_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            holder.bindViewValue(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class BookViewHolder extends RecyclerView.ViewHolder {

        public BookViewHolder(View itemView) {
            super(itemView);
        }

        public void bindViewValue(Book book) {

            ImageView vicon = itemView.findViewById(R.id.book_item_icon);
            TextView vname = itemView.findViewById(R.id.book_item_name);
            TextView vid = itemView.findViewById(R.id.book_item_id);
            TextView vnote = itemView.findViewById(R.id.book_item_note);
            TextView vsymbol = itemView.findViewById(R.id.book_item_symbol);

            vname.setText(book.getName());
            vid.setText(Integer.toString(book.getId()));
            vnote.setText(book.getNote());
            vsymbol.setText(book.getSymbol());

            if (book.getId() == workingBookId) {
                vicon.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_active));
            } else {
                vicon.setImageDrawable(Contexts.instance().getDrawable(R.drawable.book_notactive));
            }
        }
    }

}
