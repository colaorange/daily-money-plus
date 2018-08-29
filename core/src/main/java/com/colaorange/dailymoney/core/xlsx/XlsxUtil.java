package com.colaorange.dailymoney.core.xlsx;

/**
 * Created by Dennis
 */
public class XlsxUtil {

    public static final int WIDTH_BASE = 256;

    public static int characterToWidth(int i) {
        return i * WIDTH_BASE;
    }
}
