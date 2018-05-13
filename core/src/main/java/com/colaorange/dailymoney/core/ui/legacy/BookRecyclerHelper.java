package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.ui.Constants;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;
import com.colaorange.dailymoney.core.util.GUIs;
import com.colaorange.dailymoney.core.util.I18N;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author dennis
 */
public class BookRecyclerHelper /*implements OnItemClickListener */ {


    private List<Book> recyclerDataList;

    private RecyclerView vRecycler;

    private BookRecyclerAdapter recyclerAdapter;

    private OnBookListener listener;

    private ContextsActivity activity;

    private int workingBookId;

    LayoutInflater inflater;

    public BookRecyclerHelper(ContextsActivity activity, OnBookListener listener) {
        this.activity = activity;
        this.listener = listener;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setup(RecyclerView vRecycler) {
        workingBookId = Contexts.instance().getWorkingBookId();
        recyclerDataList = new LinkedList<>();
        recyclerAdapter = new BookRecyclerAdapter(activity, recyclerDataList);
        this.vRecycler = vRecycler;
        vRecycler.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        vRecycler.setLayoutManager(new LinearLayoutManager(activity));
        vRecycler.setAdapter(recyclerAdapter);


        recyclerAdapter.setOnSelectListener(new SelectableRecyclerViewAdaptor.OnSelectListener<Book>() {
            @Override
            public void onSelect(Set<Book> selection) {
                listener.onBookSelected(selection.size() == 0 ? null : selection.iterator().next());
            }
        });
    }


    public void reloadData(List<Book> data) {
        if (recyclerDataList != data) {//not self call
            recyclerDataList.clear();
            recyclerDataList.addAll(data);
        }

        workingBookId = Contexts.instance().getWorkingBookId();
        recyclerAdapter.notifyDataSetChanged();
    }


    public void doNewBook() {
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, true);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }


    public void doEditBook(Book book) {
        Intent intent = null;
        intent = new Intent(activity, BookEditorActivity.class);
        intent.putExtra(BookEditorActivity.PARAM_MODE_CREATE, false);
        intent.putExtra(BookEditorActivity.PARAM_BOOK, book);
        activity.startActivityForResult(intent, Constants.REQUEST_BOOK_EDITOR_CODE);
    }

    public void doDeleteBook(final Book book) {
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
                            recyclerAdapter.remove(book);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                        Contexts.instance().deleteData(book);
                    }
                }
                return true;
            }
        });
    }

    public void doSetWorkingBook(Book book) {
        if (Contexts.instance().getWorkingBookId() == book.getId()) {
            return;
        }
        Contexts.instance().setWorkingBookId(book.getId());

        reloadData(recyclerDataList);
    }

    public void clearSelection() {
        recyclerAdapter.clearSelection();
    }


    public interface OnBookListener {
        /**
         * select or dis select a book
         */
        void onBookSelected(Book book);

        void onBookDeleted(Book book);
    }


    public class BookRecyclerAdapter extends SelectableRecyclerViewAdaptor<Book, BookViewHolder> {

        public BookRecyclerAdapter(ContextsActivity activity, List<Book> data) {
            super(activity, data);
        }

        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.book_mgnt_item, parent, false);
            return new BookViewHolder(this, viewItem);
        }
    }

    public class BookViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<BookRecyclerAdapter, Book> {

        public BookViewHolder(BookRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Book book) {
            super.bindViewValue(book);

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
