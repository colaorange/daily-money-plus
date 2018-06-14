package com.colaorange.dailymoney.core.ui.chart;

import com.colaorange.commons.util.Numbers;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

/**
 * @author Dennis
 */
public class MoneyFormatter implements IValueFormatter {
        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return Numbers.format(v, "#0.##");
        }
    }