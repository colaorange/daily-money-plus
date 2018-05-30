package com.colaorange.dailymoney.core.ui.helper;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;

/**
 * @author dennis
 */
public class XAxisDateFormatter implements IAxisValueFormatter {

    DateFormat format;

    public XAxisDateFormatter(DateFormat format) {
        this.format = format;
    }


    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return format.format(Float.valueOf(value).longValue());
    }
}