package com.colaorange.commons.util;

import com.colaorange.commons.util.ComparableVersion;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Dennis
 */
public class ComparableVersionUnitTest {
    @Test
    public void normalTest() {
        ComparableVersion v1 = new ComparableVersion("0.9.8-rc1");
        ComparableVersion v2 = new ComparableVersion("0.9.8-rc2");
        ComparableVersion v3 = new ComparableVersion("0.9.9-rc");
        ComparableVersion v4 = new ComparableVersion("0.9.9-fl");
        ComparableVersion v5 = new ComparableVersion("0.9.9-fl-20180401");
        ComparableVersion v6 = new ComparableVersion("0.9.9-fl-20180403");
        ComparableVersion v7 = new ComparableVersion("0.9.9-ga");

        System.out.println(">>>"+v1);
        System.out.println(">>>"+v2);
        System.out.println(">>>"+v3);
        System.out.println(">>>"+v4);
        System.out.println(">>>"+v5);
        System.out.println(">>>"+v6);
        System.out.println(">>>"+v7);
        Assert.assertEquals(-1,v1.compareTo(v2));
        Assert.assertEquals(-1,v2.compareTo(v3));
        Assert.assertEquals(-1,v3.compareTo(v4));
        Assert.assertEquals(-1,v4.compareTo(v5));
        Assert.assertEquals(-1,v5.compareTo(v6));
        Assert.assertEquals(-1,v6.compareTo(v7));
    }

    @Test
    public void semanticTest() {
        ComparableVersion v1 = new ComparableVersion("0.9.8-rc1");
        ComparableVersion v2 = new ComparableVersion("0.9.8-fl-20180401");
        ComparableVersion v3 = new ComparableVersion("0.9.8-fl-20180403");
        ComparableVersion v4 = new ComparableVersion("0.9.8-ga");
        ComparableVersion v5 = new ComparableVersion("0.9.9-fl-20180405");
        ComparableVersion v6 = new ComparableVersion("0.9.9-ga");

        System.out.println(">>>"+v1);
        System.out.println(">>>"+v2);
        System.out.println(">>>"+v3);
        System.out.println(">>>"+v4);
        System.out.println(">>>"+v5);
        System.out.println(">>>"+v6);

        Assert.assertEquals(-1,v1.compareTo(v2));
        Assert.assertEquals(-1,v2.compareTo(v3));
        Assert.assertEquals(-1,v3.compareTo(v4));
        Assert.assertEquals(-1,v4.compareTo(v5));
        Assert.assertEquals(-1,v5.compareTo(v6));

        Assert.assertEquals("0.9.8",v1.getSemantic());
        Assert.assertEquals("0.9.8",v2.getSemantic());
        Assert.assertEquals("0.9.8",v3.getSemantic());
        Assert.assertEquals("0.9.8",v4.getSemantic());
        Assert.assertEquals("0.9.9",v5.getSemantic());
        Assert.assertEquals("0.9.9",v6.getSemantic());

        Assert.assertEquals(false,v1.isSnapshot());
        Assert.assertEquals(true,v2.isSnapshot());
        Assert.assertEquals(true,v3.isSnapshot());
        Assert.assertEquals(false,v4.isSnapshot());
        Assert.assertEquals(true,v5.isSnapshot());
        Assert.assertEquals(false,v6.isSnapshot());

    }
}
