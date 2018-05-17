package com.colaorange.dailymoney.core.context;

import com.colaorange.commons.util.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;

/**
 * Created by Dennis
 */
public class RecordTemplateCollection extends JsonObject {


    public static final int MAX_SIZE = 10;

    @Expose
    int bookId;

    @Expose
    LinkedHashMap<Integer, RecordTemplate> templates;

    //for json
    protected RecordTemplateCollection() {
    }

    public RecordTemplateCollection(int bookId) {
        //read from preference
        this.bookId = bookId;
    }


    public int size() {
        return MAX_SIZE;
    }

    public RecordTemplate getTemplateIfAny(int index) {
        checkIdx(index);
        return templates.get(index);
    }

    public void setBookmark(int index, String from, String to) {
        checkIdx(index);
        templates.put(index, new RecordTemplate(index, from, to));
    }

    public void clear(int index) {
        templates.remove(index);
    }

    public void clear() {
        templates.clear();
    }

    private void checkIdx(int idx) {
        if (idx >= MAX_SIZE) {
            throw new IllegalArgumentException(idx + ">=" + MAX_SIZE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordTemplateCollection that = (RecordTemplateCollection) o;

        return bookId == that.bookId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bookId;
        return result;
    }
}
