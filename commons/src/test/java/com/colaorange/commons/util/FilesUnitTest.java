package com.colaorange.commons.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;

/**
 * @author Dennis
 */
public class FilesUnitTest {
    @Test
    public void normalizeFileNameTest() {
        Assert.assertEquals("中ABC1234`-=[];',.~!@#$%^&()_+{}文XYZ890", Files.normalizeFileName("中ABC1234`-=[]\\;',./~!@#$%^&*()_+{}|:\"<>?文XYZ890"));
    }


}
