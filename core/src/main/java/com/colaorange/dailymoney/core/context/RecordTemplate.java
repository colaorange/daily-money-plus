package com.colaorange.dailymoney.core.context;

import com.colaorange.dailymoney.core.data.Account;
import com.colaorange.dailymoney.core.data.AccountType;
import com.colaorange.dailymoney.core.util.I18N;
import com.google.gson.annotations.Expose;

/**
 * @author Dennis
 */
public class RecordTemplate {
    @Expose
    public final int index;
    @Expose
    public final String from;
    @Expose
    public final String to;
    @Expose
    public final String note;

    public RecordTemplate(int index, String from, String to, String note) {
        this.index = index;
        this.from = from;
        this.to = to;
        this.note = note;
    }


    @Override
    public String toString() {
        return this.from + " > " + this.to;
    }


    public String toString(I18N i18n) {
        AccountType fromType = AccountType.UNKONW;
        AccountType toType = AccountType.UNKONW;
        String fromStr = from;
        String toStr = to;
        if (from != null && from.length() > 2) {
            fromType = AccountType.find(from.substring(0, 1));
            fromStr = from.substring(2);
        }
        if (to != null && to.length() > 2) {
            toType = AccountType.find(to.substring(0, 1));
            toStr = to.substring(2);
        }
        
        return fromType.getDisplay(i18n) + "." + fromStr + " > " + toType.getDisplay(i18n) + "." + toStr;
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
