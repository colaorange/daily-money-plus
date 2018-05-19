package com.colaorange.dailymoney.core.ui.legacy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.ContextsActivity;
import com.colaorange.dailymoney.core.data.Book;
import com.colaorange.dailymoney.core.ui.helper.SelectableRecyclerViewAdaptor;

import java.util.List;

/**
 * @author dennis
 */
public class BookRecyclerAdapter extends SelectableRecyclerViewAdaptor<Book, SelectableRecyclerViewAdaptor.SelectableViewHolder> {

    private LayoutInflater inflater;

    public BookRecyclerAdapter(ContextsActivity activity, List<Book> data) {
        super(activity, data);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewItem = inflater.inflate(R.layout.book_mgnt_item, parent, false);
        return new BookViewHolder(this, viewItem);
    }


    private class BookViewHolder extends SelectableRecyclerViewAdaptor.SelectableViewHolder<BookRecyclerAdapter, Book> {

        public BookViewHolder(BookRecyclerAdapter adapter, View itemView) {
            super(adapter, itemView);
        }

        @Override
        public void bindViewValue(Book book) {
            super.bindViewValue(book);

            //it is possible change and just reload adaptor, so we have to get it every time.
            int workingBookId = Contexts.instance().getWorkingBookId();

            ImageView vicon = itemView.findViewById(R.id.book_item_icon);
            TextView vname = itemView.findViewById(R.id.book_item_name);
            TextView vid = itemView.findViewById(R.id.book_item_id);
            TextView vnote = itemView.findViewById(R.id.book_item_note);
            TextView vsymbol = itemView.findViewById(R.id.book_item_symbol);

            vname.setText(book.getName());
            vid.setText(Integer.toString(book.getId()));
            vnote.setText(book.getNote());
            vsymbol.setText(book.getSymbol());

            vicon.setImageDrawable(activity.buildGrayIcon(R.drawable.book, book.getId() == workingBookId));
        }
    }
}