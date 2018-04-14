package com.colaorange.commons.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author dennis
 */
public class Formats {


    public static DecimalFormat getDoubleFormat() {
        return new DecimalFormat("#0.###");
    }

    public static DecimalFormat getMoneyFormat() {
        return new DecimalFormat("###,###,###,##0.###");
    }

    public static DateFormat getNormalizeDateFormat() {
        //never chnage nor format, it effect import/export
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    }

    public static DateFormat getNormalizeDateTimeFormat() {
        //never chnage nor format, it effect import/export
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    }

    public static DecimalFormat getNormalizeDoubleFormat() {
        //never chnage nor format, it effect import/export
        return new DecimalFormat("#0.###");
    }


    public static String double2String(Double d) {
        return double2String(d == null ? 0D : d);
    }

    public static String double2String(double d) {
        return getDoubleFormat().format(d);
    }

    public static double string2Double(String d)  throws ParseException {

        return getDoubleFormat().parse(d).doubleValue();
    }

    public static String normalizeDouble2String(Double d) {
        return normalizeDouble2String(d == null ? 0D : d);
    }

    public static String normalizeDouble2String(double d) {
        return getNormalizeDoubleFormat().format(d);
    }

    public static double normalizeString2Double(String d) throws ParseException {
        return getNormalizeDoubleFormat().parse(d).doubleValue();

    }

    public static String normalizeDate2String(Date date) {
        return getNormalizeDateFormat().format(date);
    }

    public static Date normalizeString2Date(String date) throws ParseException {
        return getNormalizeDateFormat().parse(date);
    }

    public static String normalizeDateTime2String(Date date) {
        return getNormalizeDateTimeFormat().format(date);
    }

    public static Date normalizeString2DateTime(String date) throws ParseException {
        return getNormalizeDateTimeFormat().parse(date);
    }

    public static String editorTextNumberDecimalToCal2(String value) throws ParseException {
        DecimalFormat f = getDoubleFormat();

        double d = f.parse(value).doubleValue();
        value = f.format(d).replace(f.getDecimalFormatSymbols().getDecimalSeparator(), '.');

        return value;
    }

    public static String cal2ToEditorTextNumberDecimal(String value) throws ParseException {
        DecimalFormat f = getDoubleFormat();

        value = value.replace('.', f.getDecimalFormatSymbols().getDecimalSeparator());
        double d = f.parse(value).doubleValue();
        value = f.format(d);

        return value;
    }
}
