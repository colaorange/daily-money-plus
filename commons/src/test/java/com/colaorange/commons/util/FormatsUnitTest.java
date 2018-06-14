package com.colaorange.commons.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author Dennis
 */
public class FormatsUnitTest {
    @Test
    public void getMoneyDecimalTest() {

        DecimalFormat f = Formats.getMoneyFormat();
        Assert.assertEquals(0, Formats.getDecimalLength(f, 23));
        Assert.assertEquals(1, Formats.getDecimalLength(f, 23.1));
        Assert.assertEquals(2, Formats.getDecimalLength(f, 23.11));
        Assert.assertEquals(3, Formats.getDecimalLength(f, 23.111));
        Assert.assertEquals(3, Formats.getDecimalLength(f, 23.1111));
    }
    @Test
    public void getFormatTest() {

        DecimalFormat f = Formats.getFormat(false, 0);
        Assert.assertEquals("1235", f.format(1234.5678));
        f = Formats.getFormat(false, 0);
        Assert.assertEquals("1234", f.format(1234.1234));
        f = Formats.getFormat(false, 2);
        Assert.assertEquals("1234.57", f.format(1234.5678));
        f = Formats.getFormat(true, 3);
        Assert.assertEquals("1,234.568", f.format(1234.5678));

        f = Formats.getFormat(true, 3);
        Assert.assertEquals("1,234.000", f.format(1234));
    }

}
