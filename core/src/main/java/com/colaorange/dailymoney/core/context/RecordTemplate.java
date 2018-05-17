package com.colaorange.dailymoney.core.context;

import com.google.gson.annotations.Expose;

/**
 * Created by Dennis
 */
public class RecordTemplate {
    @Expose
    public final int index;
    @Expose
    public final String from;
    @Expose
    public final String to;

    public RecordTemplate(int index, String from, String to) {
        this.index = index;
        this.from = from;
        this.to = to;
    }


    @Override
    public String toString(){
        return this.from+" > "+this.to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordTemplate that = (RecordTemplate) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + index;
        return result;
    }
}
