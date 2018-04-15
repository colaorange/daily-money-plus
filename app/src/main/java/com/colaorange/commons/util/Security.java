package com.colaorange.commons.util;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author Dennis Chen
 */
public class Security {


    public static String md5String(String input) {
        if (null == input)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("UTF8"), 0, input.length());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] md5(String input) {
        if (null == input)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("UTF8"), 0, input.length());
            return md.digest();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String md5String(InputStream is) {
        if (null == is)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[100];
            int i;
            while ((i = is.read(buffer)) != -1) {
                md.update(buffer, 0, i);
            }
            return new BigInteger(1, md.digest()).toString(16);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] md5(InputStream is) {
        if (null == is)
            return null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[100];
            int i;
            while ((i = is.read(buffer)) != -1) {
                md.update(buffer, 0, i);
            }
            return md.digest();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}