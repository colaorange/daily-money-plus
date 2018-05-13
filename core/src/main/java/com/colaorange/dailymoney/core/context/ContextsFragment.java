package com.colaorange.dailymoney.core.context;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.util.I18N;

/**
 * @author dennis
 */
@InstanceState(stopLookup = true)
public class ContextsFragment extends Fragment{

    private InstanceStateHelper instanceStateHelper;

    public ContextsFragment(){
        instanceStateHelper = new InstanceStateHelper(this);
    }

    public void trackEvent(String action) {
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            ((ContextsActivity) activity).trackEvent(action);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        instanceStateHelper.onRestore(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        instanceStateHelper.onBackup(savedInstanceState);
    }

    protected I18N i18n() {
        return contexts().getI18n();
    }

    protected CalendarHelper calendarHelper() {
        return preference().getCalendarHelper();
    }

    protected Contexts contexts() {
        return Contexts.instance();
    }

    protected Preference preference() {
        return contexts().getPreference();
    }


    protected ContextsActivity getContextsActivity(){
        Activity activity = getActivity();
        if (activity instanceof ContextsActivity) {
            return (ContextsActivity) activity;
        }
        throw new IllegalStateException("not a contexts activity, is "+activity.getClass());
    }
}