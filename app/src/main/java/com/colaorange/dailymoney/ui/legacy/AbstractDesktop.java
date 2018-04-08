package com.colaorange.dailymoney.ui.legacy;

import android.app.Activity;

import com.colaorange.commons.util.I18N;
import com.colaorange.dailymoney.context.Contexts;
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
