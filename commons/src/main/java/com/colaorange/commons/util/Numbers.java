package com.colaorange.commons.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Dennis Chen
 */
public class Numbers {

    public static final long K = 1024L;
    public static final long M = K * 1024L;
    public static final long G = M * 1024L;
    public static final long T = G * 1024L;

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60000L;//60 * 1000L;
    public static final long HOUR = 3600000L;//60 * 60 * 1000L;
    public static final long DAY = 86400000L;//24 * 60 * 60 * 1000L;

    public static int compare(Number x, Number y) {
        if (x == null) {
            x = BigDecimal.ZERO;
        }
        if (y == null) {
            y = BigDecimal.ZERO;
        }
        if (isSpecial(x) || isSpecial(y))
            return Double.compare(x.doubleValue(), y.doubleValue());
        else
            return toBigDecimal(x).compareTo(toBigDecimal(y));
    }

    private static boolean isSpecial(final Number x) {
        boolean specialDouble = x instanceof Double
                && (Double.isNaN((Double) x) || Double.isInfinite((Double) x));
        boolean specialFloat = x instanceof Float
                && (Float.isNaN((Float) x) || Float.isInfinite((Float) x));
        return specialDouble || specialFloat;
    }

    private static BigDecimal toBigDecimal(final Number number) {
        if (number instanceof BigDecimal)
            return (BigDecimal) number;
        if (number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);
        if (number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long)
            return new BigDecimal(number.longValue());
        if (number instanceof Float || number instanceof Double)
            return new BigDecimal(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch (final NumberFormatException e) {
            throw new RuntimeException("The given number (\"" + number
                    + "\" of class " + number.getClass().getName()
                    + ") does not have a parsable string representation", e);
        }
    }

    public static Number minus(Number x, Number y) {
        if (x instanceof BigDecimal || y instanceof BigDecimal
                || x instanceof BigInteger || y instanceof BigInteger) {
            return toBigDecimal(x).add(toBigDecimal(y).negate());
        } else if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() - y.doubleValue();
        } else if (x instanceof Float || y instanceof Float) {
            return x.floatValue() - y.floatValue();
        } else if (x instanceof Long || y instanceof Long) {
            return x.longValue() - y.longValue();
        } else if (x instanceof Integer || y instanceof Integer) {
            return x.intValue() - y.intValue();
        } else if (x instanceof Short || y instanceof Short) {
            return x.shortValue() - y.shortValue();
        } else {
            return x.doubleValue() - y.doubleValue();
        }
    }

    public static Number plus(Number x, Number y) {
        if (x instanceof BigDecimal || y instanceof BigDecimal
                || x instanceof BigInteger || y instanceof BigInteger) {
            return toBigDecimal(x).add(toBigDecimal(y));
        } else if (x instanceof Double || y instanceof Double) {
            return x.doubleValue() + y.doubleValue();
        } else if (x instanceof Float || y instanceof Float) {
            return x.floatValue() + y.floatValue();
        } else if (x instanceof Long || y instanceof Long) {
            return x.longValue() + y.longValue();
        } else if (x instanceof Integer || y instanceof Integer) {
            return x.intValue() + y.intValue();
        } else if (x instanceof Short || y instanceof Short) {
            return x.shortValue() + y.shortValue();
        } else {
            return x.doubleValue() + y.doubleValue();
        }
    }

    public static boolean isZero(Number x) {
        if (x instanceof BigDecimal) {
            return ((BigDecimal) x).signum() == 0;
        } else if (x instanceof BigInteger) {
            return ((BigInteger) x).signum() == 0;
        }
        return x.doubleValue() == 0;
    }

    public static boolean isNegative(Number x) {
        if (x instanceof BigDecimal) {
            return ((BigDecimal) x).signum() < 0;
        } else if (x instanceof BigInteger) {
            return ((BigInteger) x).signum() < 0;
        }
        return x.doubleValue() < 0;
    }

    public static boolean isPositive(Number x) {
        if (x instanceof BigDecimal) {
            return ((BigDecimal) x).signum() > 0;
        } else if (x instanceof BigInteger) {
            return ((BigInteger) x).signum() > 0;
        }
        return x.doubleValue() > 0;
    }

    public static String formatCurrency(Number x) {
        return formatCurrency(x, Locale.getDefault());
    }

    public static String formatCurrency(Number x, Locale locale) {
        NumberFormat f = NumberFormat.getCurrencyInstance(locale);
        return f.format(x);
    }

    public static String format(Number x) {
        return format(x, ",##0.##");
    }

    final public static void main(String[] args) {
        long k = 1;
        for (int i = 0; i < 15; i++) {
            System.out.println(Numbers.format(k));
            System.out.println(Numbers.formatCurrency(k));
            k = k * 10 + i;
        }
    }

    public static String format(Number x, String pattern) {
        DecimalFormat f = new DecimalFormat(pattern);
        return f.format(x);
    }
}

