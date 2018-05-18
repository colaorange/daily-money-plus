package com.colaorange.dailymoney.core.ui.legacy;

import android.app.Activity;

/**
 * 
 * @author dennis
 *
 */
public abstract class AbstractDesktop extends Desktop {
    public AbstractDesktop(String name, Activity activity) {
        super(name, activity);
        init();
    }
    abstract protected void init();
}
