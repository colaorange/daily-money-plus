package com.colaorange.dailymoney.core.context;

/**
 *
 */
public enum PeriodMode {
    DAILY(0),
    MONTHLY(1),
    WEEKLY(2), //week could cross month, so has higher value than monthly
    YEARLY(3),
    ALL(4);

    int value;

    PeriodMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}