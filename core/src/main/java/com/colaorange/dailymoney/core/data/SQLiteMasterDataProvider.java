package com.colaorange.dailymoney.core.data;

import static com.colaorange.dailymoney.core.data.MasterDataMeta.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.context.Contexts;

/**
 * @author dennis
 */
public class SQLiteMasterDataProvider implements IMasterDataProvider {

    SQLiteMasterDataHelper helper;
    CalendarHelper calHelper;

    public SQLiteMasterDataProvider(SQLiteMasterDataHelper helper, CalendarHelper calHelper) {
        this.helper = helper;
        this.calHelper = calHelper;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroyed() {
        helper.close();
    }

    @Override
    public void reset() {
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.onUpgrade(db, -1, db.getVersion());
    }


    /**
     * book impl.
     */

    private void applyCursor(Book book, Cursor c) {
        int i = 0;
        for (String n : c.getColumnNames()) {
            if (n.equals(COL_BOOK_ID)) {
                book.setId(c.getInt(i));
            } else if (n.equals(COL_BOOK_NAME)) {
                book.setName(c.getString(i));
            } else if (n.equals(COL_BOOK_SYMBOL)) {
                book.setSymbol(c.getString(i));
            } else if (n.equals(COL_BOOK_SYMBOL_POSITION)) {
                book.setSymbolPosition(SymbolPosition.find(c.getInt(i)));
            } else if (n.equals(COL_BOOK_NOTE)) {
                book.setNote(c.getString(i));
            } else if (n.equals(COL_BOOK_PRIORITY)) {
                try {
                    book.setPriority(c.getInt(i));
                }catch(Exception x){
                    //prevent null
                }
            }
            i++;
        }
    }

    private void applyContextValue(Book book, ContentValues values) {
        values.put(COL_BOOK_ID, book.getId());
        values.put(COL_BOOK_NAME, book.getName());
        values.put(COL_BOOK_SYMBOL, book.getSymbol());
        values.put(COL_BOOK_SYMBOL_POSITION, book.getSymbolPosition().getType());
        values.put(COL_BOOK_NOTE, book.getNote());
        values.put(COL_BOOK_PRIORITY, book.getPriority());
    }

    @Override
    public Book findBook(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(TB_BOOK, COL_BOOK_ALL, COL_BOOK_ID + " = " + id, null, null, null, null, "1");
        Book book = null;
        if (c.moveToNext()) {
            book = new Book();
            applyCursor(book, c);
        }
        c.close();
        return book;
    }

    public synchronized int nextBookId() {
        int bookId = Contexts.DEFAULT_BOOK_ID;

        //can't use sql max, the book id is text
        for(Book book:listAllBook()){
            bookId = Math.max(bookId,book.getId());
        }

        return bookId+1;
    }

    @Override
    public void newBook(Book bookail) {
        int id = nextBookId();
        try {
            newBook(id, bookail);
        } catch (DuplicateKeyException e) {
            Logger.e(e.getMessage(), e);
        }
    }

    public void newBook(int id, Book book) throws DuplicateKeyException {
        if (findBook(id) != null) {
            throw new DuplicateKeyException("duplicate book id " + id);
        }
        newBookNoCheck(id, book);
    }

    @Override
    public void newBookNoCheck(int id, Book book) {
        Logger.d("new book {}, {}", id, book.getName());

        book.setId(id);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        applyContextValue(book, cv);
        db.insertOrThrow(TB_BOOK, null, cv);
    }

    @Override
    public boolean updateBook(int id, Book book) {
        Book det = findBook(id);
        if (det == null) {
            return false;
        }
        //set id, book might have a dirty id from copy or zero
        book.setId(id);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        applyContextValue(book, cv);

        //use old id to update
        int r = db.update(TB_BOOK, cv, COL_BOOK_ID + " = " + id, null);
        return r > 0;
    }

    @Override
    public boolean deleteBook(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        boolean r = db.delete(TB_BOOK, COL_BOOK_ID + " = " + id, null) > 0;
        return r;
    }

    @Override
    public List<Book> listAllBook() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = null;
        c = db.query(TB_BOOK, COL_BOOK_ALL, null, null, null, null, BOOK_ORDERBY);
        List<Book> result = new ArrayList<Book>();
        Book det;
        while (c.moveToNext()) {
            det = new Book();
            applyCursor(det, c);
            result.add(det);
        }
        c.close();

        Collections.sort(result, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getPriority() == o2.getPriority()) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
                return o1.getPriority() > o2.getPriority() ? 1 : 0;
            }
        });

        return result;
    }

    static final String BOOK_ORDERBY = COL_BOOK_ID + " ASC";

}
