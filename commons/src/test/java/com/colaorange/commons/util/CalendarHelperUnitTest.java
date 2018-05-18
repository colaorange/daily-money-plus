package com.colaorange.commons.util;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.ComparableVersion;

import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dennis
 */
public class CalendarHelperUnitTest {
    @Test
    public void startMonthDayOfYear() {
        try {
            CalendarHelper helper = new CalendarHelper();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            Date date = format.parse("2017/03/04");

            Assert.assertEquals("2017/01/01 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2017/12/31 23:59:59", format2.format(helper.yearEndDate(date)));

            date = format.parse("2017/8/30");
            Assert.assertEquals("2017/01/01 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2017/12/31 23:59:59", format2.format(helper.yearEndDate(date)));

            //1/15
            helper.setStartMonthDayOfYear(15);

            date = format.parse("2017/03/04");
            Assert.assertEquals("2017/01/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2018/01/14 23:59:59", format2.format(helper.yearEndDate(date)));
            date = format.parse("2017/01/14");
            Assert.assertEquals("2016/01/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2017/01/14 23:59:59", format2.format(helper.yearEndDate(date)));

            date = format.parse("2017/01/15");
            Assert.assertEquals("2017/01/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2018/01/14 23:59:59", format2.format(helper.yearEndDate(date)));

            //5/15
            helper.setStartMonthOfYear(4);
            helper.setStartMonthDayOfYear(15);

            date = format.parse("2017/03/04");
            Assert.assertEquals("2016/05/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2017/05/14 23:59:59", format2.format(helper.yearEndDate(date)));
            date = format.parse("2017/05/14");
            Assert.assertEquals("2016/05/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2017/05/14 23:59:59", format2.format(helper.yearEndDate(date)));

            date = format.parse("2017/8/30");
            Assert.assertEquals("2017/05/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2018/05/14 23:59:59", format2.format(helper.yearEndDate(date)));
            date = format.parse("2017/5/15");
            Assert.assertEquals("2017/05/15 00:00:00", format2.format(helper.yearStartDate(date)));
            Assert.assertEquals("2018/05/14 23:59:59", format2.format(helper.yearEndDate(date)));




        }catch (Exception x){
            x.printStackTrace();;
            Assert.fail(x.getMessage());
        }

    }

}
