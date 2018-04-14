package com.colaorange.dailymoney.ui.legacy;

import android.app.Activity;

/**
 * 
 * @author dennis
 *
 */
public abstract class AbstractDesktop extends Desktop {
    public AbstractDesktop(Activity activity) {
        super(activity);
        init();
    }
    abstract protected void init();
}
