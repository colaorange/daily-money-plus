package com.colaorange.dailymoney.core.context;

import com.colaorange.commons.util.JsonObject;
import com.google.gson.annotations.Expose;

import java.util.LinkedHashMap;

/**
 * @author Dennis
 */
public class RecordTemplateCollection extends JsonObject {


    public static final int MAX_SIZE = 10;

    @Expose
    int book;

    @Expose
    LinkedHashMap<Integer, RecordTemplate> templates;

    //for json
    protected RecordTemplateCollection() {
    }

    public RecordTemplateCollection(int book) {
        //read from preference
        this.book = book;
    }


    public int size() {
        return MAX_SIZE;
    }

    public RecordTemplate getTemplateIfAny(int index) {
        checkIdx(index);
        return templates==null?null:templates.get(index);
    }

    public void setTemplate(int index, String from, String to, String note) {
        checkIdx(index);
        if(templates==null){
            templates = new LinkedHashMap<>();
        }
        templates.put(index, new RecordTemplate(index, from, to, note));
    }

    public void clear(int index) {
        if(templates!=null){
            templates.remove(index);
        }
    }

    public void clear() {
        if(templates!=null) {
            templates.clear();
        }
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

        return book == that.book;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + book;
        return result;
    }
}
